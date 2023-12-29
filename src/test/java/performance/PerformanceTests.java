package performance;

import com.appgallabs.dataplatform.client.sdk.api.Configuration;
import com.appgallabs.dataplatform.client.sdk.api.DataPipeline;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceTests {
    private static Logger logger = LoggerFactory.getLogger(PerformanceTests.class);

    @Test
    public void startSmallDataset() throws Exception{
        //configure the DataPipeline Client
        Configuration configuration = new Configuration().
                ingestionHostUrl("http://localhost:8080/").
                apiKey("0132d8be-c85c-423a-a168-4767f4dd638b").
                apiSecret("d8e452ea-9968-434c-b84c-5276781a60b6").
                streamSizeInBytes(80);
        DataPipeline.configure(configuration);

        String datasetLocation = "performance/southwest_flight.json";
        String json = Util.loadResource(datasetLocation);
        JsonObject flightJson = JsonUtil.validateJson(json).getAsJsonObject();
        JsonArray datasetElement = new JsonArray();
        for(int i=0; i<25; i++){
            datasetElement.add(flightJson);
        }
        //JsonUtil.printStdOut(datasetElement);

        String pipeId = "small_flight_pipe";
        String configLocation = "performance/small_pipe_config.json";
        String configJsonString = Util.loadResource(configLocation);
        JsonObject configJson = JsonUtil.validateJson(configJsonString).getAsJsonObject();
        configJson.addProperty("pipeId", pipeId);
        DataPipeline.registerPipe(configJson.toString());
        JsonUtil.printStdOut(configJson);

        //send source data through the pipeline
        for(int i=0; i<20; i++) {
            String entity = "flights";
            DataPipeline.sendData(pipeId, entity, datasetElement.toString());
        }

        /*String entity = "flights";
        DataPipeline.sendData(pipeId, entity, datasetElement.toString());*/
    }

    @Test
    public void startMediumDataset() throws Exception{
        //configure the DataPipeline Client
        Configuration configuration = new Configuration().
                ingestionHostUrl("http://localhost:8080/").
                apiKey("0132d8be-c85c-423a-a168-4767f4dd638b").
                apiSecret("d8e452ea-9968-434c-b84c-5276781a60b6").
                streamSizeInBytes(80);
        DataPipeline.configure(configuration);

        String datasetLocation = "performance/southwest_medium.json";
        String json = Util.loadResource(datasetLocation);
        JsonElement datasetElement = JsonUtil.validateJson(json);
        JsonUtil.printStdOut(datasetElement);

        String pipeId = "small_flight_pipe";
        String configLocation = "performance/small_pipe_config.json";
        String configJsonString = Util.loadResource(configLocation);
        JsonObject configJson = JsonUtil.validateJson(configJsonString).getAsJsonObject();
        configJson.addProperty("pipeId", pipeId);
        DataPipeline.registerPipe(configJson.toString());
        JsonUtil.printStdOut(configJson);

        //send source data through the pipeline
        String entity = "flights";
        DataPipeline.sendData(pipeId, entity, datasetElement.toString());
    }
}
