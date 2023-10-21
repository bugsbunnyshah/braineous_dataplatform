package com.appgallabs.dataplatform.client.sdk.service;

import com.appgallabs.dataplatform.client.sdk.network.DataPipelineClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DataPipelineService {
    private static DataPipelineService singleton = new DataPipelineService();

    private DataPipelineClient dataPipelineClient;

    private DataPipelineService(){
        this.dataPipelineClient = DataPipelineClient.getInstance();
    }

    public static DataPipelineService getInstance(){
        //safe-check, cause why not
        if(DataPipelineService.singleton == null){
            DataPipelineService.singleton = new DataPipelineService();
        }
        return DataPipelineService.singleton;
    }

    public JsonObject sendData(String payload){

        //TODO: produce rest payload
        JsonElement jsonElement = JsonParser.parseString(payload);


        //TODO: send data for ingestion
        JsonObject response = this.dataPipelineClient.sendData(jsonElement);


        //TODO: process response


        //TODO: provide response
        response.addProperty("statusCode",200);

        return response;
    }
}
