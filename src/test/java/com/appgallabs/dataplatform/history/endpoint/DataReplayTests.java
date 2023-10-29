package com.appgallabs.dataplatform.history.endpoint;

import com.appgallabs.dataplatform.history.service.DataReplayService;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import test.components.BaseTest;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Random;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class DataReplayTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(DataReplayTests.class);

    @Inject
    private DataReplayService dataReplayService;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Test
    public void testChain() throws Exception
    {
        String json = "[{\"payload\" : { \"Id\" : 7777777, \"Rcvr\" : 77777, \"HasSig\" : true }},{\"payload\" : { \"Id\" : 7777777, \"Rcvr\" : 77777, \"HasSig\" : false }}]";
        JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();
        JsonObject modelChain = new JsonObject();
        Random random = new Random();
        modelChain.addProperty("modelId", random.nextLong());
        modelChain.add("payload",jsonArray);
        String oid = this.dataReplayService.generateDiffChain(modelChain);
        Tenant tenant = this.securityTokenContainer.getTenant();
        List<JsonObject> diffChain = this.mongoDBJsonStore.readDiffChain(tenant,oid);
        logger.info("CHAIN_ID: "+oid);
        JsonUtil.print(JsonParser.parseString(diffChain.toString()));

        Response response = given().get("/replay/chain/?oid="+oid).andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        //TODO (5.0)
        //assertEquals(200, response.getStatusCode());
    }
}