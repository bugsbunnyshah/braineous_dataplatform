package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.receiver.framework.Registry;
import com.appgallabs.dataplatform.util.JsonUtil;
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
import java.util.Set;

@Singleton
public class EventProcessor {
    private static Logger logger = LoggerFactory.getLogger(EventProcessor.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    private Map<String, TopicListing> topicListing;

    private Map<String, SimpleProducer> producer;

    public EventProcessor() {
        this.topicListing = new HashMap<>();
        this.producer = new HashMap<>();
    }

    @PostConstruct
    public void start(){
        try {
            //TODO: (CR1)
            Registry registry = Registry.getInstance();
            JsonUtil.printStdOut(JsonUtil.validateJson(registry.allRegisteredPipeIds().toString()));

            //start all pipes which are kafka topics
            Set<String> allPipeIds = Registry.getInstance().allRegisteredPipeIds();

            for(String pipeTopic: allPipeIds) {
                TopicListing topicListing = KafkaTopicHelper.createFixedTopic(pipeTopic);
                SimpleProducer producer = SimpleProducer.getInstance();

                this.topicListing.put(pipeTopic, topicListing);
                this.producer.put(pipeTopic, producer);
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void stop(){
        try {
            for(SimpleProducer producer: this.producer.values()) {
                producer.shutdown();
            }
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
            //TODO: (CR1)
            SimpleProducer producer = this.producer.values().iterator().next();
            String pipeTopic = this.producer.keySet().iterator().next();

            producer.publishToBroker(this.securityTokenContainer,
                    pipeTopic, json.toString());
            response.addProperty("statusCode", 200);


            return response;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
