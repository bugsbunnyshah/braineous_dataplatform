package com.appgallabs.dataplatform.history.service;

import test.components.BaseTest;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class DataReplayServiceTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(DataReplayServiceTests.class);

    @Inject
    private DataReplayService dataReplayService;

    @Test
    public void testDiffChainProcess() throws Exception
    {
        String email0 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email0.json"),
                StandardCharsets.UTF_8);

        String email1 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email1.json"),
                StandardCharsets.UTF_8);

        String email2 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email2.json"),
                StandardCharsets.UTF_8);

        JsonObject top = JsonParser.parseString(email0).getAsJsonObject();
        JsonObject middle = JsonParser.parseString(email1).getAsJsonObject();
        JsonObject next = JsonParser.parseString(email2).getAsJsonObject();

        Random random = new Random();
        long modelId = random.nextLong();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("modelId",modelId);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(top);
        jsonArray.add(middle);
        jsonArray.add(next);
        jsonObject.add("payload",jsonArray);

        JsonUtil.print(jsonArray);
        String chainId = this.dataReplayService.generateDiffChain(jsonArray.get(0).getAsJsonObject());
        logger.info("************************");
        logger.info("ChainId: " + chainId);
        int size = jsonArray.size();
        for(int i=1;i<size;i++) {
            this.dataReplayService.addToDiffChain(chainId,jsonArray.get(i).getAsJsonObject());
        }

        //Assert
        List<JsonObject> diffChain = this.dataReplayService.replayDiffChain(chainId);
        logger.info("********REPLAY_CHAIN****************");
        JsonUtil.print(JsonParser.parseString(diffChain.toString()));
        logger.info("************************");
    }

    @Test
    public void testGenerateDiffChainRealData() throws Exception
    {
        String spaceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "dataMapper/data.csv"),
                StandardCharsets.UTF_8);

        String[] lines = spaceData.split("\n");
        String header = lines[0];
        String[] columns = header.split(",");
        JsonArray array = new JsonArray();
        int length = lines.length;
        for(int i=1; i<length; i++)
        {
            String line = lines[i];
            String[] data = line.split(",");
            JsonObject jsonObject = new JsonObject();
            for(int j=0; j<data.length; j++)
            {
                jsonObject.addProperty(columns[j],data[j]);
            }
            array.add(jsonObject);
        }

        Random random = new Random();
        long modelId = random.nextLong();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("modelId",modelId);
        jsonObject.add("array",array);
        String chainId = this.dataReplayService.generateDiffChain(jsonObject);
        logger.info("************************");
        logger.info("ChainId: "+chainId);
        logger.info("************************");

        //Assert
        List<JsonObject> diffChain = this.dataReplayService.replayDiffChain(chainId);
        assertNotNull(diffChain);
        logger.info("************************");
        logger.info(diffChain.toString());
        logger.info("************************");
    }
}
