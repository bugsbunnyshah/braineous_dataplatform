package com.appgallabs.dataplatform.infrastructure.kafka;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;

import javax.inject.Inject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

        for(int i=0; i<1; i++) {
            JsonObject response = this.eventProcessor.processEvent(json);

            logger.info("*****************");
            logger.info(response.toString());
            logger.info("*****************");

            assertNotNull(response);
        }

        Thread.sleep(3000);
    }

    @Test
    public void processEventWithPipeline() throws Exception {
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/input_array.json"),
                StandardCharsets.UTF_8
        );
        JsonArray jsonArray = JsonParser.parseString(jsonString).getAsJsonArray();

        for(int i=0; i<1; i++) {
            JsonObject response = this.eventProcessor.processEvent(jsonArray);

            logger.info("*****************");
            logger.info(response.toString());
            logger.info("*****************");

            assertNotNull(response);
        }

        Thread.sleep(3000);
    }
}
