package com.appgallabs.dataplatform.client.sdk.infrastructure;

import com.appgallabs.dataplatform.TempConstants;
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

    private void handleStreamEvent(String pipeId, String entity){
        SizeOf sizeOf = SizeOf.newInstance();
        long dataStreamSize = sizeOf.deepSizeOf(this.queueStream);

        //TODO: make this configurable, depending on ingestion payload size (CR2)
        int windowSize = 100;
        if (dataStreamSize >= windowSize) {
            JsonArray batch = new JsonArray();
            for (int i = 0; i < this.queueStream.size(); i++) {
                String element = this.queueStream.remove();
                JsonElement batchElement = JsonUtil.validateJson(element);
                if(batchElement == null){
                    //TODO: integrate with reporting service (CR2)
                    continue;
                }

                batch.add(batchElement);
            }

            //send batch to cloud
            if(batch.size() > 0) {
                String batchJsonString = batch.toString();
                JsonArray payloadArray = JsonUtil.validateJson(batchJsonString).getAsJsonArray();
                JsonElement payload = payloadArray.get(0);
                String payloadString = payload.toString();

                JsonObject response = sendDataToCloud(pipeId, entity, payloadString);

                //TODO: integrate with reporting service (CR2)
                //JsonUtil.printStdOut(response);
            }
        }
    }

    public synchronized void sendData(String pipeId, String entity,String json){
        // register a listener which polls a queue and prints an element
        this.queueStream.registerListener(e -> {
            handleStreamEvent(pipeId,entity);
        });
        this.queueStream.add(json);
    }

    private JsonObject sendDataToCloud(String pipeId, String entity,String payload){
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
