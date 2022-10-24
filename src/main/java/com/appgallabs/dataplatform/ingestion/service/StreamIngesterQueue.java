package com.appgallabs.dataplatform.ingestion.service;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.vertx.core.impl.ConcurrentHashSet;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StreamIngesterQueue implements Serializable {
    private Map<String,Queue<StreamObject>> queue;
    private Set<String> activeQueues;

    public StreamIngesterQueue(){
        this.queue = new ConcurrentHashMap<>();
        this.activeQueues = new ConcurrentHashSet<>();
    }

    public void clear(){
        this.queue.clear();
        this.activeQueues.clear();
    }

    public void add(StreamObject streamObject)
    {
        System.out.println("ADDING TO QUEUE......................................................123");
        JsonUtil.printStdOut(streamObject.toJson());

        String dataLakeId = streamObject.getDataLakeId();

        Queue<StreamObject> objectQueue = this.queue.get(dataLakeId);
        if(objectQueue == null){
            this.queue.put(dataLakeId, new LinkedList<>());
        }

        objectQueue = this.queue.get(dataLakeId);
        JsonArray jsonArray = JsonParser.parseString(streamObject.getData()).getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();
        while (iterator.hasNext()) {
            JsonObject jsonObject = iterator.next().getAsJsonObject();
            StreamObject cour = new StreamObject();
            String principal = streamObject.getPrincipal();
            String entity = streamObject.getEntity();
            int batchSize = streamObject.getBatchSize();
            String chainId = streamObject.getChainId();
            String data = streamObject.getData();

            cour.setPrincipal(streamObject.getPrincipal());
            cour.setBatchSize(streamObject.getBatchSize());
            cour.setDataLakeId(streamObject.getDataLakeId());
            cour.setChainId(streamObject.getChainId());
            cour.setEntity(streamObject.getEntity());
            cour.setData(jsonObject.toString());
            objectQueue.add(cour);

            StreamIngesterContext.getStreamIngesterContext().
                    ingestOnThread(principal,entity,dataLakeId,chainId,batchSize,jsonObject);
        }
        if(!objectQueue.isEmpty()) {
            this.activeQueues.add(dataLakeId);
        }
    }

    public Queue<StreamObject> getDataLakeQueue(String dataLakeId){
        Queue<StreamObject> objectQueue = this.queue.get(dataLakeId);
        if(objectQueue == null){
            return new LinkedList<>();
        }

        return objectQueue;
    }


    public Set<String> getActiveDataLakeIds(){
        Set<String> result = new HashSet<>();
        for(String active:this.activeQueues){
            result.add(active);
        }
        return result;
    }
}
