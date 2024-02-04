package prototype.ingestion;

import com.appgallabs.dataplatform.infrastructure.Tenant;

public interface DataIntegrationAgent {

    public void executeIntegrationRunner(Tenant tenant, String pipeId);
}
