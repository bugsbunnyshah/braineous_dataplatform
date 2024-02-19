package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.ingestion.pipeline.PipelineService;
import com.appgallabs.dataplatform.ingestion.pipeline.SystemStore;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.targetSystem.framework.StoreOrchestrator;

import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Singleton
public class EventConsumer {
    private static Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private KafkaSession kafkaSession;

    @Inject
    private PipelineService pipelineService;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SchemalessMapper schemalessMapper;

    @Inject
    private StoreOrchestrator storeOrchestrator;


    public EventConsumer() {

    }

    @PostConstruct
    public void start(){
        this.kafkaSession.setEventConsumer(this);
    }

    @PreDestroy
    public void stop(){
        //TODO: (CR2)
        /*try {
            SimpleConsumer.getInstance().shutdown();
        }catch(Exception e){
            throw new RuntimeException(e);
        }*/
    }

    public JsonObject checkStatus(){
        JsonObject response = new JsonObject();
        response.addProperty("status", "LISTENING...");
        return response;
    }

    public void registerPipe(String pipeId){
        try {
            String pipeTopic = pipeId;

            SystemStore systemStore = this.mongoDBJsonStore.getSystemStore();

            SimpleConsumer consumer = new SimpleConsumer(this.kafkaSession);
            try {
                consumer.runAlways(pipeTopic, new EventHandler(this.pipelineService,
                        systemStore,
                        this.schemalessMapper,
                        this.storeOrchestrator)
                );
            }catch (Exception ex){}
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
