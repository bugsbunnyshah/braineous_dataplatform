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

    public void receiveDataForStorage(SecurityToken securityToken,
                                      Storage storage,
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
            storage.storeData(tenant, pipeId, entity, records);
        }catch (Exception e){
            //TODO: reporting (CR2)

            throw new RuntimeException(e);
        }
    }

    public Storage runIntegrationAgent(SecurityToken securityToken,
                                    Storage storage,
                                    String pipeId,
                                    String entity){
        try {
            Tenant tenant = new Tenant(securityToken.getPrincipal());

            this.dataIntegrationAgent.executeIntegrationRunner(storage,
                    tenant,
                    pipeId,
                    entity);

            return storage;
        }catch(Exception e){
            //TODO: reporting (CR2)

            throw new RuntimeException(e);
        }
    }
}
