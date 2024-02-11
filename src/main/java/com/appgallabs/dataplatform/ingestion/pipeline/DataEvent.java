package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;

public class DataEvent implements Serializable {
    private String json;
    private String fieldName;
    private Object fieldValue;

    private boolean isProcessed;

    private String entity;

    public DataEvent() {}

    public DataEvent(String entity,String json, String fieldName, Object fieldValue) {
        this.entity = entity;
        this.json = json;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    /*public String toString() {
        JsonElement jsonElement = JsonParser.parseString(this.json);
        try {
            if (jsonElement.isJsonArray()) {
                return JsonUtil.getJsonHash(jsonElement.getAsJsonArray());
            }
            return JsonUtil.getJsonHash(jsonElement.getAsJsonObject());
        }catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }*/

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public void setProcessed(boolean processed) {
        isProcessed = processed;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public JsonObject toJson(){
        Gson gson = JsonUtil.getGson();
        JsonElement jsonElement = gson.toJsonTree(this);
        return jsonElement.getAsJsonObject();
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }
}
