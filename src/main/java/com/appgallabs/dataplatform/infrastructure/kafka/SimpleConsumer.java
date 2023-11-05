package com.appgallabs.dataplatform.infrastructure.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

class SimpleConsumer extends AbstractSimpleKafka{
    private static Logger log = LoggerFactory.getLogger(SimpleConsumer.class);

    private static SimpleConsumer singleton;

    //TODO: make this configurable and find the optimal polling period (CR1)
    private final int TIME_OUT_MS = 30000;
    private KafkaConsumer<String, String> kafkaConsumer = null;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private Thread t;

    private SimpleConsumer() {
        super();
        Properties props;
        try {
            props = PropertiesHelper.getProperties();
            this.kafkaConsumer = new KafkaConsumer<>(props);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SimpleConsumer getInstance(){
        if(SimpleConsumer.singleton == null){
            SimpleConsumer.singleton = new SimpleConsumer();
        }
        return SimpleConsumer.singleton;
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

                    for (ConsumerRecord<String, String> record : records) {
                        callback.processMessage(topicName, record);
                    }
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
