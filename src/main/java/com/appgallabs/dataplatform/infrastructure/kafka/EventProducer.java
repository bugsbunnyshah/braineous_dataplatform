package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.kafka.clients.admin.TopicListing;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class EventProducer {
    private static Logger logger = LoggerFactory.getLogger(EventProducer.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private KafkaSession kafkaSession;


    public EventProducer() {

    }

    @PostConstruct
    public void start(){
        this.kafkaSession.setEventProducer(this);
    }

    @PreDestroy
    public void stop(){
        try {
            SimpleProducer.getInstance().shutdown();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public JsonObject checkStatus(){
        JsonObject response = new JsonObject();
        response.addProperty("status", "LISTENING...");
        return response;
    }

    public JsonObject processEvent(String pipeId, String entity, JsonElement json) {
        try{
            JsonObject response = new JsonObject();

            SimpleProducer.getInstance().publishToBroker(this.securityTokenContainer,
                    pipeId, entity, json.toString());
            response.addProperty("statusCode", 200);

            return response;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
