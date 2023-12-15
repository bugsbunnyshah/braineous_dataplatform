package com.appgallabs.dataplatform.client.sdk.api;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.BaseTest;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class DataLakeGraphQlQueryTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(DataLakeGraphQlQueryTests.class);

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

    //TODO: solidify: (CR1)
    //@Test
    public void sendQuery() throws Exception{
        //configure the DataPipeline Client
        Configuration configuration = new Configuration().
                streamSizeInBytes(80).
                ingestionHostUrl("http://localhost:8080");
        DataPipeline.configure(configuration);

        String querySql = "query findTeas{\n" +
                "  teas{\n" +
                "    name\n" +
                "    value\n" +
                "    diff\n" +
                "  }\n" +
                "}";

        for(int i=0; i<10; i++) {
            JsonArray result = DataLakeGraphQlQuery.sendQuery(querySql);
            JsonUtil.printStdOut(result);
        }

        //assertions
        //System.out.println("********************************");
        //JsonUtil.printStdOut(response);
        //assertNotNull(response);
    }
}
