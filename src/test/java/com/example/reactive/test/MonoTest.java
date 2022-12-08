package com.example.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/*
* Reactive Stream
* 1. Asynchronous
* 2. Non-Blocking
* 3. Backpressure
* Publisher <- (Subscribe) Subscriber
* Subscription is created
* Publisher (OnSubscribe with the Subscription) -> Subscriber
* Subscription <- (request N) Subscriber
* Publisher -> (onNext) Subscriber
* Until:
* 1. publisher sends all the object requested.
* 2. Publisher sends all the object it has (onComplete) Subscriber and subscription will be canceled
* 3. there is an error! (onError) -> Subscriber and subscription will be canceled
* */

@Slf4j
public class MonoTest {

    @BeforeAll
    public static void setUp(){
        BlockHound.install();
    }
    @Test
    public void monoSubscriber(){
        String name = "Mehedi Hasan";
        Mono<String> mono = Mono.just(name)
                        .log();
        mono.subscribe();
        log.info("***************************");

        StepVerifier.create(mono)
                    .expectNext(name)
                    .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumer(){
        String name = "Mehedi Hasan";
        Mono<String> mono = Mono.just(name)
                .log();
        mono.subscribe(s -> log.info("value {}", s));
        log.info("***************************");

        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerError(){
        String name = "Mehedi Hasan";
        Mono<String> mono = Mono.just(name)
                .map(s -> {
                    throw new RuntimeException("Testing mono with error!");
                });
        mono.subscribe(s -> log.info("name {}", s), s -> log.error("something bad happened"));
        mono.subscribe(s -> log.info("name {}", s), Throwable::printStackTrace);
        log.info("***************************");

        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void monoSubscriberComplete(){
        String name = "Mehedi Hasan";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);
        mono.subscribe(s -> log.info("value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!")
                );

        log.info("***************************");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscription(){
        String name = "Mehedi Hasan";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);
        mono.subscribe(s -> log.info("value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!"),
                Subscription::cancel
        );

        log.info("***************************");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscription2(){
        String name = "Mehedi Hasan";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);
        mono.subscribe(s -> log.info("value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!"),
                subscription -> subscription.request(5)
        );

        log.info("***************************");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoDoOnMethods(){
        String name = "Mehedi Hasan";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(longNumber -> log.info("Request Received, Starting Doing Something..."))
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
                .doOnSuccess(s -> log.info("doOnSuccess Executed"));

        mono.subscribe(s -> log.info("value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!")
        );

        log.info("***************************");

//        StepVerifier.create(mono)
//                .expectNext(name.toUpperCase())
//                .verifyComplete();
    }

    @Test
    public void monoDoOnMethods2(){
        String name = "Mehedi Hasan";
        Mono<Object> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(longNumber -> log.info("Request Received, Starting Doing Something..."))
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
                .flatMap(s -> Mono.empty())
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s)) // Will not executed.
                .doOnSuccess(s -> log.info("doOnSuccess Executed {}", s));

        mono.subscribe(s -> log.info("value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!")
        );

        log.info("***************************");

//        StepVerifier.create(mono)
//                .expectNext(name.toUpperCase())
//                .verifyComplete();
    }

    @Test
    public void monoDoOnError(){
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal Argument Exception"))
                        .doOnError(e -> log.error("Error Message:  {}", e.getMessage()))
                                .log();

        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void monoDoOnError2(){
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal Argument Exception"))
                .doOnError(e -> log.error("Error Message:  {}", e.getMessage()))
                .doOnNext(s -> log.info("Executing This doOnNext"))
                .log();

        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void monoDoOnErrorResume(){
        String name = "Nayeem Ahmed";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal Argument Exception"))
                .doOnError(e -> log.error("Error Message:  {}", e.getMessage()))
                .onErrorResume(s -> {
                    log.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                .log();

        StepVerifier.create(error)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoDoOnErrorReturn(){
        String name = "Nayeem Ahmed";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal Argument Exception"))
                .onErrorReturn("mehedi")
                .onErrorResume(s -> {
                    log.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                .doOnError(e -> log.error("Error Message:  {}", e.getMessage()))
                .log();

        StepVerifier.create(error)
                .expectNext("mehedi")
                .verifyComplete();
    }
}
