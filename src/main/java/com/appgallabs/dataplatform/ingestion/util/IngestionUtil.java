package com.appgallabs.dataplatform.ingestion.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class IngestionUtil {

    public static JsonArray generationIngestionArray(JsonElement jsonElement){
        JsonArray ingestion = new JsonArray();
        if (jsonElement.isJsonArray()) {
            ingestion = jsonElement.getAsJsonArray();
        } else if (jsonElement.isJsonObject()) {
            ingestion.add(jsonElement);
        }
        return ingestion;
    }
}
