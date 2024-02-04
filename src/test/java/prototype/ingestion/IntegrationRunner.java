package prototype.ingestion;

import com.appgallabs.dataplatform.infrastructure.Tenant;

public interface IntegrationRunner {

    public void runIntegration(Tenant tenant, String pipeId);
}
