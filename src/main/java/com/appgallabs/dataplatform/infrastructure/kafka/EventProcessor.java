package com.appgallabs.dataplatform.infrastructure.kafka;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

import org.apache.kafka.clients.admin.TopicListing;

@Singleton
public class EventProcessor {
    private static Logger logger = LoggerFactory.getLogger(EventProcessor.class);

    private final String fixedTopicName = "braineous_dataplatform_kafka_topic";

    public JsonObject processEvent(JsonElement json) {
        try{
            JsonObject response = new JsonObject();

            /**
             * TODO: CREATE_TOPIC
             */
            //create topic
            TopicListing result1 = KafkaTopicHelper.createFixedTopic(fixedTopicName);
            response.addProperty("topicListing", result1.toString());

            //Wait for Kafka to catch up with the topic creation before producing
            Thread.sleep(3000);

            /**
             * TODO: PRODUCE_MESSAGES_FROM_EVENT
             */
            SimpleProducer producer = new SimpleProducer();
            producer.publishToBroker(fixedTopicName, json.toString());

            //Wait for Kafka to catch up before consuming messages
            Thread.sleep(1000);

            /**
             * TODO: CONSUME_MESSAGES_FROM_EVENT
             */
            //consume the messages
            new SimpleConsumer().run(fixedTopicName, new KafkaMessageHandlerImpl(), null);

            return response;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
