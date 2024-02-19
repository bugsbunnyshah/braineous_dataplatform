package tutorial.usecase;

import com.appgallabs.dataplatform.TestConstants;
import com.appgallabs.dataplatform.client.sdk.api.Configuration;
import com.appgallabs.dataplatform.client.sdk.api.DataPipeline;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.ingestion.pipeline.DataLakeSessionManager;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.targetSystem.framework.staging.Record;
import com.appgallabs.dataplatform.targetSystem.framework.staging.StagingStore;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

@QuarkusTest
public class UsageScenario1Tests {
    private static Logger logger = LoggerFactory.getLogger(UsageScenario1Tests.class);

    @Inject
    private DataLakeSessionManager dataLakeSessionManager;

    private StreamExecutionEnvironment env;

    public UsageScenario1Tests() {
        this.env = StreamExecutionEnvironment.createRemoteEnvironment(
                "localhost",
                Integer.parseInt("8081"),
                "dataplatform-1.0.0-cr2-runner.jar"
        );
    }

    /**
     * Scenario: A single MongoDB data source starts ingestion
     * into a Braineous pipeline and expects the receiving
     * datastore also MongoDB to receive the data into its
     * store
     *
     * @throws Exception
     */
    @Test
    public void scenario1Array() throws Exception{
        String principal = "ffb2969c-5182-454f-9a0b-f3f2fb0ebf75";
        Tenant tenant = new Tenant(principal);

        //configure the DataPipeline Client
        Configuration configuration = new Configuration().
                ingestionHostUrl("http://localhost:8080/").
                apiKey(principal).
                apiSecret("5960253b-6645-41bf-b520-eede5754196e").
                streamSizeInBytes(80);
        DataPipeline.configure(configuration);

        String datasetLocation = "tutorial/usecase/scenario1/scenario1Array.json";
        String json = Util.loadResource(datasetLocation);
        JsonElement datasetElement = JsonUtil.validateJson(json);

        //register a pipeline
        String configLocation = "tutorial/usecase/scenario1/scenario1_pipe_config.json";
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
        Thread.sleep(5000);
        //assert data is received on the receiver data store
        for(StagingStore stagingStore: registeredStores){
            List<Record> records = stagingStore.getData(tenant,
                    pipeId,
                    entity);
            logger.info("*****************************************");
            logger.info("PIPE_ID: "+ pipeId);
            logger.info("NUMBER_OF_RECORDS: "+ records.size());
            logger.info("*****************************************");

            //assert data is stored in the data lake
            /*TableEnvironment tableEnv = this.getTableEnvironment();
            String table = pipeId.toLowerCase() + "." + entity.toLowerCase();
            String sql = "select * from "+table;
            Table result = tableEnv.sqlQuery(sql);
            result.execute().print();*/
        }

        //confirm ingestion and delivery statistics
    }

    /**
     * Scenario: A single MongoDB data source starts ingestion
     * into a Braineous pipeline and expects the receiving
     * datastore also MongoDB to receive the data into its
     * store
     *
     * @throws Exception
     */
    @Test
    public void scenario1Object() throws Exception{
        //configure the DataPipeline Client
        Configuration configuration = new Configuration().
                ingestionHostUrl("http://localhost:8080/").
                apiKey("0132d8be-c85c-423a-a168-4767f4dd638b").
                apiSecret("d8e452ea-9968-434c-b84c-5276781a60b6").
                streamSizeInBytes(80);
        DataPipeline.configure(configuration);

        String datasetLocation = "tutorial/usecase/scenario1/scenario1Object.json";
        String json = Util.loadResource(datasetLocation);
        JsonElement datasetElement = JsonUtil.validateJson(json);

        //register a pipeline
        String configLocation = "tutorial/usecase/scenario1/scenario1_pipe_config.json";
        json = Util.loadResource(configLocation);
        DataPipeline.registerPipe(json);
        JsonObject configJson = JsonUtil.validateJson(json).getAsJsonObject();

        //send source data through the pipeline
        String pipeId = configJson.get("pipeId").getAsString();
        String entity = TestConstants.ENTITY;
        DataPipeline.sendData(pipeId, entity,datasetElement.toString());

        //confirm data is received on the receiver data store
    }


    private StreamTableEnvironment getTableEnvironment(){
        String name  = "myhive";
        final StreamTableEnvironment tableEnv = this.dataLakeSessionManager.newDataLakeCatalogSession(
                this.env,
                name
        );

        return tableEnv;
    }

}
