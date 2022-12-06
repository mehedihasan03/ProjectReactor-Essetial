package com.example.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

@Slf4j
public class FluxTest {

    @Test
    public void fluxSubscriberString(){
        Flux<String> fluxString = Flux.just("Mehedi", "Hasan", "Nayeem", "Ahmed", "celloscope")
                .log();
        StepVerifier.create(fluxString)
                .expectNext("Mehedi", "Hasan", "Nayeem", "Ahmed", "celloscope")
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbers(){
        Flux<Integer> fluxInteger = Flux.just(1,2,5,8,7,4,3,9,7)
                .log();
        StepVerifier.create(fluxInteger)
                .expectNext(1,2,5,8,7,4,3,9,7)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersRange(){
        Flux<Integer> fluxInteger = Flux.range(1, 7)
                .log();
        fluxInteger.subscribe(s -> log.info("Integer {}", s));

        log.info("******************************");
        StepVerifier.create(fluxInteger)
                .expectNext(1,2,3,4,5,6,7)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberFromList(){
        Flux<Integer> fluxInteger = Flux.fromIterable(List.of(1,2,3,4,5,6,7))
                .log();
        fluxInteger.subscribe(s -> log.info("Integer {}", s));

        log.info("******************************");
        StepVerifier.create(fluxInteger)
                .expectNext(1,2,3,4,5,6,7)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersError(){
        Flux<Integer> fluxInteger = Flux.range(1,7)
                .log()
                .map(i -> {
                            if(i ==5){
                                throw new IndexOutOfBoundsException("Index Error!");
                            }
                            return i;
                        });
        fluxInteger.subscribe(s -> log.info("Integer {}", s),
                Throwable::printStackTrace,
                () -> log.info("DONE!"), subscription -> subscription.request(4)
                );

        log.info("******************************");
        StepVerifier.create(fluxInteger)
                .expectNext(1,2,3,4)
                .expectError(IndexOutOfBoundsException.class)
                .verify();
    }

    @Test
    public void fluxSubscriberNumbersUglyBackpressure(){
        Flux<Integer> fluxInteger = Flux.range(1,10)
                .log();
        fluxInteger.subscribe(new Subscriber<>() {
            private int count = 0;
            private Subscription subscription;
            private final int requestCount = 0;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(2);
            }

            @Override
            public void onNext(Integer integer) {
                count++;
                if(count >= requestCount){
                    count = 0;
                    subscription.request(2);
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });

        log.info("******************************");
        StepVerifier.create(fluxInteger)
                .expectNext(1,2,3,4,5,6,7,8,9,10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersNotSoUglyBackpressure(){
        Flux<Integer> fluxInteger = Flux.range(1,10)
                .log();
        fluxInteger.subscribe(new BaseSubscriber<Integer>() {
            private int count = 0;
            private final int requestCount = 2;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(requestCount);
            }

            @Override
            protected void hookOnNext(Integer value) {
                count++;
                if(count >= requestCount){
                    count = 0;
                    request(requestCount);
                }
            }
        });

        log.info("******************************");
        StepVerifier.create(fluxInteger)
                .expectNext(1,2,3,4,5,6,7,8,9,10)
                .verifyComplete();
    }
}
