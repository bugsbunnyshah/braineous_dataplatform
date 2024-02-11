package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.util.JsonUtil;

import java.util.List;

public class LogicRunner implements IntegrationRunner {
    @Override
    public void preProcess(Tenant tenant, String pipeId, String entity) {

    }

    @Override
    public void process(Tenant tenant, String pipeId, String entity, List<Record> records) {
        System.out.println(InMemoryDB.getInstance());
        System.out.println("PROCESSING: # of records: "+ records.size());
    }

    @Override
    public void postProcess(Tenant tenant, String pipeId, String entity) {

    }
}
