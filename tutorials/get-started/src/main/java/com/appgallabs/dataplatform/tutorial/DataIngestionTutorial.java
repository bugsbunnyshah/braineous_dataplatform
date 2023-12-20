package com.appgallabs.dataplatform.tutorial;

import com.appgallabs.dataplatform.client.sdk.api.Configuration;
import com.appgallabs.dataplatform.client.sdk.api.DataPipeline;
import com.appgallabs.dataplatform.client.sdk.api.RegisterPipeException;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonObject;

public class DataIngestionTutorial {

    public static void main(String[] args) throws RegisterPipeException, InterruptedException {
        //configure the DataPipeline Client
        Configuration configuration = new Configuration().
                ingestionHostUrl("http://localhost:8080/").
                apiKey("0132d8be-c85c-423a-a168-4767f4dd638b").
                apiSecret("d8e452ea-9968-434c-b84c-5276781a60b6").
                streamSizeInBytes(80);
        DataPipeline.configure(configuration);

        //setup data pipe configuration json
        String dataPipeConfiguration = "{\n" +
                "  \"pipeId\": \"123\",\n" +
                "  \"configuration\": [\n" +
                "    {\n" +
                "      \"storeDriver\" : \"com.appgallabs.dataplatform.targetSystem.core.driver.MongoDBStoreDriver\",\n" +
                "      \"name\": \"scenario1_store_mongodb\",\n" +
                "      \"config\": {\n" +
                "        \"connectionString\": \"mongodb://localhost:27017\",\n" +
                "        \"database\": \"scenario1_store\",\n" +
                "        \"collection\": \"data\",\n" +
                "        \"jsonpathExpressions\": []\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"storeDriver\" : \"com.appgallabs.dataplatform.targetSystem.core.driver.MySqlStoreDriver\",\n" +
                "      \"name\": \"scenario1_store_mysql\",\n" +
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
        String pipeId = configJson.get("pipeId").getAsString();
        String entity = "books";
        DataPipeline.sendData(pipeId, entity, sourceData);
    }
}
