package com.example.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class OperatorTest {

    @BeforeAll
    public static void setUp(){
        BlockHound.install();
    }

    @Test
    public void switchIfEmptyOperator(){
        Flux<Object> flux = emptyFlux()
                .switchIfEmpty(Flux.just("Not empty anymore"))
                .log();

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext("Not empty anymore")
                .expectComplete()
                .verify();
    }

    private Flux<Object> emptyFlux (){
        return Flux.empty();
    }

    @Test
    public void deferOperator() throws Exception{
        Mono<Long> mono = Mono.just(System.currentTimeMillis());
        Mono<Long> defer = Mono.defer(() -> Mono.just(System.currentTimeMillis()));

        defer.subscribe(l -> log.info("time {}", l));
        Thread.sleep(200);
        defer.subscribe(l -> log.info("time {}", l));
        Thread.sleep(200);
        defer.subscribe(l -> log.info("time {}", l));
        Thread.sleep(200);
        defer.subscribe(l -> log.info("time {}", l));

        AtomicLong atomicLong = new AtomicLong();
        defer.subscribe(atomicLong::set);
        Assertions.assertTrue(atomicLong.get() > 0);
    }

    @Test
    public void concatOperator(){
        Flux<String> str1 = Flux.just("a", "b");
        Flux<String> str2 = Flux.just("c", "d");

        Flux<String> concatFlux = Flux.concat(str1, str2).log();

        StepVerifier
                .create(concatFlux)
                .expectSubscription()
                .expectNext("a", "b", "c", "d")
                .expectComplete()
                .verify();
    }

    @Test
    public void concatWithOperator(){
        Flux<String> str1 = Flux.just("a", "b");
        Flux<String> str2 = Flux.just("c", "d");

        Flux<String> concatFlux = str1.concatWith(str2).log();

        StepVerifier
                .create(concatFlux)
                .expectSubscription()
                .expectNext("a", "b", "c", "d")
                .expectComplete()
                .verify();
    }

    @Test
    public void combineLatestOperator(){
        Flux<String> str1 = Flux.just("a", "b");
        Flux<String> str2 = Flux.just("c", "d");

        Flux<String> combineLatest = Flux
                .combineLatest(str1, str2,
                        (s1, s2) -> s1.toUpperCase() + s2.toUpperCase())
                .log();

        StepVerifier
                .create(combineLatest)
                .expectSubscription()
                .expectNext("BC", "BD")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeOperator() throws Exception{
        Flux<String> str1 = Flux.just("a", "b").delayElements(Duration.ofMillis(200));
        Flux<String> str2 = Flux.just("c", "d");

        Flux<String> mergeFlux = Flux.merge(str1, str2)
                .delayElements(Duration.ofMillis(200))
                .log();

//        mergeFlux.subscribe(log::info);
//        Thread.sleep(1000);

        StepVerifier
                .create(mergeFlux)
                .expectSubscription()
                .expectNext("c", "d", "a", "b")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeWithOperator() throws Exception{
        Flux<String> str1 = Flux.just("a", "b").delayElements(Duration.ofMillis(200));
        Flux<String> str2 = Flux.just("c", "d");

        Flux<String> mergeFlux = str1.mergeWith(str2)
                .delayElements(Duration.ofMillis(200))
                .log();

        StepVerifier
                .create(mergeFlux)
                .expectSubscription()
                .expectNext("c", "d", "a", "b")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeSequentialOperator() throws Exception{
        Flux<String> str1 = Flux.just("a", "b").delayElements(Duration.ofMillis(200));
        Flux<String> str2 = Flux.just("c", "d");

        Flux<String> mergeFlux = Flux.mergeSequential(str1, str2, str1, str1)
                .delayElements(Duration.ofMillis(200))
                .log();

        StepVerifier
                .create(mergeFlux)
                .expectSubscription()
                .expectNext("a", "b","c", "d", "a", "b", "a", "b")
                .expectComplete()
                .verify();
    }

    @Test
    public void concatOperatorError(){
        Flux<String> str1 = Flux.just("a", "b")
                .map(s -> {
                    if(s.equals("b"))
                        throw new IllegalArgumentException();
                    else
                        return s;
                });
        Flux<String> str2 = Flux.just("c", "d");

        Flux<String> concatFlux = Flux.concatDelayError(str1, str2).log();

        StepVerifier
                .create(concatFlux)
                .expectSubscription()
                .expectNext("a", "c", "d")
                .expectError()
                .verify();
    }

    @Test
    public void mergeDelayErrorOperator() throws Exception {
        Flux<String> str1 = Flux.just("a", "b")
                .map(s -> {
                    if(s.equals("b"))
                        throw new IllegalArgumentException();
                    else
                        return s;
                })
                .doOnError(t -> log.error("We could do something with this"));
        Flux<String> str2 = Flux.just("c", "d");

        Flux<String> mergeFlux = Flux.mergeDelayError(1, str1, str2, str1)
                .log();

        mergeFlux.subscribe(log::info);

        StepVerifier
                .create(mergeFlux)
                .expectSubscription()
                .expectNext("a","c", "d", "a")
                .expectError()
                .verify();
    }

    @Test
    public void flatMapOperator() throws Exception{
        Flux<String> flux = Flux.just("a","b");

        Flux<String> fluxOfFlux = flux.map(String::toUpperCase)
                .flatMap(this::findByName)
                .log();

        fluxOfFlux.subscribe(log::info);
        Thread.sleep(500);

        StepVerifier
                .create(fluxOfFlux)
                .expectSubscription()
                .expectNext("nameB1", "nameB2", "nameA1", "nameA2")
                .verifyComplete();

    }

    @Test
    public void flatMapSequentialOperator() throws Exception{
        Flux<String> flux = Flux.just("a","b");

        Flux<String> fluxOfFlux = flux.map(String::toUpperCase)
                .flatMapSequential(this::findByName)
                .log();

        StepVerifier
                .create(fluxOfFlux)
                .expectSubscription()
                .expectNext("nameA1", "nameA2", "nameB1", "nameB2")
                .verifyComplete();

    }

    public Flux<String> findByName(String name){
        return name.equals("A") ? Flux.just("nameA1", "nameA2").delayElements(Duration.ofMillis(100)) : Flux.just("nameB1", "nameB2");
    }

}
