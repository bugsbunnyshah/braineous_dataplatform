package com.appgallabs.dataplatform.client.sdk.infrastructure;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class PayloadThrottler {

    public static List<JsonArray> generatePayload(JsonElement originalPayload){
        JsonArray original = generationIngestionArray(originalPayload);
        List<JsonArray> payload = throttle(original);
        return payload;
    }

    private static JsonArray generationIngestionArray(JsonElement jsonElement){
        JsonArray ingestion = new JsonArray();
        if (jsonElement.isJsonArray()) {
            ingestion = jsonElement.getAsJsonArray();
        } else if (jsonElement.isJsonObject()) {
            ingestion.add(jsonElement);
        }
        return ingestion;
    }

    private static List<JsonArray> throttle(JsonArray original){
        List<JsonArray> throttled = new ArrayList<>();

        //
        int originalSize = original.size();
        JsonArray activeArray = new JsonArray();
        int packetSize = 10;
        for(int i=0; i<originalSize; i++){
            JsonObject currentObject = original.get(i).getAsJsonObject();
            activeArray.add(currentObject);

            if(activeArray.size() == packetSize){
                throttled.add(activeArray);
                activeArray = new JsonArray();
            }
        }

        if(activeArray.size() > 0){
            throttled.add(activeArray);
        }

        return throttled;
    }
}
