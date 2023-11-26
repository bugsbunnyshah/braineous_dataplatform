package com.appgallabs.dataplatform.tutorial;

import com.appgallabs.dataplatform.client.sdk.api.Configuration;
import com.appgallabs.dataplatform.client.sdk.api.DataPipeline;
import com.appgallabs.dataplatform.client.sdk.api.RegisterPipeException;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonObject;

import java.util.UUID;

public class DataIngestionTutorial {

    public static void main(String[] args) throws RegisterPipeException, InterruptedException {
        //configure the DataPipeline Client
        Configuration configuration = new Configuration().
                streamSizeInBytes(80).
                ingestionHostUrl("http://localhost:8080");
        DataPipeline.configure(configuration);

        //String pipeId = UUID.randomUUID().toString();
        String pipeId = "mysql_mongodb_fan_out_to_target";

        //setup data pipe configuration json
        String dataPipeConfiguration = "{\n" +
                "  \"pipeId\": \""+pipeId+"\",\n" +
                "  \"configuration\": [\n" +
                "    {\n" +
                "      \"storeDriver\" : \"com.appgallabs.dataplatform.targetSystem.core.driver.MongoDBStoreDriver\",\n" +
                "      \"name\": \"scenario1_store_mongodb\",\n" +
                "      \"config\": {\n" +
                "        \"connectionString\": \"mongodb://localhost:27017\",\n" +
                "        \"database\": \"scenario1_store\",\n" +
                "        \"collection\": \"data\"\n" +
                "      },\n" +
                "      \"jsonpathExpression\": \"jsonpath:1\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"storeDriver\" : \"com.appgallabs.dataplatform.tutorial.MySqlStoreDriver\",\n" +
                "      \"name\": \"scenario1_store_mysql\",\n" +
                "      \"config\": {\n" +
                "        \"connectionString\": \"jdbc:mysql://localhost:3306/braineous_staging_database\",\n" +
                "        \"username\": \"root\",\n" +
                "        \"password\": \"\"\n" +
                "      },\n" +
                "      \"jsonpathExpression\": \"jsonpath:1\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        JsonObject configJson = JsonUtil.validateJson(dataPipeConfiguration).getAsJsonObject();
        JsonUtil.printStdOut(configJson);


        String user1 = UUID.randomUUID().toString();
        String user2 = UUID.randomUUID().toString();
        //setup source data for ingestion
        String sourceData = "[\n" +
                "  {\n" +
                "    \"id\" : 1,\n" +
                "    \"name\": \""+user1+"\",\n" +
                "    \"age\": 50,\n" +
                "    \"addr\": {\n" +
                "      \"email\": \"joe_1@email.com\",\n" +
                "      \"phone\": \"123456\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"2\",\n" +
                "    \"name\": \""+user2+"\",\n" +
                "    \"age\": 51,\n" +
                "    \"addr\": {\n" +
                "      \"email\": \"joe_2@email.com\",\n" +
                "      \"phone\": \"1234567\"\n" +
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
