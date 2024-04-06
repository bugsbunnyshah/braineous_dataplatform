package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;

public class Record implements Serializable {
    private RecordMetaData recordMetaData;
    private JsonObject data;

    public Record() {
        this.data = new JsonObject();
        this.recordMetaData = new RecordMetaData();
    }

    public RecordMetaData getRecordMetaData() {
        return recordMetaData;
    }

    public void setRecordMetaData(RecordMetaData recordMetaData) {
        this.recordMetaData = recordMetaData;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }

    public JsonObject toJson(){
        Gson gson = JsonUtil.getGson();
        JsonElement jsonElement = gson.toJsonTree(this);
        return jsonElement.getAsJsonObject();
    }
}
