package com.appgallabs.dataplatform.query.graphql.endpoint;

import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class DataLakeGraphQlQueryEndpointTests {
    private static Logger logger = LoggerFactory.getLogger(DataLakeGraphQlQueryEndpointTests.class);

    @Test
    public void query() throws Exception {

        String graphqlQuery = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("dataMapper/sourceData.json"),
                StandardCharsets.UTF_8);
        JsonObject input = new JsonObject();
        input.addProperty("graphqlQuery", graphqlQuery);

        Response response = given().body(input.toString()).when().post("/data/lake/query")
                .andReturn();

        String jsonResponse = response.getBody().prettyPrint();
        int statusCode = response.statusCode();
        logger.info("**************");
        logger.info(response.getStatusLine());
        logger.info("***************");

        //TODO: assert
        assertEquals(200, statusCode);
    }
}
