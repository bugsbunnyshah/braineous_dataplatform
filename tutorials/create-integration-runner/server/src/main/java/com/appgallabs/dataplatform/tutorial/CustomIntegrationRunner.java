package com.appgallabs.dataplatform.tutorial;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.targetSystem.framework.staging.InMemoryDB;
import com.appgallabs.dataplatform.targetSystem.framework.staging.IntegrationRunner;
import com.appgallabs.dataplatform.targetSystem.framework.staging.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CustomIntegrationRunner implements IntegrationRunner {
    private static Logger logger = LoggerFactory.getLogger(CustomIntegrationRunner.class);

    @Override
    public void preProcess(Tenant tenant, String pipeId, String entity) {

    }

    @Override
    public void process(Tenant tenant, String pipeId, String entity, List<Record> records) {
        logger.info("*****CUSTOM_INTEGRATION_RUNNER********************");
        logger.info(InMemoryDB.getInstance().toString());
        logger.info("PROCESSING: # of records: "+ records.size());
        logger.info("**************************************************");
    }

    @Override
    public void postProcess(Tenant tenant, String pipeId, String entity) {

    }
}
