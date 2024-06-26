package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class DataIntegrationAgent{
    private static Logger logger = LoggerFactory.getLogger(DataIntegrationAgent.class);

    private IntegrationRunner runner;

    public DataIntegrationAgent() {
        this.runner = new LogicRunner();
    }

    public void executeIntegrationRunner(StagingStore stagingStore,
                                         Tenant tenant,
                                         String pipeId,
                                         String entity,
                                         List<Record> records) {
        //pre-process
        this.runner.preProcess(tenant, pipeId, entity);

        //process
        this.runner.process(tenant, pipeId, entity, records);

        //post-process
        this.runner.postProcess(tenant, pipeId, entity);
    }
}
