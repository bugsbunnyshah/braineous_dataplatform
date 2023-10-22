package com.appgallabs.dataplatform.client.sdk.api;

import com.appgallabs.dataplatform.client.sdk.infrastructure.ListenableQueue;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ehcache.sizeof.SizeOf;

import java.util.LinkedList;

public class StreamingAgent {

    private ListenableQueue<String> queueStream;

    public StreamingAgent() {
        this.queueStream = new ListenableQueue<>(new LinkedList<>());
    }

    public void sendData(String json){
        // register a listener which polls a queue and prints an element
        this.queueStream.registerListener(e -> {
            SizeOf sizeOf = SizeOf.newInstance();
            long dataStreamSize = sizeOf.deepSizeOf(this.queueStream);

            //System.out.println("****DATA_STREAM_SIZE****");
            //System.out.println(q.size());
            //System.out.println(dataStreamSize+"");
            //System.out.println("************************");

            int windowSize = 400;
            if(dataStreamSize >= windowSize){
                for(int i=0; i<this.queueStream.size();i++) {
                    String element = this.queueStream.remove();
                    System.out.println(i);
                    System.out.println(element);
                    System.out.println("*****************");
                }
            }
        });

        this.queueStream.add(json);
    }
}
