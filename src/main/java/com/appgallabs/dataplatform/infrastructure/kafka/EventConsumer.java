package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.ingestion.pipeline.PipelineService;
import com.appgallabs.dataplatform.receiver.framework.StoreOrchestrator;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class EventConsumer {
    private static Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Inject
    private PipelineService pipelineService;

    private Set<String> registeredPipes;


    public EventConsumer() {
        this.registeredPipes = new HashSet<>();
    }

    @PostConstruct
    public void start(){
    }

    @PreDestroy
    public void stop(){
        try {
            SimpleConsumer.getInstance().shutdown();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public JsonObject checkStatus(){
        JsonObject response = new JsonObject();
        response.addProperty("status", "LISTENING...");
        return response;
    }

    public void registerPipe(String pipeId){
        try {
            if(!this.registeredPipes.contains(pipeId)) {
                String pipeTopic = pipeId;

                SimpleConsumer consumer = SimpleConsumer.getInstance();
                consumer.runAlways(pipeTopic, new EventHandler(this.pipelineService,
                        StoreOrchestrator.getInstance()));

                this.registeredPipes.add(pipeId);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
