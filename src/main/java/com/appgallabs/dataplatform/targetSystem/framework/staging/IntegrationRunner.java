package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.infrastructure.Tenant;

import java.util.List;

public interface IntegrationRunner {

    public void preProcess(Tenant tenant, String pipeId, String entity);

    public void process(Tenant tenant, String pipeId, String entity,
                        List<Record> records);

    public void postProcess(Tenant tenant, String pipeId, String entity);
}
