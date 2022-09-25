package com.appgallabs.dataplatform.ingestion.endpoint;

import com.appgallabs.dataplatform.ingestion.service.MapperService;
import com.appgallabs.dataplatform.ingestion.util.CSVDataUtil;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;

import com.appgallabs.dataplatform.query.ObjectGraphQueryService;
import test.components.BaseTest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.IngesterTest;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class DataMapperTests extends IngesterTest
{
    private static Logger logger = LoggerFactory.getLogger(DataMapperTests.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private MapperService mapperService;

    private CSVDataUtil csvDataUtil = new CSVDataUtil();

    @Inject
    private ObjectGraphQueryService objectGraphQueryService;

    @BeforeEach
    public void setUp()
    {
    }

    @Test
    public void testMapWithOneToOneFields() throws Exception {
        String sourceSchema = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("dataMapper/sourceSchema.json"),
                StandardCharsets.UTF_8);
        String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("dataMapper/sourceData.json"),
                StandardCharsets.UTF_8);
        JsonObject input = new JsonObject();
        input.addProperty("sourceSchema", sourceSchema);
        input.addProperty("destinationSchema", sourceSchema);
        input.addProperty("sourceData", sourceData);
        input.addProperty("entity","person");


        Response response = given().body(input.toString()).when().post("/dataMapper/map")
                .andReturn();

        String jsonResponse = response.getBody().prettyPrint();
        logger.info("**************");
        logger.info(response.getStatusLine());
        logger.info(jsonResponse);
        logger.info("***************");

        //assert the body
        JsonObject ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
        assertNotNull(ingestedData.get("dataLakeId"));
        int statusCode = response.getStatusCode();
        assertEquals(200, statusCode);

        Thread.sleep(3600000);
    }

    @Test
    public void testMapWithScatteredFields() throws Exception {
        String sourceSchema = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("dataMapper/sourceSchema.json"),
                StandardCharsets.UTF_8);
        String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("dataMapper/sourceDataWithScatteredFields.json"),
                StandardCharsets.UTF_8);

        JsonObject input = new JsonObject();
        input.addProperty("sourceSchema", sourceSchema);
        input.addProperty("destinationSchema", sourceSchema);
        input.addProperty("sourceData", sourceData);
        input.addProperty("entity","person");


        Response response = given().body(input.toString()).when().post("/dataMapper/map")
                .andReturn();

        String jsonResponse = response.getBody().prettyPrint();
        logger.info("***************");
        logger.info(response.getStatusLine());
        logger.info("***************");

        //assert the body
        JsonObject ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
        assertNotNull(ingestedData.get("dataLakeId"));
        int statusCode = response.getStatusCode();
        assertEquals(200, statusCode);
    }

    @Test
    public void testMapCsvSourceData() throws Exception
    {
        String spaceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "dataMapper/data.csv"),
                StandardCharsets.UTF_8);
        JsonObject input = new JsonObject();
        input.addProperty("sourceSchema", "");
        input.addProperty("destinationSchema", "");
        input.addProperty("sourceData", spaceData);
        input.addProperty("hasHeader", true);
        input.addProperty("entity","person");
        Response response = given().body(input.toString()).when().post("/dataMapper/mapCsv")
                .andReturn();

        String jsonResponse = response.getBody().prettyPrint();
        logger.info("****");
        logger.info(response.getStatusLine());
        logger.info(jsonResponse);
        logger.info("****");
        assertEquals(200, response.getStatusCode());

        //assert the body
        JsonObject ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
        assertNotNull(ingestedData.get("dataLakeId"));
        int statusCode = response.getStatusCode();
        assertEquals(200, statusCode);
    }

    @Test
    public void testMapCsvSourceDataWithoutHeaderForMLModel() throws Exception
    {
        String spaceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "dataScience/saturn_data_train.csv"),
                StandardCharsets.UTF_8);
        JsonObject input = new JsonObject();
        input.addProperty("sourceSchema", "");
        input.addProperty("destinationSchema", "");
        input.addProperty("sourceData", spaceData);
        input.addProperty("hasHeader", false);
        input.addProperty("entity","person");
        Response response = given().body(input.toString()).when().post("/dataMapper/mapCsv")
                .andReturn();

        String jsonResponse = response.getBody().prettyPrint();
        logger.info("****");
        logger.info(response.getStatusLine());
        logger.info(jsonResponse);
        logger.info("****");
        assertEquals(200, response.getStatusCode());

        //assert the body
        JsonObject ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
        assertNotNull(ingestedData.get("dataLakeId"));
        int statusCode = response.getStatusCode();
        assertEquals(200, statusCode);
    }

    @Test
    public void testMapCsvSourceDataWithHeaderForMLModel() throws Exception
    {
        String spaceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "dataScience/saturn_data_train_with_header.csv"),
                StandardCharsets.UTF_8);
        JsonObject input = new JsonObject();
        input.addProperty("sourceSchema", "");
        input.addProperty("destinationSchema", "");
        input.addProperty("sourceData", spaceData);
        input.addProperty("hasHeader", true);
        input.addProperty("entity","person");
        Response response = given().body(input.toString()).when().post("/dataMapper/mapCsv")
                .andReturn();

        String jsonResponse = response.getBody().prettyPrint();
        logger.info("****");
        logger.info(response.getStatusLine());
        logger.info(jsonResponse);
        logger.info("****");
        assertEquals(200, response.getStatusCode());

        //assert the body
        JsonObject ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
        assertNotNull(ingestedData.get("dataLakeId"));
        int statusCode = response.getStatusCode();
        assertEquals(200, statusCode);
    }

    @Test
    public void testMapXmlSourceData() throws Exception {
        String xml = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("dataMapper/people.xml"),
                StandardCharsets.UTF_8);

        JsonObject input = new JsonObject();
        input.addProperty("sourceSchema", xml);
        input.addProperty("destinationSchema", xml);
        input.addProperty("sourceData", xml);
        input.addProperty("entity","person");


        Response response = given().body(input.toString()).when().post("/dataMapper/mapXml/")
                .andReturn();

        String jsonResponse = response.getBody().prettyPrint();
        logger.info("****");
        logger.info(response.getStatusLine());
        //logger.info(jsonResponse);
        logger.info("****");
        assertEquals(200, response.getStatusCode());

        //assert the body
        JsonObject ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
        assertNotNull(ingestedData.get("dataLakeId"));
    }

    @Test
    public void testEndToEndQueryByTraversal() throws Exception {
        String json = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("query/person.json"),
                StandardCharsets.UTF_8);

        JsonObject input = new JsonObject();
        input.addProperty("sourceData", json);
        input.addProperty("entity","person");

        logger.info(input.toString());


        Response response = given().body(input.toString()).when().post("/dataMapper/map/")
                .andReturn();

        String jsonResponse = response.getBody().prettyPrint();
        logger.info("****");
        logger.info(response.getStatusLine());
        logger.info(jsonResponse);
        logger.info("****");
        assertEquals(200, response.getStatusCode());

        //assert the body
        JsonObject ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
        assertNotNull(ingestedData.get("dataLakeId"));
        logger.info("DataLakeId: "+ingestedData.get("dataLakeId"));
    }
}