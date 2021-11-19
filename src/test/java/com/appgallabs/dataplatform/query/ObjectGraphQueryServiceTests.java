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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@QuarkusTest
public class ObjectGraphQueryServiceTests {
    private static Logger logger = LoggerFactory.getLogger(ObjectGraphQueryServiceTests.class);

    @Inject
    private ObjectGraphQueryService service;

    private Graph graph;

    @BeforeEach
    public void setUp()
    {
        this.graph = TinkerGraph.open();

        JsonObject ausJson = new JsonObject();
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



        final Vertex aus = this.graph.addVertex(T.id, 1, T.label, "airport", "code", "aus",
                "description", "AUS", "size", 100 ,
                "source", ausJson.toString());
        final Vertex lax = this.graph.addVertex(T.id, 2, T.label, "airport", "code", "lax",
                "description", "LAX", "size", 1000,
                "source", laxJson.toString());
        final Vertex ausToLax = this.graph.addVertex(T.id, 3, T.label, "flight", "flightId", "123", "description", "SouthWest",
                "source",flight.toString());
        aus.addEdge("departure", ausToLax, T.id, 4, "weight", 0.5d);
        lax.addEdge("arrival",ausToLax,T.id, 5, "weight", 0.5d);

        SparqlTraversalSource server = new SparqlTraversalSource(this.graph);
        GraphData graphData = new LocalGraphData(server);
        this.service.setGraphData(graphData);
    }

    @Test
    public void queryByCriteria() throws Exception
    {
        JsonObject criteria = new JsonObject();
        criteria.addProperty("size", 100);
        //criteria.addProperty("code", "aus");

        JsonArray array = service.queryByCriteria("airport", criteria);
        JsonUtil.print(array);
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
