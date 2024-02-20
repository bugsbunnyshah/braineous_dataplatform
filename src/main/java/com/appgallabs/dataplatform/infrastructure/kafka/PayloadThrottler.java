package com.appgallabs.dataplatform.infrastructure.kafka;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PayloadThrottler {
    private static Logger logger = LoggerFactory.getLogger(PayloadThrottler.class);

    public List<JsonArray> throttle(JsonArray original){
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
