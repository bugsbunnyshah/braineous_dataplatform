package com.appgallabs.dataplatform.client.sdk.ingestion;

import com.appgallabs.dataplatform.client.sdk.api.Configuration;
import com.appgallabs.dataplatform.client.sdk.api.DataPlatformService;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.ingestion.pipeline.DataLakeSessionManager;
import com.appgallabs.dataplatform.ingestion.pipeline.PipelineService;
import com.appgallabs.dataplatform.ingestion.util.JobManagerUtil;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.targetSystem.framework.staging.Record;
import com.appgallabs.dataplatform.targetSystem.framework.staging.StagingStore;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

@QuarkusTest
public class DataAgnosticTests {
    private static Logger logger = LoggerFactory.getLogger(DataAgnosticTests.class);

    @Inject
    private DataLakeSessionManager dataLakeSessionManager;

    @Inject
    private PipelineService pipelineService;

    public DataAgnosticTests() {
    }

    private void execute(String datasetLocation, String configLocation) throws Exception{
        DataPlatformService dataPlatformService = DataPlatformService.getInstance();
        String apiKey = "6ca06059-aebb-43a6-ba47-306d2469c059";
        String apiSecret = "5929f027-189f-466f-94ab-5076cd5166ec";
        String principal = apiKey;
        Tenant tenant = new Tenant(principal);

        String payload = Util.loadResource(datasetLocation);

        String json = Util.loadResource(configLocation);
        JsonObject configJson = JsonUtil.validateJson(json).getAsJsonObject();
        String pipeId = configJson.get("pipeId").getAsString();
        String entity = configJson.get("entity").getAsString();

        //configure the DataPipeline Client
        Configuration configuration = new Configuration().
                ingestionHostUrl("http://localhost:8080/").
                apiKey(apiKey).
                apiSecret(apiSecret).
                streamSizeInObjects(0);
        dataPlatformService.configure(configuration);

        //register pipe
        dataPlatformService.registerPipe(configJson);

        //send source data through the pipeline
        dataPlatformService.sendData(pipeId, entity,payload);

        //------TEST_ASSERTION_SECTION-----------------------------------------------------------------------
        logger.info("********ASSERTION_PHASE_STARTED....***********");
        Thread.sleep(15000);

        Registry registry = Registry.getInstance();
        List<StagingStore> registeredStores = registry.findStagingStores(tenant.getPrincipal(),
                pipeId);

        //assert data is stored in the data lake
        String table = JobManagerUtil.getTable(apiKey, pipeId, entity);
        String selectSql = "select * from "+table;
        dataPlatformService.print(
                pipeId,
                entity,
                selectSql
        );

        //assert data is received on the receiver data store
        for(StagingStore stagingStore: registeredStores){
            List<Record> records = stagingStore.getData(tenant,
                    pipeId,
                    entity);
            logger.info("*****************************************");
            logger.info("PIPE_ID: "+ pipeId);
            logger.info("ENTITY: "+ entity);
            logger.info("NUMBER_OF_RECORDS: "+ records.size());
            logger.info("*****************************************");
            //TODO: (NOW) confirm ingestion and delivery statistics
        }
    }


    @Test
    public void ingestJson() throws Exception{
        System.out.println("****RUNNING*****: INGEST_JSON");
        String configLocation = "sdk/single_store_json_pipe_config.json";
        String datasetLocation = "sdk/json/valid.json";
        this.execute(datasetLocation, configLocation);
    }

    @Test
    public void ingestXml() throws Exception{
        System.out.println("****RUNNING*****: INGEST_XML");
        String configLocation = "sdk/single_store_xml_pipe_config.json";
        String datasetLocation = "sdk/xml/valid.xml";
        this.execute(datasetLocation, configLocation);
    }

    @Test
    public void ingestCsv() throws Exception{
        System.out.println("****RUNNING*****: INGEST_XML");
        String configLocation = "sdk/single_store_csv_pipe_config.json";
        String datasetLocation = "sdk/csv/valid.csv";
        this.execute(datasetLocation, configLocation);
    }
}
