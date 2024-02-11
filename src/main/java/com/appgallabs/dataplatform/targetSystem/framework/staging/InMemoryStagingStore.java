package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.infrastructure.Tenant;

import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryStagingStore implements Serializable, StagingStore {
    private static InMemoryStagingStore singleton = new InMemoryStagingStore();

    private JsonObject configuration;

    private Map<String, List<Record>> recordStore = new HashMap<>();

    public void clear(){
        this.recordStore.clear();
    }

    public InMemoryStagingStore() {
    }

    public static InMemoryStagingStore getInstance(){
        return singleton;
    }


    private String getKey(Tenant tenant, String pipeId, String entity){
        String principal = tenant.getPrincipal();

        String key = principal + pipeId + entity;

        return key;
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
    public void storeData(Tenant tenant, String pipeId, String entity, List<Record> dataSet) {
        String key = this.getKey(tenant, pipeId, entity);
        List<Record> stored = this.recordStore.get(key);
        if(stored == null){
            stored = new ArrayList<>();
            this.recordStore.put(key, stored);
        }
        stored.addAll(dataSet);
    }


    @Override
    public List<Record> getData(Tenant tenant, String pipeId, String entity) {
        String key = this.getKey(tenant, pipeId, entity);
        List<Record> records = this.recordStore.get(key);
        if(records == null){
            return new ArrayList<>();
        }
        return records;
    }
}
