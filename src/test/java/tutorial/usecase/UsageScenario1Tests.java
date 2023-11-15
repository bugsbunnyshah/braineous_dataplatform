package tutorial.usecase;

import com.appgallabs.dataplatform.TestConstants;
import com.appgallabs.dataplatform.client.sdk.api.DataPipeline;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        /*String datasetLocation = "tutorial/usecase/scenario1/scenario1Array.json";
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
        DataPipeline.sendData(pipeId, entity, datasetElement.toString());

        //confirm data is received on the receiver data store*/

        String dataPipeConfiguration = "{\n" +
                "  \"pipeId\": \"123\",\n" +
                "  \"configuration\": [\n" +
                "    {\n" +
                "      \"storeDriver\" : \"com.appgallabs.dataplatform.receiver.core.driver.MongoDBStoreDriver\",\n" +
                "      \"name\": \"scenario1_store\",\n" +
                "      \"config\": {\n" +
                "        \"connectionString\": \"mongodb://localhost:27017\",\n" +
                "        \"database\": \"scenario1_store\",\n" +
                "        \"collection\": \"data\"\n" +
                "      },\n" +
                "      \"jsonpathExpression\": \"jsonpath:1\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        JsonUtil.printStdOut(JsonUtil.validateJson(dataPipeConfiguration));



        String sourceData = "[\n" +
                "  {\n" +
                "    \"id\" : 1,\n" +
                "    \"name\": \"Joe Black0\",\n" +
                "    \"age\": 50,\n" +
                "    \"addr\": {\n" +
                "      \"email\": \"test@email.com\",\n" +
                "      \"phone\": \"123456\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"2\",\n" +
                "    \"name\": \"Joe Black1\",\n" +
                "    \"age\": 50,\n" +
                "    \"addr\": {\n" +
                "      \"email\": \"test@email.com\",\n" +
                "      \"phone\": \"123456\"\n" +
                "    }\n" +
                "  }\n" +
                "]";
        JsonUtil.printStdOut(JsonUtil.validateJson(sourceData));

        JsonObject response = DataPipeline.registerPipe(dataPipeConfiguration);
        JsonUtil.printStdOut(response);

        JsonObject configJson = JsonUtil.validateJson(dataPipeConfiguration).getAsJsonObject();

        //send source data through the pipeline
        String pipeId = configJson.get("pipeId").getAsString();
        String entity = "books";
        DataPipeline.sendData(pipeId, entity, sourceData);
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
