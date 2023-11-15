package com.appgallabs.dataplatform.tutorial;

import com.appgallabs.dataplatform.client.sdk.api.DataPipeline;
import com.appgallabs.dataplatform.client.sdk.api.RegisterPipeException;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonObject;

public class DataIngestionTutorial {

    public static void main(String[] args) throws RegisterPipeException, InterruptedException {
        //setup data pipe configuration json
        String dataPipeConfiguration = "{\n" +
                "  \"pipeId\": \"123\",\n" +
                "  \"configuration\": [\n" +
                "    {\n" +
                "      \"storeDriver\" : \"com.appgallabs.dataplatform.receiver.core.driver.MongoDBStoreDriver\",\n" +
                "      \"name\": \"get_started_store\",\n" +
                "      \"config\": {\n" +
                "        \"connectionString\": \"mongodb://localhost:27017\",\n" +
                "        \"database\": \"get_started_store\",\n" +
                "        \"collection\": \"data\"\n" +
                "      },\n" +
                "      \"jsonpathExpression\": \"jsonpath:1\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        JsonObject configJson = JsonUtil.validateJson(dataPipeConfiguration).getAsJsonObject();
        JsonUtil.printStdOut(configJson);


        //setup source data for ingestion
        String sourceData = "[\n" +
                "  {\n" +
                "    \"id\" : 1,\n" +
                "    \"name\": \"Joe Black1\",\n" +
                "    \"age\": 50,\n" +
                "    \"addr\": {\n" +
                "      \"email\": \"test@email.com\",\n" +
                "      \"phone\": \"123456\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"2\",\n" +
                "    \"name\": \"Joe Black2\",\n" +
                "    \"age\": 50,\n" +
                "    \"addr\": {\n" +
                "      \"email\": \"test@email.com\",\n" +
                "      \"phone\": \"123456\"\n" +
                "    }\n" +
                "  }\n" +
                "]";
        JsonUtil.printStdOut(JsonUtil.validateJson(sourceData));

        //register pipe
        JsonObject response = DataPipeline.registerPipe(dataPipeConfiguration);
        JsonUtil.printStdOut(response);

        //send source data through the pipeline
        String pipeId = configJson.get("pipeId").getAsString();
        String entity = "books";
        DataPipeline.sendData(pipeId, entity, sourceData);
    }
}
