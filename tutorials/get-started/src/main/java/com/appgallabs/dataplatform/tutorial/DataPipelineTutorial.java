package com.appgallabs.dataplatform.tutorial;

import com.appgallabs.dataplatform.client.sdk.api.Configuration;
import com.appgallabs.dataplatform.client.sdk.api.DataPipeline;
import com.appgallabs.dataplatform.client.sdk.api.RegisterPipeException;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonObject;

public class DataPipelineTutorial {

    public static void main(String[] args) throws RegisterPipeException, InterruptedException {
        DataPlatformService dataPlatformService = DataPlatformService.getInstance();
        String apiKey = "ffb2969c-5182-454f-9a0b-f3f2fb0ebf75";
        String apiSecret = "5960253b-6645-41bf-b520-eede5754196e";
        String principal = apiKey;

        //configure the DataPipeline Client
        Configuration configuration = new Configuration().
                ingestionHostUrl("http://localhost:8080/").
                apiKey(apiKey).
                apiSecret(apiSecret).
                streamSizeInBytes(80);
        DataPipeline.configure(configuration);

        //setup data pipe configuration json
        String pipeId = "yya";
        String dataPipeConfiguration = "{\n" +
                "  \"pipeId\": \""+pipeId+"\",\n" +
                "  \"configuration\": [\n" +
                "    {\n" +
                "      \"storeDriver\" : \"com.appgallabs.dataplatform.targetSystem.core.driver.MongoDBStoreDriver\",\n" +
                "      \"name\": \"get_started_store_mongodb\",\n" +
                "      \"config\": {\n" +
                "        \"connectionString\": \"mongodb://localhost:27017\",\n" +
                "        \"database\": \"get_started_store\",\n" +
                "        \"collection\": \"data\",\n" +
                "        \"jsonpathExpressions\": []\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"storeDriver\" : \"com.appgallabs.dataplatform.targetSystem.core.driver.MySqlStoreDriver\",\n" +
                "      \"name\": \"get_started_store_mysql\",\n" +
                "      \"config\": {\n" +
                "        \"connectionString\": \"jdbc:mysql://localhost:3306/braineous_staging_database\",\n" +
                "        \"username\": \"root\",\n" +
                "        \"password\": \"\",\n" +
                "        \"jsonpathExpressions\": []\n" +
                "      }\n" +
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
        pipeId = configJson.get("pipeId").getAsString();
        String entity = "books";
        DataPipeline.sendData(pipeId, entity, sourceData);
    }
}
