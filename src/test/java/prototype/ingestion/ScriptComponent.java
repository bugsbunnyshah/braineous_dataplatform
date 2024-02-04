package prototype.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptComponent implements IntegrationComponent{
    private static Logger logger = LoggerFactory.getLogger(ScriptComponent.class);

    @Override
    public void runComponent(){
        logger.info("COMPONENT: "+this);
    }
}
