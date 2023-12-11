package com.appgallabs.dataplatform.client.sdk.api;

import com.appgallabs.dataplatform.client.sdk.service.DataPipelineService;
import com.google.gson.JsonObject;

import java.util.UUID;

public class DataPipeline {
    private static Configuration configuration;

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

    public static TargetSystemBuilder createPipe(String pipeName) throws RegisterPipeException{
        TargetSystemBuilder targetSystemBuilder = new TargetSystemBuilder();
        String pipeId = UUID.randomUUID().toString();

        targetSystemBuilder.setPipeId(pipeId);
        targetSystemBuilder.setPipeName(pipeName);

        return targetSystemBuilder;
    }

    public static JsonObject registerPipe(TargetSystemBuilder targetSystemBuilder)
            throws RegisterPipeException{
        String jsonString = targetSystemBuilder.toString();
        return registerPipe(jsonString);
    }
}
