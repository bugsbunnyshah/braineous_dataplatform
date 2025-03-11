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

        //1 - process the dataset

        //1 - determine the destination dataset structure (structured/unstructured)
        // from configuration

        //2 - generate target store instructions to store the data such as sqls etc

        //3- execute CDC process
    }
}
