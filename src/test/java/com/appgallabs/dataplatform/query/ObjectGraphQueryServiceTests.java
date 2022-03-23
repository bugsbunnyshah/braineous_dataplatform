package com.appgallabs.dataplatform.query;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


@QuarkusTest
public class ObjectGraphQueryServiceTests {
    private static Logger logger = LoggerFactory.getLogger(ObjectGraphQueryServiceTests.class);

    @Inject
    private ObjectGraphQueryService service;

    @BeforeEach
    public void setUp()
    {
        this.service.onStart();
    }

    @AfterEach
    public void tearDown(){
        this.service.onStop();
    }

    @Test
    public void queryByCriteria() throws Exception
    {
        JsonObject airport = new JsonObject();
        airport.addProperty("code","aus");
        airport.addProperty("description", "AUS");
        airport.addProperty("size", "100");

        String entity = "airline_network_airport";
        this.service.saveObjectGraph(entity,airport);

        JsonObject criteria = new JsonObject();
        criteria.addProperty("size", 100);
    }

    @Test
    public void navigateByCriteria() throws Exception
    {
        JsonObject departure = new JsonObject();
        departure.addProperty("airport", "Indira Gandhi International");
        JsonObject arrival = new JsonObject();
        arrival.addProperty("arrival", "Auckland International");

        JsonObject flight = new JsonObject();
        flight.addProperty("flightId","123");
        flight.addProperty("description", "SouthWest");
        flight.add("departure", departure);
        flight.add("arrival", arrival);

        String entity = "flight";
        this.service.saveObjectGraph(entity,flight);
    }

    @Test
    public void navigateByCriteriaRealData() throws Exception
    {
        String json = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("aviation/flightsQantasSmall.json"),
                StandardCharsets.UTF_8
        );

        JsonObject flight = JsonParser.parseString(json).getAsJsonObject();
        System.out.println(flight);
        String entity = "flight";
        this.service.saveObjectGraph(entity,flight);

        String leftEntity = "airport";
        String rightEntity = entity;
        String relationship = "departure";
        String departure = "Wellington International";
        JsonObject criteria = new JsonObject();
        criteria.addProperty("name",departure);
        List<Record> resultSet = this.service.navigateByCriteria(leftEntity,rightEntity,relationship,criteria);
        System.out.println(resultSet);
    }

    @Test
    public void testCallbackRegistry() throws Exception{
        String configJsonString = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("entityCallbacks.json"),
                StandardCharsets.UTF_8
        );
        JsonArray configJson = JsonParser.parseString(configJsonString).getAsJsonArray();

        Map<String,EntityCallback> callbackMap = new HashMap<>();
        Iterator<JsonElement> iterator = configJson.iterator();
        while(iterator.hasNext()){
            JsonObject entityConfigJson = iterator.next().getAsJsonObject();
            String entity = entityConfigJson.get("entity").getAsString();
            String callback = entityConfigJson.get("callback").getAsString();
            EntityCallback object = (EntityCallback) Thread.currentThread().getContextClassLoader().loadClass(callback).getDeclaredConstructor().newInstance();
            callbackMap.put(entity,object);
        }
        System.out.println(callbackMap);
    }
}
