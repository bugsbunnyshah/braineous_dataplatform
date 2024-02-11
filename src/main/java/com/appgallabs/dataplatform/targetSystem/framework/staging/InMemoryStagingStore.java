package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.infrastructure.Tenant;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InMemoryStagingStore implements Serializable, StagingStore {

    private JsonObject configuration;

    public InMemoryStagingStore() {
    }


    @Override
    public void configure(JsonObject configJson) {
        this.configuration = configJson;
    }

    @Override
    public String getName() {
        String name = this.configuration.get("name").getAsString();
        return name;
    }

    @Override
    public JsonObject getConfiguration() {
        return this.configuration;
    }

    @Override
    public synchronized void storeData(Tenant tenant, String pipeId, String entity, List<Record> dataSet) {
        String key = this.getKey(tenant, pipeId, entity);
        List<Record> stored = InMemoryDB.getInstance().getRecordStore().get(key);
        if(stored == null){
            stored = new ArrayList<>();
            InMemoryDB.getInstance().getRecordStore().put(key, stored);
            stored = InMemoryDB.getInstance().getRecordStore().get(key);
        }

        stored.addAll(dataSet);
        //JsonUtil.printStdOut(JsonUtil.validateJson(dataSet.toString()));
    }


    @Override
    public List<Record> getData(Tenant tenant, String pipeId, String entity) {
        String key = this.getKey(tenant, pipeId, entity);
        List<Record> records = InMemoryDB.getInstance().getRecordStore().get(key);
        if(records == null){
            return new ArrayList<>();
        }
        return records;
    }

    //-----------------------------------------------------------
    public static String getKey(Tenant tenant, String pipeId, String entity){
        String principal = tenant.getPrincipal();

        String key = principal + pipeId + entity;

        return key;
    }
}
