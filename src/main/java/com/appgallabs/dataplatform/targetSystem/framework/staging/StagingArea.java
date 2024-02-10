package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class StagingArea {
    private static Logger logger = LoggerFactory.getLogger(StagingArea.class);

    @Inject
    private DataIntegrationAgent dataIntegrationAgent;

    @Inject
    private RecordGenerator recordGenerator;

    @PostConstruct
    public void start(){
    }

    public void receiveDataForStorage(SecurityToken securityToken,
                                      String pipeId,
                                      String entity,
                                      String data){
        try {
            Registry registry = Registry.getInstance();
            Tenant tenant = new Tenant(securityToken.getPrincipal());

            //Find the registered 'Storage' components for
            //this tenant/pipeId
            List<Storage> registeredStores = registry.findStorages(tenant.getPrincipal(),pipeId);

            for(Storage registeredStore: registeredStores) {
                //Parse the data into Records
                List<Record> records = this.recordGenerator.parsePayload(
                        tenant,
                        pipeId,
                        entity,
                        data);


                //Store the records into the Staging Area Store
                registeredStore.storeData(tenant, pipeId, entity, records);
            }
        }catch (Exception e){
            //TODO: reporting (CR2)

            throw new RuntimeException(e);
        }
    }

    public List<Storage> runIntegrationAgent(SecurityToken securityToken,
                                    String pipeId,
                                    String entity){
        try {
            Registry registry = Registry.getInstance();
            Tenant tenant = new Tenant(securityToken.getPrincipal());

            //Find the registered 'Storage' components for
            //this tenant/pipeId
            List<Storage> registeredStores = registry.findStorages(tenant.getPrincipal(),pipeId);

            for(Storage registeredStore: registeredStores) {
                //Execute the agent and runners
                this.dataIntegrationAgent.executeIntegrationRunner(registeredStore,
                        tenant,
                        pipeId,
                        entity);

            }

            return registeredStores;
        }catch(Exception e){
            //TODO: reporting (CR2)

            throw new RuntimeException(e);
        }
    }
}
