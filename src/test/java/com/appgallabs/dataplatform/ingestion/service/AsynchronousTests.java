package com.appgallabs.dataplatform.ingestion.service;

import io.quarkus.test.junit.QuarkusTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@QuarkusTest
public class AsynchronousTests {
    private static Logger logger = LoggerFactory.getLogger(AsynchronousTests.class);

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    //@Test
    public void testWaitOnOperation() throws Exception {
        Future<Integer> future =  executor.submit(() -> {
            Integer input = 5;
            Thread.sleep(10000);
            return input * input;
        });

        Integer result = future.get();

        logger.info("Result: "+result);
    }
}
