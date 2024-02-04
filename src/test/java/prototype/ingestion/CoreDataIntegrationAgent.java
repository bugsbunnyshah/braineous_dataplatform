package prototype.ingestion;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreDataIntegrationAgent implements DataIntegrationAgent{
    private static Logger logger = LoggerFactory.getLogger(CoreDataIntegrationAgent.class);

    private IntegrationRunner runner;

    public CoreDataIntegrationAgent() {
        this.runner = new CoreScriptRunner();
    }

    @Override
    public void executeIntegrationRunner(Tenant tenant, String pipeId) {
        this.runner.runIntegration(tenant, pipeId);
    }
}
