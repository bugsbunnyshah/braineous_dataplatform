package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class DataIntegrationAgent{
    private static Logger logger = LoggerFactory.getLogger(DataIntegrationAgent.class);

    public DataIntegrationAgent() {
    }

    public void executeIntegrationRunner(SecurityToken securityToken,
                                         Tenant tenant,
                                         String pipeId,
                                         String entity,
                                         List<Record> records) {
        String principal  = securityToken.getPrincipal();

        Registry registry = Registry.getInstance();

        //find the registered store drivers for this pipe
        List<IntegrationRunner> registeredRunners = registry.findIntegrationRunners(principal, pipeId);

        for(IntegrationRunner runner:registeredRunners){
            //pre-process
            runner.preProcess(tenant, pipeId, entity);

            //process
            runner.process(tenant, pipeId, entity, records);

            //post-process
            runner.postProcess(tenant, pipeId, entity);
        }
    }
}
