package com.appgallabs.dataplatform.client.sdk.api;

import com.appgallabs.dataplatform.client.sdk.service.DataLakeGraphQlQueryService;
import com.appgallabs.dataplatform.client.sdk.service.DataPipelineService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DataPipeline {

    public static void sendData(String pipeId, String entity, String payload){
        System.out.println("***SENDING_DATA_START*****");
        DataPipelineService.getInstance().sendData(pipeId, entity, payload);
    }

    public static JsonObject registerPipe(String payload) throws RegisterPipeException{
        JsonObject result = DataPipelineService.getInstance().registerPipe(payload);
        if(result != null){
            return result;
        }

        //throw exception
        throw new RegisterPipeException("unknown_query_error");
    }
}
