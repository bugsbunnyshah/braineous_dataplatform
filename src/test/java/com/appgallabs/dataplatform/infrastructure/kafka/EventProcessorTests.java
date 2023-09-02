package com.appgallabs.dataplatform.infrastructure.kafka;

import com.google.gson.JsonObject;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;


@QuarkusTest
public class EventProcessorTests {
    private static Logger logger = LoggerFactory.getLogger(EventProcessorTests.class);

    @Inject
    private EventProcessor eventProcessor;

    @Test
    public void processEvent() {
        JsonObject json = new JsonObject();
        json.addProperty("ingestion","braineous_data_platform");

        JsonObject response = this.eventProcessor.processEvent(json);

        logger.info("*****************");
        logger.info(response.toString());
        logger.info("*****************");

        assertNotNull(response);
    }
}
