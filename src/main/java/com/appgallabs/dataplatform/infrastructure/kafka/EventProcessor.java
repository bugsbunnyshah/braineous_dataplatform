package com.appgallabs.dataplatform.infrastructure.kafka;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.apache.kafka.clients.admin.TopicListing;

@Singleton
public class EventProcessor {
    private static Logger logger = LoggerFactory.getLogger(EventProcessor.class);

    private final String braineousKafkaTopic = "braineous_dataplatform_kafka_topic";

    private TopicListing topicListing;

    private SimpleProducer producer;

    @PostConstruct
    public void start(){
        try {
            this.topicListing = KafkaTopicHelper.createFixedTopic(braineousKafkaTopic);
            this.producer = SimpleProducer.getInstance();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void stop(){
        try {
            this.producer.shutdown();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public JsonObject processEvent(JsonElement json) {
        try{
            JsonObject response = new JsonObject();

            /**
             * PRODUCE_MESSAGES_FROM_EVENT
             */
            this.producer.publishToBroker(braineousKafkaTopic, json.toString());
            response.addProperty("statusCode", 200);


            return response;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
