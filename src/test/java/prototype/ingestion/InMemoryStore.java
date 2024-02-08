package prototype.ingestion;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import prototype.ingestion.Record;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryStore implements Serializable {

    private Map<String, List<Record>> recordStore = new HashMap<>();

    public void addRecords(Tenant tenant, String pipeId, String entity, List<Record> records){
        String key = this.getKey(tenant, pipeId, entity);
        List<Record> stored = this.recordStore.get(key);
        if(stored == null){
            stored = new ArrayList<>();
            this.recordStore.put(key, stored);
        }
        stored.addAll(records);
    }

    public List<Record> getRecords(Tenant tenant, String pipeId, String entity){
        String key = this.getKey(tenant, pipeId, entity);
        List<Record> records = this.recordStore.get(key);
        if(records == null){
            return new ArrayList<>();
        }
        return records;
    }

    private String getKey(Tenant tenant, String pipeId, String entity){
        String principal = tenant.getPrincipal();

        String key = principal + pipeId + entity;

        return key;
    }
}
