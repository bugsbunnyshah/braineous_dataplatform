package com.appgallabs.dataplatform.ingestion.service;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.query.ObjectGraphQueryService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.BaseTest;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@QuarkusTest
public class StreamIngesterContextTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(StreamIngesterContextTests.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private ObjectGraphQueryService queryService;

    @Test
    public void ingestData() throws Exception{
        StreamIngesterContext streamIngesterContext = StreamIngesterContext.getStreamIngesterContext();
        streamIngesterContext.setQueryService(this.queryService);
        streamIngesterContext.setSecurityTokenContainer(this.securityTokenContainer);
        streamIngesterContext.setMongoDBJsonStore(this.mongoDBJsonStore);

        Tenant tenant = this.securityTokenContainer.getTenant();
        String entity = "test0_flight";
        String principal = tenant.getPrincipal();
        String dataLakeId = UUID.randomUUID().toString();
        String chainId = "/" + tenant.getPrincipal() + "/" + dataLakeId;

        String jsonString = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("aviation/flight.json"),
                StandardCharsets.UTF_8);
        System.out.println(jsonString);
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

        streamIngesterContext.ingestOnThread(principal,entity,dataLakeId,chainId,json);
    }
}
