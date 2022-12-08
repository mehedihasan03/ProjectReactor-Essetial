package com.example.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BlockHoundTest {

    @BeforeAll
    public static void setUp(){
        BlockHound.install(
                builder -> builder
                        .allowBlockingCallsInside("org.slf4j.impl.SimpleLogger", "write")
        );
    }

    @Test
    public void BlockHoundWorks(){
        try {
        FutureTask<?> task = new FutureTask<>(() -> {
            Thread.sleep(0);
            return "";
        });
        Schedulers.parallel().schedule(task);

            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");

        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }

    }
}
