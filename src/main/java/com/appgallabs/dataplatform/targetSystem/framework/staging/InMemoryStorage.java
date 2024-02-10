package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class InMemoryStorage implements Storage{
    private static Logger logger = LoggerFactory.getLogger(InMemoryStorage.class);



    @Override
    public void configure(JsonObject configJson) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public JsonObject getConfiguration() {
        return null;
    }

    @Override
    public void storeData(Tenant tenant, String pipeId,String entity, List<Record> dataset) {
        InMemoryStore.getInstance().addRecords(tenant,
                pipeId,
                entity,
                dataset);
    }

    @Override
    public List<Record> getRecords(Tenant tenant, String pipeId, String entity) {
        List<Record> records = InMemoryStore.getInstance().getRecords(tenant,
                pipeId,
                entity);
        return records;
    }
}
