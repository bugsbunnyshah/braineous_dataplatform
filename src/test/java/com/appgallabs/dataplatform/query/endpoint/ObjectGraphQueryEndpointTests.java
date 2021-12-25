package com.appgallabs.dataplatform.query.endpoint;

import com.appgallabs.dataplatform.query.GraphData;
import com.appgallabs.dataplatform.query.LocalGraphData;
import com.appgallabs.dataplatform.query.ObjectGraphQueryService;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.tinkerpop.gremlin.sparql.process.traversal.dsl.sparql.SparqlTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class ObjectGraphQueryEndpointTests {
    private static Logger logger = LoggerFactory.getLogger(ObjectGraphQueryEndpointTests.class);

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

        JsonObject json = new JsonObject();
        json.addProperty("entity","airport");
        JsonObject criteria = new JsonObject();
        criteria.addProperty("size", 100);
        json.add("criteria",criteria);

        Response response = given().body(json.toString()).when().post("/graph/query/criteria").andReturn();
        String jsonString = response.getBody().print();
        JsonArray responseJson = JsonParser.parseString(jsonString).getAsJsonArray();
        assertEquals(200, response.getStatusCode());
        JsonUtil.print(responseJson);
        assertEquals(1, responseJson.size());
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

        JsonObject json = new JsonObject();
        json.addProperty("entity","flight");
        json.addProperty("relationship", "departure");
        JsonObject departureCriteria = new JsonObject();
        departureCriteria.addProperty("code","aus");
        json.add("criteria",departureCriteria);

        Response response = given().body(json.toString()).when().post("/graph/query/navigate").andReturn();
        String jsonString = response.getBody().print();
        JsonArray responseJson = JsonParser.parseString(jsonString).getAsJsonArray();
        assertEquals(200, response.getStatusCode());
        JsonUtil.print(responseJson);
        assertEquals(1, responseJson.size());
    }
}
