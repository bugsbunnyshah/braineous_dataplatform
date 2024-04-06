package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

@QuarkusTest
public class PayloadThrottlerTests {
    private static Logger logger = LoggerFactory.getLogger(PayloadThrottlerTests.class);

    @Inject
    private PayloadThrottler payloadThrottler;

    @Test
    public void throttle() throws Exception {
        String datasetLocation = "infrastructure/kafka/flight.json";
        String json = Util.loadResource(datasetLocation);
        JsonObject flightJson = JsonUtil.validateJson(json).getAsJsonObject();
        JsonArray datasetElement = new JsonArray();
        for(int i=0; i<100; i++){
            datasetElement.add(flightJson);
        }

        List<JsonArray> throttled = this.payloadThrottler.throttle(datasetElement);
        System.out.println("TOTAL_BUCKETS:"+ throttled.size());
        for(JsonArray cour: throttled) {
            System.out.println("*************");
            System.out.println("BUCKET_SIZE: "+cour.size());
        }
    }
}
