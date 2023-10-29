package com.appgallabs.dataplatform.query.graphql.service;


import com.appgallabs.dataplatform.TempConstants;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.BaseTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.inject.Inject;

@QuarkusTest
public class QueryExecutorTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(QueryExecutorTests.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private QueryExecutor queryExecutor;

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

    @Test
    public void executeQueryNoCriteria() throws Exception{
        String entity = TempConstants.ENTITY;

        String querySql = "query findTeas{\n" +
                "  teas{\n" +
                "    name\n" +
                "    value\n" +
                "    diff\n" +
                "  }\n" +
                "}";

        JsonArray result = this.queryExecutor.executeQueryNoCriteria(entity, querySql);
        JsonUtil.printStdOut(result);

        //TODO assert (CR1)
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    public void executeQueryByANDCriteria() throws Exception{
        String entity = TempConstants.ENTITY;

        String querySql = "query findTeas{\n" +
                "  teas{\n" +
                "    name\n" +
                "    value\n" +
                "    diff\n" +
                "  }\n" +
                "}";

        JsonArray result = this.queryExecutor.executeQueryByANDCriteria(entity, querySql);
        JsonUtil.printStdOut(result);

        //TODO assert (CR1)
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void executeQueryByORCriteria() throws Exception{
        String entity = TempConstants.ENTITY;

        String querySql = "query findTeas{\n" +
                "  teas{\n" +
                "    name\n" +
                "    value\n" +
                "    diff\n" +
                "  }\n" +
                "}";

        JsonArray result = this.queryExecutor.executeQueryByORCriteria(entity, querySql);
        JsonUtil.printStdOut(result);

        //TODO assert (CR1)
        assertNotNull(result);
        assertEquals(3, result.size());
    }
}
