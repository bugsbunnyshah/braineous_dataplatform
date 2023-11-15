package com.appgallabs.dataplatform.infrastructure.kafka;

import org.apache.log4j.Logger;

public abstract class AbstractSimpleKafka {
    private final Logger log = Logger.getLogger(AbstractSimpleKafka.class);

    public AbstractSimpleKafka(){
        /*Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        log.info(MessageHelper.getSimpleJSONObject("Created the Shutdown Hook"));*/
    }

    public abstract void shutdown() throws Exception;

    public abstract void runAlways(String topicName, KafkaMessageHandler callback) throws Exception;
}
