package prototype.ingestion;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreScriptRunner implements IntegrationRunner {
    private static Logger logger = LoggerFactory.getLogger(CoreScriptRunner.class);

    private Map<String, ScriptComponent> components;

    public CoreScriptRunner() {
        this.components = new HashMap<>();

        for(int i=0; i<2; i++){
            ScriptComponent scriptComponent = new ScriptComponent();
            this.components.put(""+i, scriptComponent);
        }
    }

    public void runIntegration(Tenant tenant, String pipeId, String entity) {
        logger.info("*************");
        logger.info("RUNNING_SCRIPT: "+ this);
        logger.info("************");

        Collection<ScriptComponent> components = this.components.values();
        for(ScriptComponent component: components){
            component.runComponent(tenant, pipeId);
        }
    }

    @Override
    public void preProcess(Tenant tenant, String pipeId, String entity) {

    }

    @Override
    public void process(Tenant tenant, String pipeId, String entity, List<Record> records) {

    }

    @Override
    public void postProcess(Tenant tenant, String pipeId, String entity) {

    }
}
