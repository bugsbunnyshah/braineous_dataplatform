package com.appgallabs.dataplatform.pipeline.manager.model;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;

public class Subscription implements Serializable {

    private String subscriptionId;

    private SubscriberGroup group;
    private Pipe pipe;

    public Subscription() {
    }

    public Subscription(String subscriptionId, SubscriberGroup group, Pipe pipe) {
        this.subscriptionId = subscriptionId;
        this.group = group;
        this.pipe = pipe;
    }

    public SubscriberGroup getGroup() {
        return group;
    }

    public void setGroup(SubscriberGroup group) {
        this.group = group;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public Pipe getPipe() {
        return pipe;
    }

    public JsonObject toJson(){
        Gson gson = JsonUtil.getGson();
        JsonElement jsonElement = gson.toJsonTree(this);
        return jsonElement.getAsJsonObject();
    }

    public static Subscription parse(String jsonString){
        Gson gson = JsonUtil.getGson();

        Subscription subscription = gson.fromJson(jsonString,Subscription.class);

        return subscription;
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }
}
