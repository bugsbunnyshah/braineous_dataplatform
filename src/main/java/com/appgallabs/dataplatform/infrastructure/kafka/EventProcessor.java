package com.appgallabs.dataplatform.infrastructure.kafka;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
public class EventProcessor {
    private static Logger logger = LoggerFactory.getLogger(EventProcessor.class);

    public JsonObject processEvent() {
        try{
            JsonObject response = new JsonObject();

            return response;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
