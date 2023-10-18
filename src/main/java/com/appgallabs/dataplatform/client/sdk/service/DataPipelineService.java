package com.appgallabs.dataplatform.client.sdk.service;

import com.google.gson.JsonObject;

public class DataPipelineService {
    private static DataPipelineService singleton = new DataPipelineService();

    private DataPipelineService(){

    }

    public static DataPipelineService getInstance(){
        //safe-check, cause why not
        if(DataPipelineService.singleton == null){
            DataPipelineService.singleton = new DataPipelineService();
        }
        return DataPipelineService.singleton;
    }

    public JsonObject sendData(){
        JsonObject response = new JsonObject();
        return response;
    }

    public JsonObject sendStream(){
        JsonObject response = new JsonObject();
        return response;
    }
}
