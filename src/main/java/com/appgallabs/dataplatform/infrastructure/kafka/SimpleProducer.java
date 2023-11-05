package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


class SimpleProducer extends AbstractSimpleKafka {
    private static Logger logger = LoggerFactory.getLogger(SimpleProducer.class);
    private static SimpleProducer singleton;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private KafkaProducer<String, String> kafkaProducer;

    private SimpleProducer(){
        super();
        try {
            Properties props = PropertiesHelper.getProperties();
            this.kafkaProducer = new KafkaProducer<>(props);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static SimpleProducer getInstance(){
        if(SimpleProducer.singleton == null){
            SimpleProducer.singleton = new SimpleProducer();
        }
        return SimpleProducer.singleton;
    }

    public void shutdown() throws Exception {
        closed.set(true);
        this.kafkaProducer.close();
    }

    public void publishToBroker(SecurityTokenContainer securityTokenContainer,
                                String topicName, String message) throws Exception {
        String key = UUID.randomUUID().toString();
        this.send(securityTokenContainer,topicName, key, message);
    }

    protected void send(SecurityTokenContainer securityTokenContainer, String topicName, String key, String message) throws Exception {
        String source = SimpleProducer.class.getName();

        //Use the helper to create an informative log entry in JSON format
        JSONObject obj = MessageHelper.getMessageLogEntryJSON(securityTokenContainer.getSecurityToken(), source, topicName, key, message);
        //log.info(obj.toJSONString());

        //create the ProducerRecord object which will
        //represent the message to the Kafka broker.
        String messageString = obj.toJSONString();
        ProducerRecord<String, String> producerRecord =
                new ProducerRecord<>(topicName, key, messageString);

        //Send the message to the Kafka broker using the internal
        //KafkaProducer
        this.kafkaProducer.send(producerRecord);
    }

    @Override
    public void runAlways(String topicName, KafkaMessageHandler callback) throws Exception {

    }
}
