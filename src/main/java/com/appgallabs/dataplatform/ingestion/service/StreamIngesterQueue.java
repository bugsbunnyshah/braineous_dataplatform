package com.appgallabs.dataplatform.ingestion.service;

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
        String dataLakeId = streamObject.getDataLakeId();

        //System.out.println("********ACTIVE_DATA_LAKE_ID********");
        //System.out.println(dataLakeId);
        //System.out.println("***********************************");

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
            cour.setPrincipal(streamObject.getPrincipal());
            cour.setDataLakeId(streamObject.getDataLakeId());
            cour.setChainId(streamObject.getChainId());
            cour.setData(jsonObject.toString());
            objectQueue.add(cour);
        }
        if(!objectQueue.isEmpty()) {
            this.activeQueues.add(dataLakeId);
        }

        /*System.out.println("*************ADD*********************");
        System.out.println("DataLakeId: "+dataLakeId);
        System.out.println(this.queue.get(dataLakeId));
        System.out.println("**********************************");*/
    }

    public Queue<StreamObject> getDataLakeQueue(String dataLakeId){
        Queue<StreamObject> objectQueue = this.queue.get(dataLakeId);
        if(objectQueue == null){
            return new LinkedList<>();
        }

        /*System.out.println("*************GET_QUEUE*********************");
        System.out.println("DataLakeId: "+dataLakeId);
        System.out.println(objectQueue);
        System.out.println(this.getActiveDataLakeIds());
        System.out.println("**********************************");*/

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
