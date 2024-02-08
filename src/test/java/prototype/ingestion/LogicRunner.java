package prototype.ingestion;

import com.appgallabs.dataplatform.infrastructure.Tenant;

import java.util.List;

public class LogicRunner implements IntegrationRunner {
    @Override
    public void preProcess(Tenant tenant, String pipeId, String entity) {
        System.out.println("****PRE_PROCESS****");
    }

    @Override
    public void process(Tenant tenant, String pipeId, String entity, List<Record> records) {
        System.out.println("****PROCESS****");
    }

    @Override
    public void postProcess(Tenant tenant, String pipeId, String entity) {
        System.out.println("****POST_PROCESS****");
    }
}
