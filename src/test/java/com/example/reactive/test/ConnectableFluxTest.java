package com.example.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

@Slf4j
public class ConnectableFluxTest {

    @BeforeAll
    public static void setUp(){
        BlockHound.install();
    }

    @Test
    public void ConnectableFlux() throws Exception{
        ConnectableFlux<Integer> connectableFlux = Flux.range(1,10)
                .log()
                .delayElements(Duration.ofMillis(100))
                .publish();
//        connectableFlux.connect();
//
//        log.info("Thread sleeping for 300ms");
//        Thread.sleep(300);
//        connectableFlux.subscribe(i -> log.info("Sub1 Number {}", i));
//
//        log.info("Thread Sleeping for 200ms");
//        Thread.sleep(200);
//        connectableFlux.subscribe(i -> log.info("Sub2 Number {}", i));

        StepVerifier.create(connectableFlux)
                .then(connectableFlux::connect)
                .thenConsumeWhile(i -> i <= 5)
                .expectNext(6,7,8,9,10)
                .expectComplete()
                .verify();
    }

    @Test
    public void ConnectableFluxAutoConnect() throws Exception{
        Flux<Integer> fluxAutoConnect = Flux.range(1,5)
                .log()
                .delayElements(Duration.ofMillis(100))
                .publish()
                .autoConnect(2);

        StepVerifier.create(fluxAutoConnect)
                .then(fluxAutoConnect::subscribe)
                .expectNext(1,2,3,4,5)
                .expectComplete()
                .verify();
    }
}
