package prototype.ingestion;

import com.appgallabs.dataplatform.infrastructure.Tenant;

public interface IntegrationComponent {
    public void runComponent(Tenant tenant, String pipeId);
}
