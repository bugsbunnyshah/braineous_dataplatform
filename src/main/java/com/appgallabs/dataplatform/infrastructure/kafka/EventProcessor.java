package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.pipeline.Registry;
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

    public EventProcessor() {
        this.topicListing = new HashMap<>();
    }

    @PostConstruct
    public void start(){
        try {
            //start all pipes which are kafka topics
            Set<String> allPipeIds = Registry.getInstance().allRegisteredPipeIds();

            for(String pipeTopic: allPipeIds) {
                TopicListing topicListing = KafkaTopicHelper.createFixedTopic(pipeTopic);
                this.topicListing.put(pipeTopic, topicListing);
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void stop(){
        try {
            SimpleProducer.getInstance().shutdown();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public JsonObject processEvent(JsonElement json) {
        try{
            JsonObject response = new JsonObject();

            SimpleProducer.getInstance().publishToBroker(this.securityTokenContainer,
                    "blah", json.toString());
            response.addProperty("statusCode", 200);


            return response;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public JsonObject processEvent(String pipeId,JsonElement json) {
        try{
            JsonObject response = new JsonObject();

            System.out.println("****EVENT_PROCESSOR_DEBUG**********");
            System.out.println("TOPIC: "+pipeId);
            JsonUtil.printStdOut(json);


            SimpleProducer.getInstance().publishToBroker(this.securityTokenContainer,
                    pipeId, json.toString());
            response.addProperty("statusCode", 200);


            return response;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
