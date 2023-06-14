package com.appgallabs.dataplatform.infrastructure;

import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import test.components.BaseTest;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.*;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class MongoDBJsonStoreTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(MongoDBJsonStoreTests.class);

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;


    @AfterEach
    public void tearDown()
    {

    }

    @Test
    public void testStoreDataSetRealDataForEval() throws Exception
    {
        String csv = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("dataScience/saturn_data_eval.csv"),
                StandardCharsets.UTF_8);
        //logger.info(csv);

        JsonObject dataSetJson = new JsonObject();
        dataSetJson.addProperty("format","csv");
        dataSetJson.addProperty("data", csv);
        String oid = this.mongoDBJsonStore.storeTrainingDataSet(this.securityTokenContainer.getTenant(),dataSetJson);
        logger.info("Lookup DataSetId: "+oid);

        JsonObject dataSet = this.mongoDBJsonStore.readDataSet(this.securityTokenContainer.getTenant(),oid);
        String csvData = dataSet.get("data").getAsString();
        String storedOid = dataSet.get("dataSetId").getAsString();
        logger.info(""+storedOid);
        assertEquals(oid, storedOid);
        assertEquals(csv, csvData);
    }

    //TODO
    /*@Test
    public void testRollOverToTraningDataSets() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject liveModelDeployedJson = this.packagingService.performPackagingForLiveDeployment(modelPackage);
        String modelId = liveModelDeployedJson.get("modelId").getAsString();

        String data = IOUtils.resourceToString("dataScience/saturn_data_eval.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject input = new JsonObject();
        input.addProperty("modelId", modelId);
        input.addProperty("format", "csv");
        input.addProperty("data", data);
        Response response = given().body(input.toString()).when().post("/dataset/storeEvalDataSet/").andReturn();
        logger.info("************************");
        logger.info("ModelId: "+modelId);
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        String dataSetId = JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataSetId").getAsString();


        JsonObject rolledOverDataSetIds = this.mongoDBJsonStore.rollOverToTraningDataSets(this.securityTokenContainer.getTenant(),
                modelId);
        logger.info(rolledOverDataSetIds.toString());

        //Assert
        List<String> dataSetIds = new ArrayList<>();
        JsonArray array = rolledOverDataSetIds.getAsJsonArray("rolledOverDataSetIds");
        Iterator<JsonElement> iterator = array.iterator();
        while(iterator.hasNext())
        {
            dataSetIds.add(iterator.next().getAsString());
        }
        assertTrue(dataSetIds.contains(dataSetId));
    }*/

    @Test
    public void testIngestion() throws Exception
    {
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
        JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
        String tenant = json.get("tenant").getAsString();

        Tenant cour = new Tenant();
        cour.setPrincipal(tenant);
        JsonArray metaData = this.mongoDBJsonStore.getIngestedDataSetsMetaData(cour);
        JsonUtil.print(metaData);
    }

    @Test
    public void readByEntity() throws Exception{
        String entity = "flight";
        JsonArray data = this.mongoDBJsonStore.readByEntity(this.securityTokenContainer.getTenant(),entity);
    }

    @Test
    public void readEntity() throws Exception{
        JsonObject jsonObject = new JsonObject();
        String objectHash = JsonUtil.getJsonHash(jsonObject);
        jsonObject.addProperty("objectHash",objectHash);
        this.mongoDBJsonStore.storeIngestion(this.securityTokenContainer.getTenant(),jsonObject);
        Tenant tenant = this.securityTokenContainer.getTenant();
        JsonObject data = this.mongoDBJsonStore.readEntity(tenant,objectHash);
        assertNotNull(data);
        assertEquals(objectHash,data.get("objectHash").getAsString());
    }
}
