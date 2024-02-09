package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class StagingArea {
    private static Logger logger = LoggerFactory.getLogger(StagingArea.class);

    private Map<String, Storage> registeredStores;

    @Inject
    private DataIntegrationAgent dataIntegrationAgent;

    @Inject
    private RecordGenerator recordGenerator;

    @PostConstruct
    public void start(){
        //TODO: integrate with pipeline_registry (CR2)
        this.registeredStores = new HashMap<>();

        String pipeId = "book_pipe";
        String registration = pipeId;

        InMemoryStorage sqlStorage = new InMemoryStorage();
        this.registeredStores.put(registration, sqlStorage);

        DataIntegrationAgent agent = new DataIntegrationAgent();
    }

    public void receiveDataForStorage(SecurityToken securityToken,
                                      String pipeId,
                                      String entity,
                                      String data){
        try {
            Tenant tenant = new Tenant(securityToken.getPrincipal());

            //Find the registered 'Storage' component for
            //this tenant/pipeId
            String registration = pipeId;
            Storage registeredStore = this.registeredStores.get(registration);

            //Parse the data into Records
            List<Record> records = this.recordGenerator.parsePayload(
                    tenant,
                    pipeId,
                    entity,
                    data);


            //Store the records into the Staging Area Store
            registeredStore.storeData(tenant, pipeId, entity, records);
        }catch (Exception e){
            //TODO: reporting (CR2)

            throw new RuntimeException(e);
        }
    }

    public Storage runIntegrationAgent(SecurityToken securityToken,
                                    String pipeId,
                                    String entity){
        try {
            Tenant tenant = new Tenant(securityToken.getPrincipal());

            //Find the registered 'Agent' for this tenant/pipeId
            String registration = pipeId;
            Storage registeredStore = this.registeredStores.get(registration);

            //Execute the agent and runners
            this.dataIntegrationAgent.executeIntegrationRunner(registeredStore,
                    tenant,
                    pipeId,
                    entity);

            return registeredStore;
        }catch(Exception e){
            //TODO: reporting (CR2)

            throw new RuntimeException(e);
        }
    }
}
