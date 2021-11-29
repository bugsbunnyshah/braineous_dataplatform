package com.appgallabs.dataplatform.ingestion.service;

import com.appgallabs.dataplatform.util.BackgroundProcessListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.IngesterTest;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

@QuarkusTest
public class MapperServiceSimpleSourceTests extends IngesterTest{
    private static Logger logger = LoggerFactory.getLogger(MapperServiceSimpleSourceTests.class);

    @Inject
    private MapperService mapperService;

    @Test
    public void testSimpleSource() throws Exception
    {
        String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "dataMapper/sourceData.json"),
                StandardCharsets.UTF_8);
        JsonArray jsonArray = JsonParser.parseString(sourceData).getAsJsonArray();
        JsonObject result = this.mapperService.map("testEntity",jsonArray);
        System.out.println(result);

        System.out.println("*****WAITING********");
        synchronized (BackgroundProcessListener.getInstance().getReceiver()) {
            BackgroundProcessListener.getInstance().getReceiver().wait();
        }

        System.out.println("*****TERMINATING********");
        System.out.println(BackgroundProcessListener.getInstance().getReceiver().getData());
    }
}