package com.appgallabs.dataplatform.infrastructure.kafka;

import com.google.gson.JsonObject;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;


@QuarkusTest
public class EventProcessorTests {
    private static Logger logger = LoggerFactory.getLogger(EventProcessorTests.class);

    @Inject
    private EventProcessor eventProcessor;

    @Inject
    private EventConsumer eventConsumer;

    @BeforeTest
    public void setUp(){
        JsonObject response = this.eventConsumer.checkStatus();
        logger.info(response.toString());
    }

    @Test
    public void processEvent() throws InterruptedException {
        JsonObject json = new JsonObject();
        json.addProperty("ingestion","braineous_data_platform");

        for(int i=0; i<10; i++) {
            JsonObject response = this.eventProcessor.processEvent(json);

            logger.info("*****************");
            logger.info(response.toString());
            logger.info("*****************");

            assertNotNull(response);
        }

        Thread.sleep(10000);
    }
}
