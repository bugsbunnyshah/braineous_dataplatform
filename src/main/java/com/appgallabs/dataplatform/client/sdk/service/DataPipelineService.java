package com.appgallabs.dataplatform.client.sdk.service;

import com.appgallabs.dataplatform.client.sdk.api.Configuration;
import com.appgallabs.dataplatform.client.sdk.api.RegisterPipeException;
import com.appgallabs.dataplatform.client.sdk.infrastructure.StreamingAgent;
import com.appgallabs.dataplatform.client.sdk.network.DataPipelineClient;
import com.appgallabs.dataplatform.util.JsonUtil;
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

    public void sendData(Configuration configuration, String pipeId, String entity, String payload){
        //StreamingAgent.getInstance().sendData(configuration, pipeId, entity, payload);


        this.sendDataToCloud(configuration,
                pipeId,
                entity,
                payload);
    }

    public void print(Configuration configuration, String pipeId, String entity, String selectSql){
        this.dataPipelineClient.print(
                configuration,
                pipeId,
                entity,
                selectSql
        );
    }

    public JsonObject registerPipe(Configuration configuration, String payload) throws RegisterPipeException {
        //send query
        JsonObject response = this.dataPipelineClient.registerPipe(
                configuration,
                JsonUtil.validateJson(payload));

        //process response
        String queryStatusMessage = null;
        if(response.has("registerPipeError")){
            queryStatusMessage = response.get("registerPipeError").getAsString();
        }else{
            queryStatusMessage = response.get("registerPipeStatusCode").getAsString();
        }
        response.addProperty("status",queryStatusMessage);

        JsonObject result = new JsonObject();
        if(response.has("registerPipeResult") && queryStatusMessage.equals("200")){
            String queryResult = response.get("registerPipeResult").getAsString();
            result = JsonParser.parseString(queryResult).getAsJsonObject();
            return result;
        }

        throw new RegisterPipeException(response.toString());
    }
    //----------------------------
    private JsonObject sendDataToCloud(Configuration configuration, String pipeId, String entity,String payload){

        //validate and prepare rest payload
        JsonElement jsonElement = JsonUtil.validateJson(payload);
        if(jsonElement == null){
            throw new RuntimeException("payload_not_in_json_format");
        }

        //send data for ingestion
        JsonObject response = this.dataPipelineClient.sendData(configuration, pipeId, entity,jsonElement);

        //process response
        String ingestionStatusMessage = null;
        if(response.has("ingestionError")){
            ingestionStatusMessage = response.get("ingestionError").getAsString();
        }else{
            ingestionStatusMessage = response.get("ingestionStatusCode").getAsString();
        }
        response.addProperty("status",ingestionStatusMessage);

        return response;
    }
}
