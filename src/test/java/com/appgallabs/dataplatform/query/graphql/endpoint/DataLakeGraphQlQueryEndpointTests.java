package com.appgallabs.dataplatform.query.graphql.endpoint;

import com.appgallabs.dataplatform.TestConstants;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.BaseTest;

import javax.inject.Inject;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class DataLakeGraphQlQueryEndpointTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(DataLakeGraphQlQueryEndpointTests.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @BeforeEach
    public void setUp() throws Exception{
        super.setUp();
        for(int i=0; i<3; i++) {
            JsonObject jsonObject = new JsonObject();
            String objectHash = JsonUtil.getJsonHash(jsonObject);
            jsonObject.addProperty("objectHash", objectHash);
            jsonObject.addProperty("name", "hello");
            jsonObject.addProperty("value","value");
            jsonObject.addProperty("diff",""+i);
            this.mongoDBJsonStore.storeIngestion(this.securityTokenContainer.getTenant(), jsonObject);
            Tenant tenant = this.securityTokenContainer.getTenant();
            JsonObject data = this.mongoDBJsonStore.readEntity(tenant, objectHash);
            assertNotNull(data);
            assertEquals(objectHash, data.get("objectHash").getAsString());
        }
    }

    //@Test
    public void query() throws Exception {
        //get OAuth Token
        String credentials = IOUtils.resourceToString("oauth/credentials.json",
                StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject credentialsJson = JsonParser.parseString(credentials).getAsJsonObject();
        String tenant = credentialsJson.get("client_id").getAsString();

        String token = IOUtils.resourceToString("oauth/jwtToken.json",
                StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject securityTokenJson = JsonParser.parseString(token).getAsJsonObject();
        String generatedToken = securityTokenJson.get("access_token").getAsString();

        String entity = TestConstants.ENTITY;

        String graphqlQuery = "query findTeas{\n" +
                "  teas{\n" +
                "    name\n" +
                "    value\n" +
                "    diff\n" +
                "  }\n" +
                "}";

        JsonObject input = new JsonObject();
        input.addProperty("graphqlQuery", graphqlQuery);

        Response response = given().body(input.toString()).when()
                .header("tenant", tenant)
                .header("token", "Bearer "+generatedToken)
                .post("/data/lake/query")
                .andReturn();

        response.getBody().prettyPrint();
        int statusCode = response.statusCode();
        logger.info("**************");
        logger.info(response.getStatusLine());
        logger.info("***************");

        //TODO: (CR1)
        assertEquals(200, statusCode);
    }
}
