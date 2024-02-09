package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.infrastructure.Tenant;

import java.util.List;

public class NoSqlStorage implements Storage{
    @Override
    public void storeData(Tenant tenant, String pipeId,String entity, List<Record> dataset) {

    }

    @Override
    public List<Record> getRecords(Tenant tenant, String pipeId, String entity) {
        return null;
    }
}
