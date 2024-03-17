package com.appgallabs.dataplatform.client.sdk.infrastructure;

import com.appgallabs.dataplatform.client.sdk.api.Configuration;
import com.appgallabs.dataplatform.client.sdk.api.DataPipeline;
import com.appgallabs.dataplatform.client.sdk.network.DataPipelineClient;
import com.appgallabs.dataplatform.client.sdk.service.ReportingService;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ehcache.sizeof.SizeOf;

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

    private void handleStreamEvent(String pipeId, String entity){
        Configuration configuration = DataPipeline.getConfiguration();
        int windowSize = configuration.streamSizeInBytes();

        //System.out.println("***SENDING_DATA_HANDLE*****");
        SizeOf sizeOf = SizeOf.newInstance();
        long dataStreamSize = sizeOf.deepSizeOf(this.queueStream);

        //System.out.println("**********");
        //System.out.println(this.queueStream);
        //System.out.println("SIZE: "+dataStreamSize);
        //System.out.println("**********");

        if (dataStreamSize >= windowSize) {
            //System.out.println("***SENDING_DATA_HANDLED*****");
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
                        JsonObject response = sendDataToCloud(pipeId, entity, payloadString);

                        //integrate with reporting service
                        JsonObject reportingError = new JsonObject();
                        ReportingService reportingService = ReportingService.getInstance();
                        reportingService.reportDataError(reportingError);
                    });
                }
            }
        }else{
            //System.out.println("***SENDING_DATA_QUEUED*****");
        }
    }

    public synchronized void sendData(String pipeId, String entity,String json){
        //System.out.println("***SENDING_DATA_ASYNC*****");

        // register a listener which polls a queue and prints an element
        this.queueStream.registerListener(e -> {
            handleStreamEvent(pipeId,entity);
        });
        this.queueStream.add(json);
    }

    private JsonObject sendDataToCloud(String pipeId, String entity,String payload){
        //System.out.println("***SENDING_DATA_START_SEND_TO_CLOUD*****");

        //validate and prepare rest payload
        JsonElement jsonElement = JsonUtil.validateJson(payload);
        if(jsonElement == null){
            throw new RuntimeException("payload_not_in_json_format");
        }

        //send data for ingestion
        JsonObject response = this.dataPipelineClient.sendData(pipeId, entity,jsonElement);

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
