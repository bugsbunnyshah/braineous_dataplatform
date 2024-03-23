package com.appgallabs.dataplatform.client.sdk.api;

import com.appgallabs.dataplatform.client.sdk.service.DataPipelineService;
import com.google.gson.JsonObject;

import java.util.UUID;

public class DataPipeline {

    public static void sendData(Configuration configuration, String pipeId, String entity, String payload){
        System.out.println("***SENDING_DATA_START*****");
        DataPipelineService.getInstance().sendData(configuration, pipeId, entity, payload);
    }

    public static JsonObject registerPipe(Configuration configuration, String payload) throws RegisterPipeException{
        JsonObject result = DataPipelineService.getInstance().registerPipe(configuration, payload);
        if(result != null){
            return result;
        }

        //throw exception
        throw new RegisterPipeException("unknown_query_error");
    }
}
