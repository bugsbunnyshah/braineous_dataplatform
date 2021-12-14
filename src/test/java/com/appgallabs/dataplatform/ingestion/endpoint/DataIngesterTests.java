package com.appgallabs.dataplatform.ingestion.endpoint;

import com.appgallabs.dataplatform.util.JsonUtil;
import test.components.BaseTest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class DataIngesterTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(DataIngesterTests.class);

    @BeforeEach
    public void setUp()
    {
    }

    @Test
    public void testFetch() throws Exception {
        //TODO: Add sourceSchema and destinationSchema concepts
        JsonObject input = new JsonObject();
        input.addProperty("entity", "flight");

        JsonUtil.print(input);
        Response response = given().body(input.toString()).when().post("/dataIngester/fetch")
                .andReturn();

        String jsonResponse = response.getBody().prettyPrint();

        //assert the body
        JsonObject ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
        assertTrue(ingestedData.get("success").getAsBoolean());
        int statusCode = response.getStatusCode();
        assertEquals(200, statusCode);
    }
}