package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.security.NoSuchAlgorithmException;

public class SinkEvent {
    private String json;

    public SinkEvent() {}

    public SinkEvent(String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String toString() {
        JsonElement jsonElement = JsonParser.parseString(this.json);
        try {
            if (jsonElement.isJsonArray()) {
                return JsonUtil.getJsonHash(jsonElement.getAsJsonArray());
            }
            return JsonUtil.getJsonHash(jsonElement.getAsJsonObject());
        }catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
