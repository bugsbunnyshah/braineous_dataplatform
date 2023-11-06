package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.ingestion.pipeline.PipelineService;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.receiver.framework.StoreOrchestrator;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class EventConsumer {
    private static Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Inject
    private PipelineService pipelineService;

    public EventConsumer() {

    }

    @PostConstruct
    public void start(){
        try {
            //start all pipes which are kafka topics
            Set<String> allPipeIds = Registry.getInstance().allRegisteredPipeIds();

            for(String pipeTopic:allPipeIds) {
                SimpleConsumer consumer = SimpleConsumer.getInstance();
                consumer.runAlways(pipeTopic, new EventHandler(this.pipelineService,
                        StoreOrchestrator.getInstance()));
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
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
}
