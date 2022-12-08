package com.example.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple3;

@Slf4j
public class ZipOperatorTest {

    @BeforeAll
    public static void setUp(){
        BlockHound.install();
    }

    @Test
    public void zipOperator(){
        Flux<String> titleflux = Flux.just("Grand Blue", "Royal Blue");
        Flux<String> studioflux = Flux.just("Fox-World", "Z-Studio");
        Flux<Integer> episodesflux = Flux.just(15, 30);

        Flux<Tuple3<String, String, Integer>> zipFlux = Flux.zip(titleflux, studioflux, episodesflux);

        Flux<Anime> animeFlux = zipFlux
                .flatMap(tuple -> Flux
                        .just(new Anime(tuple.getT1(), tuple.getT2(), tuple.getT3())));

//        animeFlux.subscribe(anime -> log.info(anime.toString()));

        StepVerifier
                .create(animeFlux)
                .expectSubscription()
                .expectNext(
                        new Anime("Grand Blue", "Fox-World", 15),
                        new Anime("Royal Blue", "Z-Studio", 30)
                )
                .verifyComplete();

    }

    @Test
    public void zipWithOperator(){
        Flux<String> titleflux = Flux.just("Grand Blue", "Royal Blue");
//        Flux<String> studioflux = Flux.just("Fox-World", "Z-Studio");
        Flux<Integer> episodesflux = Flux.just(15, 30);

        Flux<Anime> animeFlux = titleflux.zipWith(episodesflux)
                .flatMap(tuple -> Flux
                        .just(new Anime(tuple.getT1(), null, tuple.getT2())));

        animeFlux.subscribe(anime -> log.info(anime.toString()));

        StepVerifier
                .create(animeFlux)
                .expectSubscription()
                .expectNext(
                        new Anime("Grand Blue", null, 15),
                        new Anime("Royal Blue", null, 30)
                )
                .verifyComplete();

    }

}
