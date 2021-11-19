package com.appgallabs.dataplatform.history;

import com.appgallabs.dataplatform.history.service.DataReplayService;
import test.components.BaseTest;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
public class ObjectDiffAlgorithmTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(ObjectDiffAlgorithmTests.class);

    private static Map<String, Object> diff = new HashMap<>();

    @Inject
    private ObjectDiffAlgorithm objectDiffAlgorithm;

    @Inject
    private DataReplayService dataReplayService;

    @Test
    public void testFlattening() throws Exception
    {
        String json = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("dataLake/email.json"),
                StandardCharsets.UTF_8);
        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
        String flat = flattenJson.toString();

        logger.info("String: "+json);
        logger.info("Map: "+flat);

        JsonObject jsonObject = JsonParser.parseString(JsonUnflattener.unflatten(flat)).getAsJsonObject();
        logger.info("JSON: "+jsonObject.toString());
    }

    @Test
    public void testDiff() throws Exception
    {
        logger.info("****************");

        String email0 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/email0.json"),
                StandardCharsets.UTF_8);

        String email1 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/email1.json"),
                StandardCharsets.UTF_8);

        //JsonObject left = new JsonObject();
        JsonObject left = JsonParser.parseString(email0).getAsJsonObject();
        //left.add("object",JsonParser.parseString(email0).getAsJsonObject());
        logger.info("LEFT: "+left.toString());

        //JsonObject right = new JsonObject();
        JsonObject right = JsonParser.parseString(email1).getAsJsonObject();
        //right.add("object",JsonParser.parseString(email1).getAsJsonObject());
        logger.info("RIGHT: "+right.toString());

        JsonObject diff = this.objectDiffAlgorithm.diff(left, right);
        logger.info("DIFF: "+diff.toString());

        //assert
        assertEquals("5129151162", diff.getAsJsonObject("profile").get("mobile").getAsString());
    }

    @Test
    public void testDiffChain() throws Exception
    {
        logger.info("****************");

        String email0 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email0.json"),
                StandardCharsets.UTF_8);

        String email1 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email1.json"),
                StandardCharsets.UTF_8);

        String email2 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email2.json"),
                StandardCharsets.UTF_8);

        //First Payload
        JsonObject top = JsonParser.parseString(email0).getAsJsonObject();
        JsonObject next = JsonParser.parseString(email1).getAsJsonObject();
        logger.info("TOP: "+top.toString());
        logger.info("NEXT: "+next.toString());
        JsonObject diff = this.objectDiffAlgorithm.diff(top, next);
        logger.info("DIFF: "+diff.toString());

        logger.info("****************");

        //Next Payload
        top =  next;
        next = JsonParser.parseString(email2).getAsJsonObject();
        logger.info("TOP: "+top.toString());
        logger.info("NEXT: "+next.toString());
        diff = objectDiffAlgorithm.diff(top, next);
        logger.info("DIFF: "+diff.toString());
    }

    @Test
    public void testDiffReplay() throws Exception
    {
        LinkedList<JsonObject> chain = new LinkedList<>();
        LinkedList<JsonObject> incomingData = new LinkedList<>();

        String email0 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email0.json"),
                StandardCharsets.UTF_8);

        String email1 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email1.json"),
                StandardCharsets.UTF_8);

        String email2 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email2.json"),
                StandardCharsets.UTF_8);

        //Payloads
        JsonObject top = JsonParser.parseString(email0).getAsJsonObject();
        JsonObject middle = JsonParser.parseString(email1).getAsJsonObject();
        JsonObject next = JsonParser.parseString(email2).getAsJsonObject();

        //Populate the chain
        incomingData.add(top);
        incomingData.add(middle);
        incomingData.add(next);
        logger.info("*******PAYLOADS*********");
        logger.info(top.toString());
        logger.info(middle.toString());
        logger.info(next.toString());
        logger.info("****************");

        //Calculate Diffs
        JsonObject diff0 = this.objectDiffAlgorithm.diff(top, middle);

        //Next Payload
        JsonObject diff1 = this.objectDiffAlgorithm.diff(middle, next);

        //Populate the chain
        chain.add(diff0);
        chain.add(diff1);
        logger.info("******DIIF_CHAIN**********");
        logger.info(chain.toString());
        logger.info("****************");

        //Re-construct the first payload
        JsonObject currentPayload = incomingData.getFirst();
        logger.info("******FIRST_PAYLOAD**********");
        logger.info(currentPayload.toString());
        logger.info("****************");

        //Re-construct the second payload
        JsonObject nextDiff = chain.getFirst();
        currentPayload = this.objectDiffAlgorithm.merge(currentPayload, nextDiff);
        logger.info("******SECOND_PAYLOAD**********");
        logger.info(currentPayload.toString());
        logger.info("****************");

        //Re-construct the third payload
        nextDiff = chain.get(1);
        currentPayload = this.objectDiffAlgorithm.merge(currentPayload, nextDiff);
        logger.info("******THIRD_PAYLOAD**********");
        logger.info(currentPayload.toString());
        logger.info("****************");
    }

    @Test
    public void testPayloadReplay() throws Exception
    {
        logger.info("****************");

        String email0 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/email0.json"),
                StandardCharsets.UTF_8);

        String email1 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email1.json"),
                StandardCharsets.UTF_8);

        String email2 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email2.json"),
                StandardCharsets.UTF_8);

        Random random = new Random();
        long modelId = random.nextLong();

        JsonObject top = new JsonObject();
        top.addProperty("modelId",modelId);
        top.add("payload", JsonParser.parseString(email0).getAsJsonObject());


        JsonObject middle = new JsonObject();
        middle.addProperty("modelId",modelId);
        middle.add("payload", JsonParser.parseString(email1).getAsJsonObject());

        JsonObject next = new JsonObject();
        next.addProperty("modelId",modelId);
        next.add("payload", JsonParser.parseString(email2).getAsJsonObject());

        String chainId = this.dataReplayService.generateDiffChain(top);
        this.dataReplayService.addToDiffChain(chainId, middle);
        this.dataReplayService.addToDiffChain(chainId, next);

        List<JsonObject> diffChain = this.dataReplayService.replayDiffChain(chainId);
        logger.info("****************************");
        logger.info("ChainId: "+ chainId);
        logger.info("ChainId: "+ diffChain.toString());
        logger.info("****************************");
    }

    @Test
    public void testDiffReal() throws Exception
    {
        logger.info("****************");

        String flight0 = "{\n" +
                "  \"flight\": {\n" +
                "    \"number\": \"204\",\n" +
                "    \"iata\": \"LM204\",\n" +
                "    \"icao\": \"LOG204\"\n" +
                "  },\n" +
                "  \"departure\": {\n" +
                "    \"airport\": \"Donegal\",\n" +
                "    \"timezone\": \"Europe/Dublin\",\n" +
                "    \"iata\": \"CFN\",\n" +
                "    \"icao\": \"EIDL\",\n" +
                "    \"scheduled\": \"2020-09-25T12:05:00+00:00\",\n" +
                "    \"estimated\": \"2020-09-25T12:05:00+00:00\"\n" +
                "  },\n" +
                "  \"arrival\": {\n" +
                "    \"airport\": \"Glasgow International\",\n" +
                "    \"timezone\": \"Europe/London\",\n" +
                "    \"iata\": \"GLA\",\n" +
                "    \"icao\": \"EGPF\",\n" +
                "    \"terminal\": \"M\",\n" +
                "    \"scheduled\": \"2020-09-25T13:05:00+00:00\",\n" +
                "    \"estimated\": \"2020-09-25T13:05:00+00:00\"\n" +
                "  },\n" +
                "  \"flight_status\": \"scheduled\",\n" +
                "  \"index\": 0,\n" +
                "  \"flight_date\": \"2020-09-25\",\n" +
                "  \"airline\": {\n" +
                "    \"name\": \"Loganair\",\n" +
                "    \"iata\": \"LM\",\n" +
                "    \"icao\": \"LOG\"\n" +
                "  },\n" +
                "  \"chainId\": \"/-2061008798/e35b8ba8-b269-481d-832a-05fee9832930\",\n" +
                "  \"braineous_datalakeid\": \"e35b8ba8-b269-481d-832a-05fee9832930\"\n" +
                "}";

        String flight1 = "{\n" +
                "  \"airline\": {\n" +
                "    \"name\": \"British Airways\",\n" +
                "    \"iata\": \"BA\",\n" +
                "    \"icao\": \"BAW\"\n" +
                "  },\n" +
                "  \"departure\": {\n" +
                "    \"airport\": \"Donegal\",\n" +
                "    \"timezone\": \"Europe/Dublin\",\n" +
                "    \"iata\": \"CFN\",\n" +
                "    \"icao\": \"EIDL\",\n" +
                "    \"scheduled\": \"2020-09-25T12:05:00+00:00\",\n" +
                "    \"estimated\": \"2020-09-25T12:05:00+00:00\"\n" +
                "  },\n" +
                "  \"flight\": {\n" +
                "    \"number\": \"4086\",\n" +
                "    \"iata\": \"BA4086\",\n" +
                "    \"icao\": \"BAW4086\",\n" +
                "    \"codeshared\": {\n" +
                "      \"airline_name\": \"loganair\",\n" +
                "      \"airline_iata\": \"lm\",\n" +
                "      \"airline_icao\": \"log\",\n" +
                "      \"flight_number\": \"204\",\n" +
                "      \"flight_iata\": \"lm204\",\n" +
                "      \"flight_icao\": \"log204\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"arrival\": {\n" +
                "    \"airport\": \"Glasgow International\",\n" +
                "    \"timezone\": \"Europe/London\",\n" +
                "    \"iata\": \"GLA\",\n" +
                "    \"icao\": \"EGPF\",\n" +
                "    \"terminal\": \"M\",\n" +
                "    \"scheduled\": \"2020-09-25T13:05:00+00:00\",\n" +
                "    \"estimated\": \"2020-09-25T13:05:00+00:00\"\n" +
                "  },\n" +
                "  \"braineous_datalakeid\": \"e35b8ba8-b269-481d-832a-05fee9832930\",\n" +
                "  \"chainId\": \"/-2061008798/e35b8ba8-b269-481d-832a-05fee9832930\",\n" +
                "  \"flight_date\": \"2020-09-25\",\n" +
                "  \"flight_status\": \"scheduled\"\n" +
                "}";

        flight0 = "{\"flight_date\": \"2020-09-25\",\n" +
                "    \"index\": \"0\",\n" +
                "  \"flight\": {\n" +
                "    \"number\": \"204\",\n" +
                "    \"iata\": \"LM204\",\n" +
                "    \"icao\": \"LOG204\"\n" +
                "  },\n" +
                "    \"flight_status\": \"scheduled\"}";
        flight1 = "{\"flight\": {\n" +
                "    \"number\": \"4086\",\n" +
                "    \"iata\": \"BA4086\",\n" +
                "    \"icao\": \"BAW4086\",\n" +
                "    \"codeshared\": {\n" +
                "      \"airline_name\": \"loganair\",\n" +
                "      \"airline_iata\": \"lm\",\n" +
                "      \"airline_icao\": \"log\",\n" +
                "      \"flight_number\": \"204\",\n" +
                "      \"flight_iata\": \"lm204\",\n" +
                "      \"flight_icao\": \"log204\"\n" +
                "    }\n" +
                "  }," +
                "  \"flight_date\": \"2020-09-25\",\n" +
                "  \"flight_status\": \"scheduled\"\n" +
                "}";

        //JsonObject left = new JsonObject();
        //System.out.println(flight0);
        JsonObject left = JsonParser.parseString(flight0).getAsJsonObject();
        //left.add("object",JsonParser.parseString(email0).getAsJsonObject());
        JsonUtil.print(left);

        //JsonObject right = new JsonObject();
        //System.out.println(flight1);
        JsonObject right = JsonParser.parseString(flight1).getAsJsonObject();
        //right.add("object",JsonParser.parseString(email1).getAsJsonObject());
        JsonUtil.print(right);

        logger.info("************DIFF***********");

        JsonObject diff = this.objectDiffAlgorithm.diff(left, right);
        JsonUtil.print(diff);

        JsonObject merge = this.objectDiffAlgorithm.merge(left,diff);
        JsonUtil.print(merge);

        //assert
        //assertEquals("5129151162", diff.getAsJsonObject("profile").get("mobile").getAsString());
    }
}
