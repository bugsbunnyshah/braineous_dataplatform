package com.appgallabs.dataplatform.infrastructure.kafka;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.kafka.clients.admin.TopicListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

@Singleton
public class EventConsumer {
    private static Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    private final String fixedTopicName = "braineous_dataplatform_kafka_topic";
    private SimpleConsumer consumer;

    @PostConstruct
    public void start(){
        try {
            this.consumer = SimpleConsumer.getInstance();
            this.consumer.runAlways(fixedTopicName, new KafkaMessageHandlerImpl());
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void stop(){
        try {
            this.consumer.close();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public JsonObject checkStatus(){
        JsonObject response = new JsonObject();
        response.addProperty("status", "LISTENING");
        return response;
    }
}
