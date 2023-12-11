package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.ingestion.pipeline.PipelineService;
import com.appgallabs.dataplatform.ingestion.pipeline.SystemStore;
import com.appgallabs.dataplatform.targetSystem.framework.StoreOrchestrator;
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

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

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

                SystemStore systemStore = this.mongoDBJsonStore.getSystemStore();

                //TODO: (CR2) dig deeper
                SimpleConsumer consumer = SimpleConsumer.getInstance();
                try {
                    consumer.runAlways(pipeTopic, new EventHandler(this.pipelineService,
                            systemStore,
                            StoreOrchestrator.getInstance()));

                    this.registeredPipes.add(pipeId);
                }catch (Exception ex){}
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
