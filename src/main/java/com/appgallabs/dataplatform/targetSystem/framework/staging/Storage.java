package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.infrastructure.Tenant;

import java.util.List;

public interface Storage {
    public void storeData(Tenant tenant, String pipeId,
            String entity, List<Record> dataset);

    public List<Record> getRecords(Tenant tenant, String pipeId, String entity);
}
