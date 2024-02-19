package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.ingestion.pipeline.PipelineService;
import com.appgallabs.dataplatform.ingestion.pipeline.SystemStore;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.targetSystem.framework.StoreOrchestrator;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Singleton
public class KafkaSession {
    private static Logger logger = LoggerFactory.getLogger(KafkaSession.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SchemalessMapper schemalessMapper;

    @Inject
    private StoreOrchestrator storeOrchestrator;

    @Inject
    private PipelineService pipelineService;

    private EventProducer eventProducer;

    private EventConsumer eventConsumer;

    private Map<String, Set<String>> bootstrappedPipes;

    private Map<String, TopicListing> topicListing;


    public KafkaSession() {
        this.bootstrappedPipes = new HashMap<>();
        this.topicListing = new HashMap<>();
    }

    public void setEventProducer(EventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    public void setEventConsumer(EventConsumer eventConsumer) {
        this.eventConsumer = eventConsumer;
    }

    public void registerPipe(String pipeId){
        String tenant = this.securityTokenContainer.getTenant().getPrincipal();
        Set<String> tenantPipes = this.bootstrappedPipes.get(tenant);
        if(tenantPipes == null){
            this.bootstrappedPipes.put(tenant, new HashSet<>());
        }

        tenantPipes = this.bootstrappedPipes.get(tenant);
        tenantPipes.add(pipeId);

        this.registerWithProducer(pipeId, tenantPipes);
        this.registerConsumer(pipeId, tenantPipes);
    }

    public void bootstrap(String pipeId){
        try {
            Thread.sleep(5000);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    //-----------------------------------------------------------------------------------------------------
    private void registerWithProducer(String pipeId, Set<String> registeredPipes){
        try {
            if(!registeredPipes.contains(pipeId)) {
                String pipeTopic = pipeId;

                TopicListing topicListing = null;
                try {
                    topicListing = KafkaTopicHelper.createFixedTopic(pipeTopic);
                }catch(Exception ex){}

                if(topicListing != null) {
                    this.topicListing.put(pipeTopic, topicListing);
                }
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void registerConsumer(String pipeId, Set<String> registeredPipes){
        try {
            String pipeTopic = pipeId;

            SystemStore systemStore = this.mongoDBJsonStore.getSystemStore();

            SimpleConsumer consumer = new SimpleConsumer(this);
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
