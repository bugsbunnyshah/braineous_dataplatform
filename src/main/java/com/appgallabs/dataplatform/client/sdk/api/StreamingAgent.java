package com.appgallabs.dataplatform.client.sdk.api;

import com.appgallabs.dataplatform.client.sdk.infrastructure.ListenableQueue;
import com.appgallabs.dataplatform.client.sdk.network.DataPipelineClient;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ehcache.sizeof.SizeOf;

import java.util.LinkedList;

public class StreamingAgent {
    private static StreamingAgent singleton = new StreamingAgent();

    private DataPipelineClient dataPipelineClient;

    private ListenableQueue<String> queueStream;

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

    public synchronized void sendData(String json){
        // register a listener which polls a queue and prints an element
        this.queueStream.registerListener(e -> {
            SizeOf sizeOf = SizeOf.newInstance();
            long dataStreamSize = sizeOf.deepSizeOf(this.queueStream);

            //TODO: make this configurable, depending on ingestion payload size
            int windowSize = 100;
                if (dataStreamSize >= windowSize) {
                    JsonArray batch = new JsonArray();
                    for (int i = 0; i < this.queueStream.size(); i++) {
                        String element = this.queueStream.remove();
                        JsonElement batchElement = JsonUtil.validateJson(element);
                        if(batchElement == null){
                            //TODO: integrate with reporting service
                            continue;
                        }

                        batch.add(batchElement);
                    }

                    //send batch to cloud
                    if(batch.size() > 0) {
                        JsonUtil.printStdOut(batch);
                        String batchJsonString = batch.toString();
                        JsonObject response = sendDataToCloud(batchJsonString);

                        //TODO: integrate with reporting service
                        JsonUtil.printStdOut(response);
                    }
                }
        });

        this.queueStream.add(json);
    }

    private JsonObject sendDataToCloud(String payload){
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

        return response;
    }
}
