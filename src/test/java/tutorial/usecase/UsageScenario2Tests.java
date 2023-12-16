package tutorial.usecase;

import com.appgallabs.dataplatform.TestConstants;
import com.appgallabs.dataplatform.client.sdk.api.Configuration;
import com.appgallabs.dataplatform.client.sdk.api.DataPipeline;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsageScenario2Tests {
    private static Logger logger = LoggerFactory.getLogger(UsageScenario2Tests.class);

    /**
     * Scenario: A single MongoDB data source starts ingestion
     * into a Braineous pipeline and expects the receiving
     * datastore also MongoDB to receive the data into its
     * store
     *
     * @throws Exception
     */
    @Test
    public void scenario2Array() throws Exception{
        //configure the DataPipeline Client
        Configuration configuration = new Configuration().
                ingestionHostUrl("http://localhost:8080/").
                apiKey("0132d8be-c85c-423a-a168-4767f4dd638b").
                apiSecret("d8e452ea-9968-434c-b84c-5276781a60b6").
                streamSizeInBytes(80);
        DataPipeline.configure(configuration);

        String datasetLocation = "tutorial/usecase/scenario2/scenario2Array.json";
        String json = Util.loadResource(datasetLocation);
        JsonElement datasetElement = JsonUtil.validateJson(json);

        //register a pipeline
        String configLocation = "tutorial/usecase/scenario2/scenario2_pipe_config.json";
        json = Util.loadResource(configLocation);
        DataPipeline.registerPipe(json);
        JsonObject configJson = JsonUtil.validateJson(json).getAsJsonObject();


        //send source data through the pipeline
        String pipeId = configJson.get("pipeId").getAsString();
        String entity = TestConstants.ENTITY;
        DataPipeline.sendData(pipeId, entity, datasetElement.toString());

        //confirm data is received on the receiver data store
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
    public void scenario2Object() throws Exception{
        //configure the DataPipeline Client
        Configuration configuration = new Configuration().
                ingestionHostUrl("http://localhost:8080/").
                apiKey("0132d8be-c85c-423a-a168-4767f4dd638b").
                apiSecret("d8e452ea-9968-434c-b84c-5276781a60b6").
                streamSizeInBytes(80);
        DataPipeline.configure(configuration);

        String datasetLocation = "tutorial/usecase/scenario2/scenario2Object.json";
        String json = Util.loadResource(datasetLocation);
        JsonElement datasetElement = JsonUtil.validateJson(json);

        //register a pipeline
        String configLocation = "tutorial/usecase/scenario2/scenario2_pipe_config.json";
        json = Util.loadResource(configLocation);
        DataPipeline.registerPipe(json);
        JsonObject configJson = JsonUtil.validateJson(json).getAsJsonObject();

        //send source data through the pipeline
        String pipeId = configJson.get("pipeId").getAsString();
        String entity = TestConstants.ENTITY;
        DataPipeline.sendData(pipeId, entity, datasetElement.toString());

        //confirm data is received on the receiver data store
    }

}
