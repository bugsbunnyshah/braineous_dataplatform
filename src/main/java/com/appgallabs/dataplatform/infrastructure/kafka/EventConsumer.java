package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EventConsumer {
    private static Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private KafkaSession kafkaSession;


    public EventConsumer() {

    }

    @PostConstruct
    public void start(){
        this.kafkaSession.setEventConsumer(this);
    }
}
