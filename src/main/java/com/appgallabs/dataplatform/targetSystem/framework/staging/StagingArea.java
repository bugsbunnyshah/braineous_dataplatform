package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityToken;

import com.appgallabs.dataplatform.reporting.IngestionReportingService;
import com.google.gson.JsonObject;
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

    @Inject
    private IngestionReportingService ingestionReportingService;

    @PostConstruct
    public void start(){
        logger.info("**********************");
        logger.info("STAGING_AREA_SERVICE (STATUS): 200OK");
        logger.info("**********************");
    }

    public List<Record> receiveDataForStorage(SecurityToken securityToken,
                                      StagingStore stagingStore,
                                      String pipeId,
                                      long offset,
                                      String entity,
                                      String data){
        try {
            Tenant tenant = new Tenant(securityToken.getPrincipal());

            //Parse the data into Records
            List<Record> records = this.recordGenerator.parsePayload(
                    tenant,
                    pipeId,
                    offset,
                    entity,
                    data);

            //Store the records into the Staging Area Store
            stagingStore.storeData(tenant, pipeId, entity, records);

            return records;
        }catch (Exception e){
            //report to the pipeline monitoring service
            JsonObject jsonObject = new JsonObject();
            this.ingestionReportingService.reportDataError(jsonObject);

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
            //report to the pipeline monitoring service
            JsonObject jsonObject = new JsonObject();
            this.ingestionReportingService.reportDataError(jsonObject);

            throw new RuntimeException(e);
        }
    }
}
