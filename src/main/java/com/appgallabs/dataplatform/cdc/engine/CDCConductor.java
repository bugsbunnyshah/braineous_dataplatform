package com.appgallabs.dataplatform.cdc.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
public class CDCConductor {
    private static Logger logger = LoggerFactory.getLogger(CDCConductor.class);

    public void orchestrate(){
        logger.info("*************************************");
        logger.info("CONDUCTOR_ORCHESTRATION_START_SUCCESS");
        logger.info("*************************************");
    }
}
