package prototype.ingestion;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.targetSystem.framework.StoreDriver;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SqlStorage implements Storage{
    private static Logger logger = LoggerFactory.getLogger(SqlStorage.class);

    private InMemoryStore inMemoryStore = new InMemoryStore();

    @Override
    public void storeData(Tenant tenant, String pipeId,String entity, List<Record> dataset) {
        logger.info(dataset.toString());

        /*Registry registry = Registry.getInstance();

        List<StoreDriver> storeDrivers = registry.findStoreDrivers(tenant.getPrincipal(), pipeId);

        StoreDriver storeDriver = storeDrivers.get(0);
        JsonArray jsonArray = new JsonArray();
        for(Record record: dataset){
            jsonArray.add(record.toJson());
        }

        storeDriver.storeData(jsonArray);*/
        this.inMemoryStore.addRecords(tenant,
                pipeId,
                entity,
                dataset);
    }

    @Override
    public List<Record> getRecords(Tenant tenant, String pipeId, String entity) {
        List<Record> records = this.inMemoryStore.getRecords(tenant,
                pipeId,
                entity);
        return records;
    }
}
