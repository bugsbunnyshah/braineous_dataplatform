package prototype.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptComponent {
    private static Logger logger = LoggerFactory.getLogger(ScriptComponent.class);

    public void runComponent(){
        logger.info("COMPONENT: "+this);
    }
}
