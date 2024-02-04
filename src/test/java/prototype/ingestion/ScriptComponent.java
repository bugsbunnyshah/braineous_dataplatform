package prototype.ingestion;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.targetSystem.framework.StoreDriver;
import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ScriptComponent implements IntegrationComponent{
    private static Logger logger = LoggerFactory.getLogger(ScriptComponent.class);

    @Override
    public void runComponent(Tenant tenant, String pipeId){
        logger.info("COMPONENT: "+this);

        Registry registry = Registry.getInstance();

        List<StoreDriver> storeDrivers = registry.findStoreDrivers(tenant.getPrincipal(), pipeId);

        logger.info("*********SCRIPT_COMPONENT****************");
        logger.info(storeDrivers.toString());
        logger.info("*****************************************");
    }
}
