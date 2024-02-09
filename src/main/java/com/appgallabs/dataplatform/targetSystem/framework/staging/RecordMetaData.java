package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;

public class RecordMetaData implements Serializable {
    private JsonObject metadata;

    public RecordMetaData() {
    }

    public JsonObject getMetadata() {
        return metadata;
    }

    public void setMetadata(JsonObject metadata) {
        this.metadata = metadata;
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
