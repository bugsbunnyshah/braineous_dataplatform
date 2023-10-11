package com.appgallabs.dataplatform.pipeline.manager.model;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SubscriberGroup implements Serializable {

    private List<Subscriber> subscribers;

    public SubscriberGroup() {
        this.subscribers = new ArrayList<>();
    }

    public SubscriberGroup(List<Subscriber> subscribers) {
        if(subscribers != null && !subscribers.isEmpty()) {
            this.subscribers = subscribers;
        }else{
            this.subscribers = new ArrayList<>();
        }
    }

    public List<Subscriber> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(List<Subscriber> subscribers) {
        this.subscribers = subscribers;
    }

    public void addSubscriber(Subscriber subscriber){
        if(subscriber != null) {
            this.subscribers.add(subscriber);
        }
    }

    public void removeSubscriber(Subscriber subscriber){
        if(subscriber != null){
            this.subscribers.remove(subscriber);
        }
    }

    public void clearSubscribers(){
        this.subscribers.clear();
    }

    public JsonObject toJson(){
        Gson gson = JsonUtil.getGson();
        JsonElement jsonElement = gson.toJsonTree(this);
        return jsonElement.getAsJsonObject();
    }
}
