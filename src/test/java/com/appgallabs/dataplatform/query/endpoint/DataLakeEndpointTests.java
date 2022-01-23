package com.appgallabs.dataplatform.query.endpoint;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class DataLakeEndpointTests {
    private static Logger logger = LoggerFactory.getLogger(DataLakeEndpointTests.class);

    @Test
    public void all() throws Exception{
        String restUrl = "http://localhost:8080/data/lake/?entity=flight";
        Response response = given().get(restUrl).andReturn();
        JsonUtil.printStdOut(JsonParser.parseString(response.body().asString()));
    }
}
