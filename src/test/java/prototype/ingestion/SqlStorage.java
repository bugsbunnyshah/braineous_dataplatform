package prototype.ingestion;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.targetSystem.framework.StoreDriver;
import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SqlStorage implements Storage{
    private static Logger logger = LoggerFactory.getLogger(SqlStorage.class);

    @Override
    public void storeData(Tenant tenant, String pipeId, List<Record> dataset) {
        logger.info(dataset.toString());

        Registry registry = Registry.getInstance();

        List<StoreDriver> storeDrivers = registry.findStoreDrivers(tenant.getPrincipal(), pipeId);

        logger.info("*********SCRIPT_COMPONENT****************");
        logger.info(storeDrivers.toString());
        logger.info("*****************************************");

        StoreDriver storeDriver = storeDrivers.get(0);
        JsonArray jsonArray = new JsonArray();
        for(Record record: dataset){
            jsonArray.add(record.getData());
        }

        storeDriver.storeData(jsonArray);
    }
}
