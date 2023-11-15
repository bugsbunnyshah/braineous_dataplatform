package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.datalake.DataLakeDriver;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.BaseTest;
import test.components.Util;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;


@QuarkusTest
public class EventProcessorTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(EventProcessorTests.class);

    private static final long HANG_TIME =  15000l;

    @Inject
    private EventProducer eventProducer;

    @Inject
    private EventConsumer eventConsumer;

    @Inject
    private Instance<DataLakeDriver> dataLakeDriverInstance;

    private String dataLakeDriverName;
    private DataLakeDriver dataLakeDriver;

    @Inject
    private SecurityTokenContainer securityTokenContainer;


    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        JsonObject response = this.eventConsumer.checkStatus();
        logger.info(response.toString());

        String jsonString = Util.loadResource("pipeline/temp.json");

        Registry registry = Registry.getInstance();
        JsonObject pipeRegistration = JsonUtil.validateJson(jsonString).getAsJsonObject();
        registry.registerPipe(pipeRegistration);

        this.eventProducer.start();
        this.eventConsumer.start();
    }

    //@Test
    public void processEventWithPipeline() throws Exception {
        Tenant tenant = this.securityTokenContainer.getTenant();

        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/scenario1.json"),
                StandardCharsets.UTF_8
        );
        JsonArray jsonArray = JsonParser.parseString(jsonString).getAsJsonArray();

        JsonObject response = this.eventProducer.processEvent(jsonArray);

        logger.info("*****************");
        logger.info(response.toString());
        logger.info("*****************");

        assertNotNull(response);
        assertEquals(200, response.get("statusCode").getAsInt());

        Thread.sleep(HANG_TIME);

        //Assert storage of ingested data
        /*
        for(int index=0; index<jsonArray.size();index++) {
            JsonObject jsonObject = jsonArray.get(index).getAsJsonObject();
            String originalObjectHash = JsonUtil.getJsonHash(jsonObject);
            jsonObject.remove("expensive");
            String compareLeftObjectHash = JsonUtil.getJsonHash(jsonObject);

            JsonArray storedDataArray = this.dataLakeDriver.readIngestion(tenant, originalObjectHash);
            JsonObject storedJson = storedDataArray.get(0).getAsJsonObject();
            JsonUtil.printStdOut(storedJson);

            storedJson.remove("expensive");
            String compareRightObjectHash = JsonUtil.getJsonHash(storedJson);

            logger.info("*****LHS********");
            logger.info(compareLeftObjectHash);
            JsonUtil.printStdOut(jsonObject);
            logger.info("****************");

            logger.info("*****RHS********");
            logger.info(compareRightObjectHash);
            JsonUtil.printStdOut(storedJson);
            logger.info("****************");

            assertEquals(compareLeftObjectHash, compareRightObjectHash);
        }*/
    }
}
