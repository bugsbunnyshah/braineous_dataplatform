package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleConsumer extends AbstractSimpleKafka{
    private static Logger log = LoggerFactory.getLogger(SimpleConsumer.class);

    //TODO: make this configurable and find the optimal polling period (CR1)
    private final int TIME_OUT_MS = 30000;

    private KafkaSession kafkaSession;
    private KafkaConsumer<String, String> kafkaConsumer = null;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private Thread t;

    public SimpleConsumer(KafkaSession kafkaSession) {
        super();

        this.kafkaSession = kafkaSession;
        Properties props;
        try {
            props = PropertiesHelper.getProperties();
            this.kafkaConsumer = new KafkaConsumer<>(props);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public KafkaConsumer<String, String> getKafkaConsumer() {
        return kafkaConsumer;
    }

    public void setKafkaConsumer(KafkaConsumer<String, String> kafkaConsumer) {
        this.kafkaConsumer = kafkaConsumer;
    }

    public void close() throws Exception {
        this.kafkaConsumer.close();
    }

    public void runAlways(String topicName, KafkaMessageHandler callback) throws Exception {
        this.kafkaConsumer.subscribe(List.of(topicName));

        this.t = new Thread(() -> {
            //keep running forever or until shutdown() is called from another thread.
            try {
                while (!closed.get()) {
                    ConsumerRecords<String, String> records =
                            this.kafkaConsumer.poll(Duration.ofMillis(TIME_OUT_MS));
                    if (records.count() == 0) {
                        continue;
                    }

                    System.out.println("*****************");
                    System.out.println("RECORDS_RECEIVED: "+records.count());
                    System.out.println("*****************");
                    for (ConsumerRecord<String, String> record : records){
                        String messageValue = record.value();
                        JsonObject json = JsonParser.parseString(messageValue).getAsJsonObject();
                        JsonUtil.printStdOut(json);

                        /*String payload = json.get("message").getAsString();
                        JsonElement payloadElem = JsonParser.parseString(payload);
                        JsonUtil.printStdOut(payloadElem);*/
                    }


                    /*if(isBootstrapped) {
                        for (ConsumerRecord<String, String> record : records) {
                            callback.processMessage(topicName, record);
                        }
                    }else{
                        try {
                            for (ConsumerRecord<String, String> record : records) {
                                String messageValue = record.value();
                                JsonObject json = JsonParser.parseString(messageValue).getAsJsonObject();
                                String payload = json.get("message").getAsString();
                                JsonElement payloadElem = JsonParser.parseString(payload);
                                JsonUtil.printStdOut(payloadElem);
                                if (payloadElem.getAsJsonObject()
                                        .has("handshake_received")) {
                                    isBootstrapped = true;
                                }
                            }
                        }catch(Exception e){

                        }finally {
                            System.out.println("****************");
                            System.out.println("PIPE_ID: "+this.pipeId);
                            System.out.println("BOOTSTRAPPED: "+isBootstrapped);
                            System.out.println("****************");
                        }
                    }*/
                }
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        });

        this.t.start();
    }

    public void shutdown() throws Exception {
        closed.set(true);
        this.kafkaConsumer.wakeup();
    }
}
