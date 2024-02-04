package prototype.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ScriptRunner implements IntegrationRunner {
    private static Logger logger = LoggerFactory.getLogger(ScriptRunner.class);

    private Map<String, ScriptComponent> components;

    public ScriptRunner() {
        this.components = new HashMap<>();

        for(int i=0; i<2; i++){
            ScriptComponent scriptComponent = new ScriptComponent();
            this.components.put(""+i, scriptComponent);
        }
    }

    @Override
    public void runIntegration() {
        logger.info("*************");
        logger.info("RUNNING_SCRIPT: "+ this);
        logger.info("************");

        Collection<ScriptComponent> components = this.components.values();
        for(ScriptComponent component: components){
            component.runComponent();
        }
    }
}
