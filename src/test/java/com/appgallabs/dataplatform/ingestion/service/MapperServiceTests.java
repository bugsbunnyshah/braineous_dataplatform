package com.appgallabs.dataplatform.ingestion.service;

import com.appgallabs.dataplatform.query.ObjectGraphQueryService;
import com.appgallabs.dataplatform.util.BackgroundProcessListener;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.IngesterTest;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class MapperServiceTests extends IngesterTest{
    private static Logger logger = LoggerFactory.getLogger(MapperServiceTests.class);

    @Inject
    private MapperService mapperService;

    @Inject
    private ObjectGraphQueryService queryService;

    //@Test
    public void testMapAirlineData() throws Exception
    {
        String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "aviation/flights_small.json"),
                StandardCharsets.UTF_8);
        JsonObject json = JsonParser.parseString(sourceData).getAsJsonObject();
        JsonArray array = json.get("data").getAsJsonArray();

        BackgroundProcessListener.getInstance().setThreshold(array.size());


        JsonObject result = this.mapperService.map("flight",array);
        System.out.println(result);

        System.out.println("*****WAITING********");
        synchronized (BackgroundProcessListener.getInstance().getReceiver()) {
            BackgroundProcessListener.getInstance().getReceiver().wait();
        }

        System.out.println("*****TERMINATING********");
        System.out.println(BackgroundProcessListener.getInstance().getReceiver().getData());

        //assert ObjectGraph
        JsonObject departureCriteria = new JsonObject();
        departureCriteria.addProperty("airport","Auckland International");
        array = this.queryService.navigateByCriteria("flight",
                "departure",departureCriteria);
        JsonUtil.printStdOut(array);
        assertEquals(5, array.size());
    }
}