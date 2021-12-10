package com.appgallabs.dataplatform.query;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
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
        /*JsonObject ausJson = new JsonObject();
        ausJson.addProperty("code","aus");
        ausJson.addProperty("description", "AUS");
        ausJson.addProperty("size", 100);

        JsonObject laxJson = new JsonObject();
        laxJson.addProperty("code","lax");
        laxJson.addProperty("description", "LAX");
        laxJson.addProperty("size", 1000);

        JsonObject flight = new JsonObject();
        flight.addProperty("flightId","123");
        flight.addProperty("description", "SouthWest");

        Vertex ausV = this.service.saveObjectGraph("airport",ausJson,null,false);
        System.out.println(ausV.graph());*/
        this.service.start();
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
        JsonObject departureCriteria = new JsonObject();
        departureCriteria.addProperty("code","aus");
        JsonArray array = this.service.navigateByCriteria("airport","flight",
                "departure",departureCriteria);

        JsonUtil.print(array);

        JsonObject arrivalCriteria = new JsonObject();
        arrivalCriteria.addProperty("code","lax");
        array = this.service.navigateByCriteria("airport","flight",
                "arrival",arrivalCriteria);
        JsonUtil.print(array);
    }
}
