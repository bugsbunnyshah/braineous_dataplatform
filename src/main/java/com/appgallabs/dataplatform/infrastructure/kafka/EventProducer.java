package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.pipeline.Registry;

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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Singleton
public class EventProducer {
    private static Logger logger = LoggerFactory.getLogger(EventProducer.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    private Map<String, TopicListing> topicListing;

    private Set<String> allPipeIds;

    public EventProducer() {
        this.topicListing = new HashMap<>();
        this.allPipeIds = new HashSet<>();
    }

    @PostConstruct
    public void start(){
        try {
            //start all pipes which are kafka topics
            this.allPipeIds = Registry.getInstance().allRegisteredPipeIds();

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

    public void registerPipe(String pipeId){
        try {
            if(!this.allPipeIds.contains(pipeId)) {
                this.allPipeIds.add(pipeId);
                String pipeTopic = pipeId;

                TopicListing topicListing = KafkaTopicHelper.createFixedTopic(pipeTopic);
                this.topicListing.put(pipeTopic, topicListing);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
