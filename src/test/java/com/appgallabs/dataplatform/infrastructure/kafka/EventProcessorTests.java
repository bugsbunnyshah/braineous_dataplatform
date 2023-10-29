package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.datalake.DataLakeDriver;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.receiver.framework.Registry;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import test.components.BaseTest;
import test.components.Util;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.inject.Inject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;


@QuarkusTest
public class EventProcessorTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(EventProcessorTests.class);

    private static final long HANG_TIME =  15000l;

    @Inject
    private EventProcessor eventProcessor;

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

        Config config = ConfigProvider.getConfig();
        this.dataLakeDriverName = config.getValue("datalake_driver_name", String.class);
        this.dataLakeDriver = dataLakeDriverInstance.select(NamedLiteral.of(dataLakeDriverName)).get();

        String jsonString = Util.loadResource("receiver/mongodb_config_1.json");

        Registry registry = Registry.getInstance();
        registry.registerPipe(JsonUtil.validateJson(jsonString).getAsJsonObject());
        JsonUtil.printStdOut(JsonUtil.validateJson(registry.allRegisteredPipeIds().toString()));

        this.eventProcessor.start();
        this.eventConsumer.start();
    }

    @Test
    public void processEventWithPipeline() throws Exception {
        Tenant tenant = this.securityTokenContainer.getTenant();

        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/input_array.json"),
                StandardCharsets.UTF_8
        );
        JsonArray jsonArray = JsonParser.parseString(jsonString).getAsJsonArray();

        for(int i=0; i<10; i++) {
            JsonObject response = this.eventProcessor.processEvent(jsonArray);

            logger.info("*****************");
            logger.info(response.toString());
            logger.info("*****************");

            assertNotNull(response);
            assertEquals(200, response.get("statusCode").getAsInt());
        }

        Thread.sleep(HANG_TIME);

        //Assert storage of ingested data
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
            logger.info(compareRightObjectHash);
            logger.info("****************");

            logger.info("*****RHS********");
            logger.info(compareRightObjectHash);
            logger.info("****************");

            assertEquals(compareLeftObjectHash, compareRightObjectHash);
        }
    }
}
