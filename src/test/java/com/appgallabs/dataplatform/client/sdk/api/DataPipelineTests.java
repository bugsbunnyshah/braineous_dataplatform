package com.appgallabs.dataplatform.client.sdk.api;

import com.appgallabs.dataplatform.TempConstants;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class DataPipelineTests {
    private static Logger logger = LoggerFactory.getLogger(DataPipelineTests.class);

    //TODO: solidify: (CR1)
    @Test
    public void sendData() throws Exception{
        String jsonResource = "ingestion/algorithm/input.json";
        //String jsonResource = "people.json";
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream(jsonResource),
                StandardCharsets.UTF_8
        );

        for(int i=0; i<1; i++) {
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
            json.addProperty("offset",i);

            String pipeId = "123";
            String entity = TempConstants.ENTITY;
            DataPipeline.sendData(pipeId, entity, json.toString());
        }

        //assertions
        //System.out.println("********************************");
        //JsonUtil.printStdOut(response);
        //assertNotNull(response);
    }
}
