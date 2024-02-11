package com.appgallabs.dataplatform.targetSystem.framework.staging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryDB {
    private static InMemoryDB singleton = new InMemoryDB();
    private Map<String, List<Record>> recordStore = new HashMap<>();

    private InMemoryDB() {
    }


    public static InMemoryDB getInstance(){
        return singleton;
    }

    public Map<String, List<Record>> getRecordStore() {
        return recordStore;
    }

    public void setRecordStore(Map<String, List<Record>> recordStore) {
        this.recordStore = recordStore;
    }

    public void clear(){
        this.recordStore.clear();
    }
}
