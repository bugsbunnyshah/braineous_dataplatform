package com.appgallabs.dataplatform.client.sdk.api;

import com.appgallabs.dataplatform.TestConstants;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class DataPipelineTests {
    private static Logger logger = LoggerFactory.getLogger(DataPipelineTests.class);

    //TODO: solidify: (CR1)
    @Test
    public void sendData() throws Exception{
        //configure the DataPipeline Client
        Configuration configuration = new Configuration().
                ingestionHostUrl("http://localhost:8080/").
                apiKey("0132d8be-c85c-423a-a168-4767f4dd638b").
                apiSecret("d8e452ea-9968-434c-b84c-5276781a60b6").
                streamSizeInBytes(80);
        DataPipeline.configure(configuration);

        String jsonResource = "ingestion/algorithm/input.json";
        //String jsonResource = "people.json";
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream(jsonResource),
                StandardCharsets.UTF_8
        );

        for(int i=0; i<1; i++) {
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
            json.addProperty("offset",i);

            String pipeId = "123";
            String entity = TestConstants.ENTITY;
            DataPipeline.sendData(pipeId, entity, json.toString());
        }

        //assertions
        //System.out.println("********************************");
        //JsonUtil.printStdOut(response);
        //assertNotNull(response);
    }

    @Test
    public void createTargetSystemRegistration() throws Exception
    {
        //configure the DataPipeline Client
        Configuration configuration = new Configuration().
                ingestionHostUrl("http://localhost:8080/").
                apiKey("0132d8be-c85c-423a-a168-4767f4dd638b").
                apiSecret("d8e452ea-9968-434c-b84c-5276781a60b6").
                streamSizeInBytes(80);
        DataPipeline.configure(configuration);

        String pipeName = "telescope_data";

        TargetSystemBuilder targetSystemBuilder = DataPipeline.createPipe(pipeName);
        String pipeId = targetSystemBuilder.getPipeId();

        targetSystemBuilder.setPipeName(pipeName);
        targetSystemBuilder.setPipeId(pipeId);

        String storeDriver = "com.appgallabs.dataplatform.targetSystem.core.driver.MongoDBStoreDriver";
        String storeName = "scenario1_store_mongodb";
        String jsonPathExpression = "jsonpath:mongodb";
        JsonObject config = new JsonObject();
        config.addProperty("connectionString","mongodb://localhost:27017");
        config.addProperty("database","scenario1_store");
        config.addProperty("collection","data");
        targetSystemBuilder.setStoreDriver(storeDriver).
                setStoreName(storeName).
                setJsonPathExpression(jsonPathExpression).
                setConfig(config).
                addTargetSystem();


        storeDriver = "com.appgallabs.dataplatform.targetSystem.core.driver.MySqlStoreDriver";
        storeName = "scenario1_store_mysql";
        jsonPathExpression = "jsonpath:mysql";
        config = new JsonObject();
        config.addProperty("connectionString","jdbc:mysql://localhost:3306/braineous_staging_database");
        config.addProperty("username","root");
        config.addProperty("password","");
        JsonObject registryEntry = targetSystemBuilder.setStoreDriver(storeDriver).
                setStoreName(storeName).
                setJsonPathExpression(jsonPathExpression).
                setConfig(config).
                build();

        JsonObject registration = DataPipeline.registerPipe(targetSystemBuilder);
        JsonUtil.printStdOut(registration);
    }
}
