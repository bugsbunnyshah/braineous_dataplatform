package tutorial.usecase;

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

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

@QuarkusTest
public class UsageScenarioTests {
    private static Logger logger = LoggerFactory.getLogger(UsageScenarioTests.class);

    @Inject
    private DataLakeSessionManager dataLakeSessionManager;

    @Inject
    private PipelineService pipelineService;

    public UsageScenarioTests() {
    }

    private void execute(String datasetLocation, String configLocation) throws Exception{
        DataPlatformService dataPlatformService = DataPlatformService.getInstance();
        String apiKey = "ffb2969c-5182-454f-9a0b-f3f2fb0ebf75";
        String apiSecret = "5960253b-6645-41bf-b520-eede5754196e";
        String principal = apiKey;
        Tenant tenant = new Tenant(principal);

        String json = Util.loadResource(datasetLocation);
        JsonElement datasetElement = JsonUtil.validateJson(json);

        json = Util.loadResource(configLocation);
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
        dataPlatformService.sendData(pipeId, entity,datasetElement.toString());

        //------TEST_ASSERTION_SECTION-----------------------------------------------------------------------
        logger.info("********ASSERTION_PHASE_STARTED....***********");
        Thread.sleep(15000);

        Registry registry = Registry.getInstance();
        List<StagingStore> registeredStores = registry.findStagingStores(tenant.getPrincipal(),
                pipeId);
        //assert data is stored in the data lake
        String catalog = JobManagerUtil.getCatalog(apiKey, pipeId);
        String table = JobManagerUtil.getTable(apiKey, pipeId, entity);
        StreamTableEnvironment tableEnv = this.getTableEnvironment(catalog);
        String selectSql = "select * from "+table;
        //this.printData(tableEnv, table, selectSql);
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

    /**
     * Scenario1 : A data source starts ingestion
     * into a Braineous pipeline and expects the receiving
     * MongoDB database to receive the data into its
     * staging store
     *
     * @throws Exception
     */
    @Test
    public void singleStore() throws Exception{
        System.out.println("****RUNNING*****: SINGLE_STORE");
        String datasetLocation = "tutorial/usecase/scenario1/scenario1.json";
        String configLocation = "tutorial/usecase/scenario1/single_store_pipe_config.json";
        this.execute(datasetLocation, configLocation);
    }

    /**
     * Scenario: A data source starts ingestion
     * into a Braineous pipeline and expects the receiving
     * to multiple stores
     *
     * @throws Exception
     */
    @Test
    public void multipleStores() throws Exception{
        System.out.println("****RUNNING*****: MULTIPLE_STORES");
        String datasetLocation = "tutorial/usecase/scenario1/scenario1.json";
        String configLocation = "tutorial/usecase/scenario1/multiple_stores_pipe_config.json";
        this.execute(datasetLocation, configLocation);
    }

    /*@Test
    public void singleStoreWithTransformation() throws Exception{
        String principal = "ffb2969c-5182-454f-9a0b-f3f2fb0ebf75";
        Tenant tenant = new Tenant(principal);

        //configure the DataPipeline Client
        Configuration configuration = new Configuration().
                ingestionHostUrl("http://localhost:8080/").
                apiKey(principal).
                apiSecret("5960253b-6645-41bf-b520-eede5754196e").
                streamSizeInBytes(80);
        DataPipeline.configure(configuration);

        String datasetLocation = "tutorial/usecase/scenario1/scenario1.json";
        String json = Util.loadResource(datasetLocation);
        JsonElement datasetElement = JsonUtil.validateJson(json);

        //register/or connect an existing pipeline
        String configLocation = "tutorial/usecase/scenario1/single_store_pipe_config_with_transformation.json";
        json = Util.loadResource(configLocation);
        DataPipeline.registerPipe(json);


        //send source data through the pipeline
        JsonObject configJson = JsonUtil.validateJson(json).getAsJsonObject();
        Registry registry = Registry.getInstance();
        String pipeId = configJson.get("pipeId").getAsString();
        String entity = configJson.get("entity").getAsString();
        List<StagingStore> registeredStores = registry.findStagingStores(tenant.getPrincipal(),
                pipeId);
        JsonUtil.printStdOut(JsonUtil.validateJson(registeredStores.toString()));
        DataPipeline.sendData(pipeId, entity,datasetElement.toString());

        //------TEST_ASSERTION_SECTION-----------------------------------------------------------------------
        logger.info("********ASSERTION_PHASE_STARTED....***********");
        Thread.sleep(30000);
        //assert data is received on the receiver data store
        for(StagingStore stagingStore: registeredStores){
            List<Record> records = stagingStore.getData(tenant,
                    pipeId,
                    entity);
            logger.info("*****************************************");
            logger.info("PIPE_ID: "+ pipeId);
            logger.info("NUMBER_OF_RECORDS: "+ records.size());
            JsonUtil.printStdOut(JsonParser.parseString(records.toString()));
            logger.info("*****************************************");

            //assert data is stored in the data lake

            //TODO: (NOW) confirm ingestion and delivery statistics
        }
    }*/
    //----------------------------------------------------------------------------------
    private StreamTableEnvironment getTableEnvironment(String catalogName){
        StreamExecutionEnvironment env = this.pipelineService.getEnv();
        final StreamTableEnvironment tableEnv = this.dataLakeSessionManager.newDataLakeCatalogSession(
                env,
                catalogName
        );

        return tableEnv;
    }

    private void printData(StreamTableEnvironment tableEnv, String table, String selectSql) throws Exception{
        // insert some example data into the table
        final TableResult result =
                tableEnv.executeSql(selectSql);

        // since all cluster operations of the Table API are executed asynchronously,
        // we need to wait until the insertion has been completed,
        // an exception is thrown in case of an error
        result.await();

        System.out.println("********DATA**********");
        System.out.println(selectSql);
        result.print();
        System.out.println("**********************");
    }
}
