package com.appgallabs.dataplatform.client.sdk.service;

import com.appgallabs.dataplatform.client.sdk.api.StreamingAgent;
import com.appgallabs.dataplatform.client.sdk.network.DataPipelineClient;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

    public void sendData(String payload){

        StreamingAgent.getInstance().sendData(payload);

        /*
        //validate and prepare rest payload
        JsonElement jsonElement = JsonUtil.validateJson(payload);
        if(jsonElement == null){
            throw new RuntimeException("payload_not_in_json_format");
        }

        //send data for ingestion
        JsonObject response = this.dataPipelineClient.sendData(jsonElement);

        //process response
        String ingestionStatusMessage = null;
        if(response.has("ingestionError")){
            ingestionStatusMessage = response.get("ingestionError").getAsString();
        }else{
            ingestionStatusMessage = response.get("ingestionStatusCode").getAsString();
        }
        response.addProperty("status",ingestionStatusMessage);

        return response;*/
    }
}
