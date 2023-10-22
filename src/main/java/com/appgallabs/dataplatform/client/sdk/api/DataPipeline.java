package com.appgallabs.dataplatform.client.sdk.api;

import com.appgallabs.dataplatform.client.sdk.service.DataPipelineService;

public class DataPipeline {

    public static void sendData(String payload){
        DataPipelineService.getInstance().sendData(payload);
    }
}
