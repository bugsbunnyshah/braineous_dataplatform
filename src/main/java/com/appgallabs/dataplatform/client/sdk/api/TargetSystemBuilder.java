package com.appgallabs.dataplatform.client.sdk.api;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class TargetSystemBuilder {
    private String pipeId;
    private String pipeName;

    private JsonObject pipeConfig = new JsonObject();
    private JsonArray configuration = new JsonArray();

    private JsonObject currentConfiguration = new JsonObject();

    public TargetSystemBuilder() {
        pipeConfig.add("configuration", configuration);
    }

    public String getPipeId() {
        return pipeId;
    }

    public void setPipeId(String pipeId) {
        this.pipeId = pipeId;
        this.pipeConfig.addProperty("pipeId", pipeId);
    }

    public String getPipeName() {
        return pipeName;
    }

    public void setPipeName(String pipeName) {
        this.pipeName = pipeName;
    }

    public TargetSystemBuilder setStoreDriver(String value){
        this.currentConfiguration.addProperty("storeDriver", value);
        return this;
    }

    public TargetSystemBuilder setStoreName(String value){
        this.currentConfiguration.addProperty("name", value);
        return this;
    }

    public TargetSystemBuilder setJsonPathExpression(String value){
        this.currentConfiguration.addProperty("jsonPathExpression", value);
        return this;
    }

    public TargetSystemBuilder setConfig(JsonObject value){
        this.currentConfiguration.add("config", value);
        return this;
    }

    public TargetSystemBuilder addTargetSystem(){
        this.configuration.add(this.currentConfiguration);
        this.currentConfiguration = new JsonObject();
        return this;
    }

    public JsonObject build(){
        this.addTargetSystem();

        String jsonString = this.toString();

        JsonObject jsonObject = JsonUtil.validateJson(jsonString).getAsJsonObject();

        return jsonObject;
    }

    public JsonObject toJson(){
        String jsonString = this.toString();
        return JsonUtil.validateJson(jsonString).getAsJsonObject();
    }

    @Override
    public String toString() {
        Gson gson = JsonUtil.getGson();
        String result = gson.toJson(this.pipeConfig);
        return result;
    }
}
