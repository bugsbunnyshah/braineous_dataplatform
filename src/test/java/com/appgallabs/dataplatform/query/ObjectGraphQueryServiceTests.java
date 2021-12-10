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
        JsonObject ausJson = new JsonObject();
        ausJson.addProperty("code","aus");
        ausJson.addProperty("description", "AUS");
        ausJson.addProperty("size", 100);
        Vertex ausV = this.service.saveObjectGraph("airport",ausJson,null,false);
        System.out.println(ausV.graph());

        JsonObject criteria = new JsonObject();
        criteria.addProperty("size", 100);
        //criteria.addProperty("code", "aus");

        JsonArray array = service.queryByCriteria("airport", criteria);
        System.out.println(array);
        assertTrue(array.size()> 0);
    }

    @Test
    public void navigateByCriteria() throws Exception
    {
        JsonObject ausJson = new JsonObject();
        ausJson.addProperty("code","aus");
        ausJson.addProperty("description", "AUS");
        ausJson.addProperty("size", 100);

        JsonObject laxJson = new JsonObject();
        laxJson.addProperty("code","lax");
        laxJson.addProperty("description", "LAX");
        laxJson.addProperty("size", 150);

        JsonObject flight = new JsonObject();
        flight.addProperty("flightId","123");
        flight.addProperty("description", "SouthWest");
        flight.add("departure", ausJson);
        flight.add("arrival", laxJson);

        JsonUtil.print(flight);

        Vertex v = this.service.saveObjectGraph("flight",flight,null,false);
        System.out.println(v.graph());

        JsonObject departureCriteria = new JsonObject();
        departureCriteria.addProperty("code","aus");
        JsonArray array = this.service.navigateByCriteria("flight",
                "departure",departureCriteria);
        JsonUtil.print(array);

        JsonObject arrivalCriteria = new JsonObject();
        arrivalCriteria.addProperty("code","lax");
        array = this.service.navigateByCriteria("flight",
                "arrival",arrivalCriteria);
        JsonUtil.print(array);
    }

    @Test
    public void queryByDeparture() throws Exception{
        String data = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                getResourceAsStream("query/flights.json"), StandardCharsets.UTF_8);

        JsonObject json = JsonParser.parseString(data).getAsJsonObject();
        JsonArray dataArray = json.get("data").getAsJsonArray();
        System.out.println("# of Flights: "+dataArray.size());

        for(int i=0; i<dataArray.size(); i++){
            JsonObject flightJson = dataArray.get(i).getAsJsonObject();
            flightJson.addProperty("oid",UUID.randomUUID().toString());
            Vertex v = this.service.saveObjectGraph("flight",flightJson,null,false);
            System.out.println(v.graph());
        }

        JsonObject departureCriteria = new JsonObject();
        departureCriteria.addProperty("airport","Auckland International");
        JsonArray array = this.service.navigateByCriteria("flight",
                "departure",departureCriteria);
        JsonUtil.print(array);
    }
}
