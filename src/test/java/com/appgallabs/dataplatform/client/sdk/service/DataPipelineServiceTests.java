package com.appgallabs.dataplatform.client.sdk.service;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class DataPipelineServiceTests {
    private static Logger logger = LoggerFactory.getLogger(DataPipelineServiceTests.class);

    @Test
    public void sendData() throws Exception{
        DataPipelineService dataPipelineService = DataPipelineService.getInstance();

        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/input.json"),
                StandardCharsets.UTF_8
        );

        JsonObject response = dataPipelineService.sendData(jsonString);

        //assertions
        System.out.println("********************************");
        JsonUtil.printStdOut(response);
        assertNotNull(response);
    }
}
