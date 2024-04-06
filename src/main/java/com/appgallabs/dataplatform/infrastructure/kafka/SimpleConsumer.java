package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.reporting.IngestionReportingService;
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

    private KafkaSession kafkaSession;
    private KafkaConsumer<String, String> kafkaConsumer = null;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private Thread t;

    private String pipeId;
    private KafkaMessageHandler callback;

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

    public KafkaMessageHandler getCallback() {
        return callback;
    }

    public void close() throws Exception {
        this.kafkaConsumer.close();
    }

    public void runAlways(String topicName, KafkaMessageHandler callback) throws Exception {
        this.kafkaConsumer.subscribe(List.of(topicName));
        this.pipeId = topicName;
        this.callback = callback;

        this.t = new Thread(() -> {
            //keep running forever or until shutdown() is called from another thread.
            try {
                while (!closed.get()) {
                    ConsumerRecords<String, String> records =
                            this.kafkaConsumer.poll(Duration.ofMillis(kafkaSession.getTimeOut()));
                    if (records.count() == 0) {
                        continue;
                    }

                    //For debugging only
                    /*
                    System.out.println("*****************");
                    System.out.println("RECORDS_RECEIVED: "+records.count());
                    System.out.println("*****************");
                    for (ConsumerRecord<String, String> record : records){
                        String messageValue = record.value();
                        JsonObject json = JsonParser.parseString(messageValue).getAsJsonObject();
                        JsonUtil.printStdOut(json);
                    }
                    */


                    for (ConsumerRecord<String, String> record : records) {
                        callback.processMessage(topicName, record);
                    }
                }
            }catch (Exception e){
                log.error(e.getMessage(), e);

                //integrated with failure reports
                JsonObject error = new JsonObject();
                IngestionReportingService reportingService =
                        this.kafkaSession.getIngestionReportingService();
                reportingService.reportDataError(error);
            }
        });

        this.t.start();
    }

    public void shutdown() throws Exception {
        closed.set(true);
        this.kafkaConsumer.wakeup();
    }
}
