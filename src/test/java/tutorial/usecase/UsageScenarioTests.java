package tutorial.usecase;

import com.appgallabs.dataplatform.client.sdk.api.DataPipeline;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsageScenarioTests {
    private static Logger logger = LoggerFactory.getLogger(UsageScenarioTests.class);

    /**
     * Scenario: A single MongoDB data source starts ingestion
     * into a Braineous pipeline and expects the receiving
     * datastore also MongoDB to receive the data into its
     * store
     *
     * @throws Exception
     */
    @Test
    public void scenario1() throws Exception{
        String datasetLocation = "tutorial/usecase/scenario1/scenario1.json";
        String json = Util.loadResource(datasetLocation);
        JsonObject datasetObject = JsonUtil.validateJson(json).getAsJsonObject();

        //setup source data store
        /*String configLocation = "tutorial/usecase/scenario1/mongodb_pipeline.json";
        json = Util.loadResource(configLocation);
        JsonObject configJson = JsonUtil.validateJson(json).getAsJsonObject();
        JsonArray configurations = configJson.getAsJsonArray("configuration");
        Utils.setupSourceStore(configurations.get(0).
                        getAsJsonObject().getAsJsonObject("config"),
                datasetArray);*/

        //register a pipeline
        String configLocation = "tutorial/usecase/scenario1/mongodb_config_1.json";
        json = Util.loadResource(configLocation);
        JsonObject configJson = JsonUtil.validateJson(json).getAsJsonObject();
        JsonUtil.printStdOut(configJson);

        //send source data through the pipeline
        DataPipeline.sendData(datasetObject.toString());

        //confirm data is received on the receiver data store
    }

}
