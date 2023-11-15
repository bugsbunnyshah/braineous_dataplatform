package com.appgallabs.dataplatform.client.sdk.api;

import com.appgallabs.dataplatform.client.sdk.service.DataLakeGraphQlQueryService;
import com.appgallabs.dataplatform.client.sdk.service.DataPipelineService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class DataPipeline {
    private static Configuration configuration;

    /*static{
        configuration = new HashMap<>();

        configuration.put("steamSizeInBytes", "80");
        configuration.put("ingestionHostBaseUrl", "http://localhost:8080/");
    }*/

    public static Configuration configure(Configuration configuration){
        DataPipeline.configuration = configuration;
        return DataPipeline.configuration;
    }

    public static Configuration getConfiguration() {
        return DataPipeline.configuration;
    }

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
