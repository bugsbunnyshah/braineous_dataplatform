package com.appgallabs.dataplatform.client.sdk.service;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class DataPipelineServiceTests {
    private static Logger logger = LoggerFactory.getLogger(DataPipelineServiceTests.class);

    @Test
    public void sendData() throws Exception{
        DataPipelineService dataPipelineService = DataPipelineService.getInstance();

        JsonObject response = dataPipelineService.sendData();

        //assertions
        JsonUtil.printStdOut(response);
        assertNotNull(response);
    }

    @Test
    public void sendStream() throws Exception{
        DataPipelineService dataPipelineService = DataPipelineService.getInstance();

        JsonObject response = dataPipelineService.sendStream();

        //assertions
        JsonUtil.printStdOut(response);
        assertNotNull(response);
    }
}
