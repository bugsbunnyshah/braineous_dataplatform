package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LogicRunner implements IntegrationRunner {
    private static Logger logger = LoggerFactory.getLogger(LogicRunner.class);

    @Override
    public void preProcess(Tenant tenant, String pipeId, String entity) {

    }

    @Override
    public void process(Tenant tenant, String pipeId, String entity, List<Record> records) {
        logger.info(InMemoryDB.getInstance().toString());
        logger.info("PROCESSING: # of records: "+ records.size());
    }

    @Override
    public void postProcess(Tenant tenant, String pipeId, String entity) {

    }
}
