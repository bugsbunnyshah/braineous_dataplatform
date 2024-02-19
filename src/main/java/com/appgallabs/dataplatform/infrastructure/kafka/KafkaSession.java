package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.ingestion.pipeline.PipelineService;
import com.appgallabs.dataplatform.ingestion.pipeline.SystemStore;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.targetSystem.framework.StoreOrchestrator;

import org.apache.kafka.clients.admin.TopicListing;

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
public class KafkaSession {
    private static Logger logger = LoggerFactory.getLogger(KafkaSession.class);

    //TODO: (NOW)
    private final int TIME_OUT_MS = 30000;

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

    public int getTimeOut() {
        return TIME_OUT_MS;
    }

    //lifecyle------
    @PostConstruct
    public void start(){
        //TODO: (NOW)
    }

    @PreDestroy
    public void stop(){
        //TODO: (NOW)
    }

    //-------------------------------------------

    public void registerPipe(String pipeId){
        String tenant = this.securityTokenContainer.getTenant().getPrincipal();
        Set<String> tenantPipes = this.bootstrappedPipes.get(tenant);
        if(tenantPipes == null){
            this.bootstrappedPipes.put(tenant, new HashSet<>());
        }

        tenantPipes = this.bootstrappedPipes.get(tenant);
        if(tenantPipes.contains(pipeId)){
            //its registed and live with the kafka system
            return;
        }


        tenantPipes.add(pipeId);

        //make sure pipe is registered and live at the broker
        this.registerWithProducer(pipeId, tenantPipes);

        //register the consumer
        SimpleConsumer simpleConsumer = this.registerConsumer(pipeId, tenantPipes);

        //make sure producer, broker, and consumer are ready
        this.bootstrap(pipeId, simpleConsumer);
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

    private SimpleConsumer registerConsumer(String pipeId, Set<String> registeredPipes){
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

            return consumer;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void bootstrap(String pipeId, SimpleConsumer simpleConsumer){
        String topicName = pipeId;
        try {
            Thread.sleep(TIME_OUT_MS);
        }catch(Exception e){
            throw new RuntimeException(e);
        }

        /*try {
            KafkaMessageHandler callback = simpleConsumer.getCallback();
            while (true) {
                ConsumerRecords<String, String> records = simpleConsumer.getKafkaConsumer().
                        poll(Duration.ofMillis(TIME_OUT_MS));

                if (records.count() == 0) {
                    continue;
                }

                for (ConsumerRecord<String, String> record : records) {
                    callback.processMessage(topicName, record);
                }

                break;
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }*/
    }
}
