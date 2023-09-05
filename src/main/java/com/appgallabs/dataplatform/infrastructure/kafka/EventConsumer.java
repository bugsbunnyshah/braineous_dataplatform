package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.ingestion.pipeline.PipelineService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EventConsumer {
    private static Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    private final String braineousKafkaTopic = "braineous_dataplatform_kafka_topic";
    private SimpleConsumer consumer;

    @Inject
    private PipelineService pipelineService;

    @PostConstruct
    public void start(){
        try {
            this.consumer = SimpleConsumer.getInstance();
            this.consumer.runAlways(braineousKafkaTopic, new KafkaMessageHandlerImpl(this.pipelineService));
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void stop(){
        try {
            this.consumer.close();
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
