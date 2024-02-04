package prototype.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreDataIntegrationAgent implements DataIntegrationAgent{
    private static Logger logger = LoggerFactory.getLogger(CoreDataIntegrationAgent.class);

    private ScriptRunner runner = new ScriptRunner();

    @Override
    public void executeIntegrationRunner() {
        logger.info("**********************");
        logger.info("RUNNING: "+this);
        logger.info("**********************");

        this.runner.runIntegration();
    }
}
