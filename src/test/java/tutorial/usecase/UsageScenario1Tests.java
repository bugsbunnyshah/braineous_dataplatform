package tutorial.usecase;

import com.appgallabs.dataplatform.TestConstants;
import com.appgallabs.dataplatform.client.sdk.api.Configuration;
import com.appgallabs.dataplatform.client.sdk.api.DataPipeline;
import com.appgallabs.dataplatform.infrastructure.Tenant;
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

import java.util.List;

@QuarkusTest
public class UsageScenario1Tests {
    private static Logger logger = LoggerFactory.getLogger(UsageScenario1Tests.class);

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
        JsonObject configJson = JsonUtil.validateJson(json).getAsJsonObject();
        Registry registry = Registry.getInstance();

        //send source data through the pipeline
        String pipeId = configJson.get("pipeId").getAsString();
        String entity = configJson.get("entity").getAsString();
        List<StagingStore> registeredStores = registry.findStagingStores(tenant.getPrincipal(),
                pipeId);
        JsonUtil.printStdOut(JsonUtil.validateJson(registeredStores.toString()));

        //DataPipeline.sendData(pipeId, entity,datasetElement.toString());

        //assert data is received on the receiver data store
        for(StagingStore stagingStore: registeredStores){
            List<Record> records = stagingStore.getData(tenant,
                    pipeId,
                    entity);
            JsonUtil.printStdOut(JsonUtil.validateJson(records.toString()));
        }

        //assert data is stored in the data lake

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

}
