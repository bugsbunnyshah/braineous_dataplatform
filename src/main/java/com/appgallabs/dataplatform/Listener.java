package com.appgallabs.dataplatform;

import com.appgallabs.dataplatform.infrastructure.kafka.KafkaMessageHandler;
import com.appgallabs.dataplatform.infrastructure.kafka.PropertiesHelper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.List;

public class Listener {
    private static Listener singleton;
    private Thread listener;

    private KafkaConsumer kafkaConsumer;

    private KafkaMessageHandler messageHandler;

    private final int TIME_OUT_MS = 200;

    private Listener() {
        try {
            this.kafkaConsumer = new KafkaConsumer<>(PropertiesHelper.getProperties());
            this.kafkaConsumer.subscribe(List.of("123"));

            this.listener = new Thread(new Poller());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static Listener getInstance(){
        if(Listener.singleton == null){
            Listener.singleton = new Listener();
        }
        return Listener.singleton;
    }

    public void start(){
        this.listener.start();
    }



    private class Poller implements Runnable{

        @Override
        public void run() {
            while(true){
                ConsumerRecords<String, String> records =
                        kafkaConsumer.poll(Duration.ofMillis(TIME_OUT_MS));


                if (records.count() == 0) {
                    System.out.println("****KAISIHAIMERIJAAN?**********");
                    continue;
                }

                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("****CONSUMER_COURTESY_FUUUCCCKKK**********");
                    System.out.println(record.value());
                }
            }
        }
    }
}
