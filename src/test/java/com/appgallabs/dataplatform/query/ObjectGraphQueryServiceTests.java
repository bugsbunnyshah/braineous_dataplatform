package com.appgallabs.dataplatform.query;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        JsonArray array = service.queryByCriteria(entity, criteria);
        //System.out.println(array);
        //assertTrue(array.size()> 0);
    }

    //@Test
    public void navigateByCriteria() throws Exception
    {
        JsonObject ausJson = new JsonObject();
        ausJson.addProperty("code","aus");
        ausJson.addProperty("description", "AUS");
        ausJson.addProperty("size", "100");

        JsonObject laxJson = new JsonObject();
        laxJson.addProperty("code","lax");
        laxJson.addProperty("description", "LAX");
        laxJson.addProperty("size", "150");

        JsonObject flight = new JsonObject();
        flight.addProperty("flightId","123");
        flight.addProperty("description", "SouthWest");
        flight.add("departure", ausJson);
        flight.add("arrival", laxJson);
        JsonUtil.print(flight);

        JsonObject airport = new JsonObject();
        airport.addProperty("name", "Dallas");

        String left = "airline_network_airport";
        String right = "airline_network_flight";
        String relationship = "connection";
        this.service.saveObjectGraph(left,airport);
        this.service.saveObjectGraph(right,flight);
        this.service.establishRelationship(left,right, relationship);

        JsonObject departureCriteria = new JsonObject();
        departureCriteria.addProperty("code","aus");
        JsonArray array = this.service.navigateByCriteria(left,right,
                relationship,departureCriteria);
        //JsonUtil.print(array);
        //assertEquals(1,array.size());

        JsonObject arrivalCriteria = new JsonObject();
        arrivalCriteria.addProperty("code","lax");
        array = this.service.navigateByCriteria(left,right,
                relationship,arrivalCriteria);
        //JsonUtil.print(array);
        //assertEquals(1,array.size());
    }

    /*@Test
    public void navigateByCriteriaRealData() throws Exception{
        String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "aviation/flights_small.json"),
                StandardCharsets.UTF_8);
        JsonObject json = JsonParser.parseString(sourceData).getAsJsonObject();
        JsonArray array = json.get("data").getAsJsonArray();

        for(int i=0; i<array.size();i++){
            JsonObject cour = array.get(i).getAsJsonObject();
            //this.service.saveObjectGraph("flight",cour,null,false);
        }

        JsonObject departureCriteria = new JsonObject();
        departureCriteria.addProperty("airport","Auckland International");
        array = this.service.navigateByCriteria("flight",
                "departure",departureCriteria);
        JsonUtil.print(array);
        assertEquals(5, array.size());
    }*/
}
