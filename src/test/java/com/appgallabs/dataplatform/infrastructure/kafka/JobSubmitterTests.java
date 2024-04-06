package com.appgallabs.dataplatform.infrastructure.kafka;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@QuarkusTest
public class JobSubmitterTests {
    private static Logger logger = LoggerFactory.getLogger(JobSubmitterTests.class);

    @Test
    public void submit() throws Exception{
        ExecutorService threadpool = Executors.newCachedThreadPool();
        threadpool.execute(() -> {
            logger.info("****TASK SUBMITTED*****");
        });
        Thread.sleep(1000);
        logger.info("***SUBMITTER_DONE*****");
        threadpool.shutdown();
    }


}
