package com.appgallabs.dataplatform.client.sdk.api;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class DataLakeGraphQlQueryTests {
    private static Logger logger = LoggerFactory.getLogger(DataLakeGraphQlQueryTests.class);

    //TODO: solidify: CR2
    @Test
    public void sendQuery() throws Exception{
        String graphqlQuery = "";

        for(int i=0; i<10; i++) {
            JsonArray result = DataLakeGraphQlQuery.sendQuery(graphqlQuery);
            JsonUtil.printStdOut(result);
        }

        //assertions
        //System.out.println("********************************");
        //JsonUtil.printStdOut(response);
        //assertNotNull(response);
    }
}
