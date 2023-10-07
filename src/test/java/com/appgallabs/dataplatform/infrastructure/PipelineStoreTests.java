package com.appgallabs.dataplatform.infrastructure;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

@QuarkusTest
public class PipelineStoreTests implements Serializable {
    private Logger logger = LoggerFactory.getLogger(PipelineStoreTests.class);

    @Test
    public void testStore() throws Exception{
        logger.info("*******************");
        logger.info("TEST_PIPELINE_STORE");
        logger.info("*******************");
    }
}
