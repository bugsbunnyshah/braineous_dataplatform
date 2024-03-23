package com.appgallabs.dataplatform.client.sdk.service;

import com.appgallabs.dataplatform.client.sdk.api.Configuration;
import com.appgallabs.dataplatform.client.sdk.api.GraphQlQueryException;
import com.appgallabs.dataplatform.client.sdk.api.RegisterPipeException;
import com.appgallabs.dataplatform.client.sdk.infrastructure.StreamingAgent;
import com.appgallabs.dataplatform.client.sdk.network.DataPipelineClient;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
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
        StreamingAgent.getInstance().sendData(configuration, pipeId, entity, payload);
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
}
