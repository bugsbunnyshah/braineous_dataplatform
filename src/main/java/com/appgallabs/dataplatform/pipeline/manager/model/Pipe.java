package com.appgallabs.dataplatform.pipeline.manager.model;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Pipe implements Serializable {
    private String pipeId;

    private String subscriptionId;

    private String pipeName;

    private PipeStage pipeStage;

    private PipeType pipeType;
    private List<DataCleanerFunction> cleanerFunctions;

    public Pipe() {
        this.pipeStage = PipeStage.DEVELOPMENT;
        this.pipeType = PipeType.PUSH;
    }

    public Pipe(String pipeId, String pipeName) {
        this.pipeId = pipeId;
        this.pipeName = pipeName;
        this.cleanerFunctions = new ArrayList<>();
        this.pipeStage = PipeStage.DEVELOPMENT;
        this.pipeType = PipeType.PUSH;
    }

    public Pipe(String pipeId, String pipeName, List<DataCleanerFunction> cleanerFunctions) {
        if(cleanerFunctions == null || cleanerFunctions.isEmpty()){
            cleanerFunctions = new ArrayList<>();
        }

        this.pipeId = pipeId;
        this.pipeName = pipeName;
        this.cleanerFunctions = cleanerFunctions;
        this.pipeStage = PipeStage.DEVELOPMENT;
        this.pipeType = PipeType.PUSH;
    }

    public String getPipeId() {
        return pipeId;
    }

    public void setPipeId(String pipeId) {
        this.pipeId = pipeId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getPipeName() {
        return pipeName;
    }

    public void setPipeName(String pipeName) {
        this.pipeName = pipeName;
    }

    public List<DataCleanerFunction> getCleanerFunctions() {
        return cleanerFunctions;
    }

    public void setCleanerFunctions(List<DataCleanerFunction> cleanerFunctions) {
        this.cleanerFunctions = cleanerFunctions;
    }

    public void addCleanerFunction(DataCleanerFunction cleanerFunction){
        if(cleanerFunction != null){
            this.cleanerFunctions.add(cleanerFunction);
        }
    }

    public void removeCleanerFunction(DataCleanerFunction cleanerFunction){
        if(cleanerFunction != null){
            this.cleanerFunctions.remove(cleanerFunction);
        }
    }

    public void clearCleanerFunctions(){
        this.cleanerFunctions.clear();
    }

    public PipeStage getPipeStage() {
        return pipeStage;
    }

    public void setPipeStage(PipeStage pipeStage) {
        this.pipeStage = pipeStage;
    }

    public PipeType getPipeType() {
        return pipeType;
    }

    public void setPipeType(PipeType pipeType) {
        this.pipeType = pipeType;
    }

    public JsonObject toJson(){
        Gson gson = JsonUtil.getGson();
        JsonElement jsonElement = gson.toJsonTree(this);
        return jsonElement.getAsJsonObject();
    }

    public static Pipe parse(String jsonString){
        Gson gson = JsonUtil.getGson();

        Pipe parsed = gson.fromJson(jsonString,Pipe.class);

        return parsed;
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }
}
