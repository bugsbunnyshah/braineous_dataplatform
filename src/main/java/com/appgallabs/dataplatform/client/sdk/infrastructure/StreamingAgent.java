package com.appgallabs.dataplatform.client.sdk.infrastructure;

import com.appgallabs.dataplatform.client.sdk.api.Configuration;
import com.appgallabs.dataplatform.client.sdk.network.DataPipelineClient;
import com.appgallabs.dataplatform.client.sdk.service.ReportingService;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StreamingAgent {
    private static StreamingAgent singleton = new StreamingAgent();

    private DataPipelineClient dataPipelineClient;

    private ListenableQueue<String> queueStream;

    private ExecutorService threadpool = Executors.newCachedThreadPool();

    private StreamingAgent(){
        this.dataPipelineClient = DataPipelineClient.getInstance();
        this.queueStream = new ListenableQueue<>(new LinkedList<>());
    }

    public static StreamingAgent getInstance(){
        //safe-check, cause why not
        if(StreamingAgent.singleton == null){
            StreamingAgent.singleton = new StreamingAgent();
        }
        return StreamingAgent.singleton;
    }

    public synchronized void sendData(Configuration configuration, String pipeId, String entity,String json){
        // register a listener which polls a queue and prints an element
        this.queueStream.registerListener(e -> {
            handleStreamEvent(configuration, pipeId,entity);
        });
        this.queueStream.add(json);
    }

    //-----------------------------------------------------------------------------------------------------
    private void handleStreamEvent(Configuration configuration, String pipeId, String entity){
        int windowSize = configuration.getStreamSizeInObjects();

        int dataStreamSize = this.queueStream.size();

        if (dataStreamSize >= windowSize) {
            JsonArray batch = new JsonArray();
            for (int i = 0; i < this.queueStream.size(); i++) {
                String element = this.queueStream.remove();
                JsonElement batchElement = JsonUtil.validateJson(element);
                if(batchElement == null){
                    //integrate with reporting service
                    JsonObject reportingError = new JsonObject();
                    ReportingService reportingService = ReportingService.getInstance();
                    reportingService.reportDataError(reportingError);
                    continue;
                }

                batch.add(batchElement);
            }

            //send batch to cloud
            if(batch.size() > 0) {
                String batchJsonString = batch.toString();
                JsonArray payloadArray = JsonUtil.validateJson(batchJsonString).getAsJsonArray();

                //array or object
                JsonElement payload = payloadArray.get(0);
                List<JsonArray> throttled = PayloadThrottler.generatePayload(payload);

                for(JsonArray throttle: throttled) {
                    String payloadString = throttle.toString();

                    this.threadpool.execute(() -> {
                        JsonObject response = sendDataToCloud(configuration, pipeId, entity, payloadString);

                        //integrate with reporting service
                        JsonObject reportingError = new JsonObject();
                        ReportingService reportingService = ReportingService.getInstance();
                        reportingService.reportDataError(reportingError);
                    });
                }
            }
        }
    }

    //-----------------------------------------------------------------------------------------------------
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
