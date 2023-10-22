package com.appgallabs.dataplatform.client.sdk.api;

import com.appgallabs.dataplatform.client.sdk.infrastructure.ListenableQueue;
import com.appgallabs.dataplatform.client.sdk.network.DataPipelineClient;
import com.appgallabs.dataplatform.client.sdk.service.DataPipelineService;
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

    public void sendData(String json){
        // register a listener which polls a queue and prints an element
        this.queueStream.registerListener(e -> {
            SizeOf sizeOf = SizeOf.newInstance();
            long dataStreamSize = sizeOf.deepSizeOf(this.queueStream);

            //TODO: make this configurable, depending on
            //ingestion payload size
            int windowSize = 1024;
            if(dataStreamSize >= windowSize){
                for(int i=0; i<this.queueStream.size();i++) {
                    String element = this.queueStream.remove();

                    //TODO: send to DataPipelineClient
                    System.out.println(i);
                    System.out.println(element);
                    System.out.println("*****************");
                }
            }
        });

        this.queueStream.add(json);
    }
}
