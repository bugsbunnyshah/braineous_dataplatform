package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

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

    public List<Record> receiveDataForStorage(SecurityToken securityToken,
                                      StagingStore stagingStore,
                                      String pipeId,
                                      String entity,
                                      String data){
        try {
            Tenant tenant = new Tenant(securityToken.getPrincipal());

            //Parse the data into Records
            List<Record> records = this.recordGenerator.parsePayload(
                    tenant,
                    pipeId,
                    entity,
                    data);

            //Store the records into the Staging Area Store
            stagingStore.storeData(tenant, pipeId, entity, records);

            return records;
        }catch (Exception e){
            //TODO: reporting (CR2)

            throw new RuntimeException(e);
        }
    }

    public StagingStore runIntegrationAgent(SecurityToken securityToken,
                                    StagingStore stagingStore,
                                    String pipeId,
                                    String entity,
                                            List<Record> records){
        try {
            Tenant tenant = new Tenant(securityToken.getPrincipal());

            this.dataIntegrationAgent.executeIntegrationRunner(stagingStore,
                    tenant,
                    pipeId,
                    entity,
                    records);

            return stagingStore;
        }catch(Exception e){
            //TODO: reporting (CR2)

            throw new RuntimeException(e);
        }
    }
}
