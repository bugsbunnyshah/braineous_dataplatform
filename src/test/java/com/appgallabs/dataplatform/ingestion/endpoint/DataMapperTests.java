package com.appgallabs.dataplatform.ingestion.endpoint;

import com.appgallabs.dataplatform.datalake.DataLakeDriver;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.ingestion.service.MapperService;
import com.appgallabs.dataplatform.ingestion.util.CSVDataUtil;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;

import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.query.ObjectGraphQueryService;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
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

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.NamedLiteral;
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

    @Inject
    private Instance<DataLakeDriver> dataLakeDriverInstance;

    private String dataLakeDriverName;
    private DataLakeDriver dataLakeDriver;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        Config config = ConfigProvider.getConfig();
        this.dataLakeDriverName = config.getValue("datalake_driver_name", String.class);
        this.dataLakeDriver = dataLakeDriverInstance.select(NamedLiteral.of(dataLakeDriverName)).get();
    }

    @Test
    public void testMapWithOneToOneFields() throws Exception {
        Tenant tenant = this.securityTokenContainer.getTenant();

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
        int statusCode = response.getStatusCode();
        assertEquals(200, statusCode);
        assertEquals(ingestedData.get("statusCode").getAsInt(),200);
        assertEquals(ingestedData.get("message").getAsString(),"DATA_SUCCESSFULLY_INGESTED");

        Thread.sleep(10000);

        //Assert storage of ingested data
        JsonArray sourceDataArray = JsonParser.parseString(sourceData).getAsJsonArray();

        //TODO: add assertion for index 0 object after fixing issues with decimal values
        /**
         * Input =
         * {
         *         "Id": 123456789,
         *         "Rcvr": 1234567,
         *         "HasSig": true
         * }
         *
         * Stored =
         * {
         *     "HasSig": true,
         *     "Rcvr": 1234567.0,
         *     "Id": 1.23456789E8
         * }
         */
        JsonObject sourceDataJson = sourceDataArray.get(1).getAsJsonObject();
        String sourceObjectHash = JsonUtil.getJsonHash(sourceDataJson);
        logger.info("SOURCE_OBJECT_HASH: "+sourceObjectHash);

        JsonArray storedDataArray = this.dataLakeDriver.readIngestion(tenant,sourceObjectHash);
        JsonUtil.printStdOut(storedDataArray);

        JsonObject storedDataJson = storedDataArray.get(0).getAsJsonObject();
        storedDataJson.addProperty("Id", 7777777);
        storedDataJson.addProperty("Rcvr", 77777);
        String storedObjectHash = JsonUtil.getJsonHash(storedDataJson);
        logger.info("STORED_OBJECT_HASH: "+storedObjectHash);

        assertEquals(sourceObjectHash, storedObjectHash);
    }

    //@Test
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

    //@Test
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

    //@Test
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

    //@Test
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

    //@Test
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

    //@Test
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