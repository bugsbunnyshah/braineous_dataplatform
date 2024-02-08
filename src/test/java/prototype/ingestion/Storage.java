package prototype.ingestion;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.google.gson.JsonArray;

import java.util.List;

public interface Storage {
    public void storeData(Tenant tenant, String pipeId,
            String entity, List<Record> dataset);

    public List<Record> getRecords(Tenant tenant, String pipeId, String entity);
}
