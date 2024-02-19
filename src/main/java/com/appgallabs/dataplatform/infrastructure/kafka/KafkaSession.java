package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class KafkaSession {
    private static Logger logger = LoggerFactory.getLogger(KafkaSession.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    private EventProducer eventProducer;

    private EventConsumer eventConsumer;

    private Map<String, Boolean> bootstrappedPipes;

    public void setEventProducer(EventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    public void setEventConsumer(EventConsumer eventConsumer) {
        this.eventConsumer = eventConsumer;
    }

    public void registerPipe(String pipeId){

    }

    public void bootstrap(String pipeId){
       /*for (ConsumerRecord<String, String> record : records) {
            String messageValue = record.value();
            JsonObject json = JsonParser.parseString(messageValue).getAsJsonObject();
            String payload = json.get("message").getAsString();
            JsonElement payloadElem = JsonParser.parseString(payload);
            JsonUtil.printStdOut(payloadElem);
        }*/
        try {
            Thread.sleep(5000);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
