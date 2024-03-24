package com.appgallabs.dataplatform.client.sdk.api;

import com.appgallabs.dataplatform.client.sdk.service.DataPipelineService;
import com.google.gson.JsonObject;

import java.util.UUID;

class DataPipeline {

    public static void sendData(Configuration configuration, String pipeId, String entity, String payload){
        System.out.println("***SENDING_DATA_START*****");
        DataPipelineService.getInstance().sendData(configuration, pipeId, entity, payload);
    }

    public static JsonObject registerPipe(Configuration configuration, String pipeConf) throws RegisterPipeException{
        JsonObject result = DataPipelineService.getInstance().registerPipe(configuration, pipeConf);
        if(result != null){
            return result;
        }

        //throw exception
        throw new RegisterPipeException("unknown_query_error");
    }
}
