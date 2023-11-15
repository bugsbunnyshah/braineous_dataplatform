package com.appgallabs.dataplatform.client.sdk.service;

import com.appgallabs.dataplatform.TestConstants;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class DataPipelineServiceTests {
    private static Logger logger = LoggerFactory.getLogger(DataPipelineServiceTests.class);

    //TODO: solidify: CR2
    @Test
    public void sendData() throws Exception{
        DataPipelineService dataPipelineService = DataPipelineService.getInstance();

        String jsonResource = "ingestion/algorithm/input.json";
        //String jsonResource = "people.json";
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream(jsonResource),
                StandardCharsets.UTF_8
        );

        for(int i=0; i<10; i++) {
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
            json.addProperty("offset",i);

            String pipeId = "123";
            String entity = TestConstants.ENTITY;
            dataPipelineService.sendData(pipeId, entity,json.toString());
        }

        //assertions
        //System.out.println("********************************");
        //JsonUtil.printStdOut(response);
        //assertNotNull(response);
    }
}
