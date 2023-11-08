package tutorial.usecase;

import com.appgallabs.dataplatform.client.sdk.api.DataPipeline;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;

import com.google.gson.JsonElement;
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
        String datasetLocation = "tutorial/usecase/scenario1/scenario1Array.json";
        String json = Util.loadResource(datasetLocation);
        JsonElement datasetElement = JsonUtil.validateJson(json);

        //register a pipeline
        String configLocation = "tutorial/usecase/scenario1/scenario1_pipe_config.json";
        json = Util.loadResource(configLocation);
        DataPipeline.registerPipe(json);

        //send source data through the pipeline
        DataPipeline.sendData(datasetElement.toString());

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
    public void scenario1Object() throws Exception{
        String datasetLocation = "tutorial/usecase/scenario1/scenario1Object.json";
        String json = Util.loadResource(datasetLocation);
        JsonElement datasetElement = JsonUtil.validateJson(json);

        //register a pipeline
        String configLocation = "tutorial/usecase/scenario1/scenario1_pipe_config.json";
        json = Util.loadResource(configLocation);
        DataPipeline.registerPipe(json);

        //send source data through the pipeline
        DataPipeline.sendData(datasetElement.toString());

        //confirm data is received on the receiver data store
    }

}
