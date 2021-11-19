package com.appgallabs.dataplatform.query;

import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.sparql.process.traversal.dsl.sparql.SparqlTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class GraphQueryGeneratorTests {
    private static Logger logger = LoggerFactory.getLogger(GraphQueryGenerator.class);

    @Inject
    private GraphQueryGenerator graphQueryGenerator;

    @Inject
    private GraphQueryProcessor graphQueryProcessor;

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
    }

    @Test
    public void generateQueryByCriteria() throws Exception
    {
        Graph tinkerGraph = TinkerGraph.open();
        final Vertex aus = tinkerGraph.addVertex(T.id, 1, T.label, "airport", "code", "aus", "description", "AUS", "size", 100);
        final Vertex lax = tinkerGraph.addVertex(T.id, 2, T.label, "airport", "code", "lax", "description", "LAX", "size", 1000);

        this.criteria1(tinkerGraph);
        this.criteria2(tinkerGraph);
    }

    @Test
    public void generateNavigationQuery() throws Exception
    {
        Graph tinkerGraph = TinkerGraph.open();
        final Vertex aus = tinkerGraph.addVertex(T.id, 1, T.label, "airport", "code", "aus", "description", "AUS", "size", 100);
        final Vertex lax = tinkerGraph.addVertex(T.id, 2, T.label, "airport", "code", "lax", "description", "LAX", "size", 1000);
        final Vertex ausToLax = tinkerGraph.addVertex(T.id, 3, T.label, "flight", "flightId", "123", "description", "SouthWest");
        aus.addEdge("departure", ausToLax, T.id, 4, "weight", 0.5d);
        lax.addEdge("arrival",ausToLax,T.id, 5, "weight", 0.5d);

        JsonObject departureCriteria = new JsonObject();
        departureCriteria.addProperty("code","aus");
        String navQuery = this.graphQueryGenerator.generateNavigationQuery("airport","flight",
                "departure",departureCriteria);
        logger.info(navQuery);

        SparqlTraversalSource server = new SparqlTraversalSource(this.graph);
        GraphData graphData = new LocalGraphData(server);
        GraphTraversal result = this.graphQueryProcessor.navigate(graphData,navQuery);
        Iterator<Map> itr = result.toSet().iterator();
        while(itr.hasNext())
        {
            Map map = itr.next();
            Vertex flight = (Vertex) map.get("flight");
            Vertex airport = (Vertex) map.get("airport");
            if(flight == null)
            {
                continue;
            }

            System.out.println(map);
            System.out.println("Flight: "+flight.property("flightId").value());
            System.out.println("Airport: "+airport.property("code").value());
        }
    }

    private void criteria1(Graph graph)
    {
        JsonObject criteria = new JsonObject();
        criteria.addProperty("size", 100);
        String query = this.graphQueryGenerator.generateQueryByCriteria("airport",criteria);
        logger.info(query);
        SparqlTraversalSource server = new SparqlTraversalSource(this.graph);
        GraphData graphData = new LocalGraphData(server);
        GraphTraversal result = this.graphQueryProcessor.query(graphData,query);
        Iterator<Vertex> itr = result.toSet().iterator();
        while(itr.hasNext())
        {
            Vertex vertex = itr.next();
            logger.info("CODE: "+vertex.property("code").value());
            logger.info("DESCRIPTION: "+vertex.property("description").value());
            logger.info("SIZE: "+vertex.property("size").value());

            assertEquals("aus",vertex.property("code").value());
        }
    }

    private void criteria2(Graph graph)
    {
        JsonObject criteria = new JsonObject();
        GraphQueryGenerator queryGenerator = new GraphQueryGenerator();
        String query = this.graphQueryGenerator.generateQueryByCriteria("airport",criteria);
        logger.info(query);
        SparqlTraversalSource server = new SparqlTraversalSource(this.graph);
        GraphData graphData = new LocalGraphData(server);
        GraphTraversal result = this.graphQueryProcessor.query(graphData,query);
        Iterator<Vertex> itr = result.toSet().iterator();
        int counter = 0;
        while(itr.hasNext())
        {
            Vertex vertex = itr.next();
            logger.info("CODE: "+vertex.property("code").value());
            logger.info("DESCRIPTION: "+vertex.property("description").value());
            logger.info("SIZE: "+vertex.property("size").value());
            counter++;
        }
        assertEquals(2, counter);
    }

    @Test
    public void prototypeUnion() throws Exception
    {
        Graph graph = TinkerFactory.createModern();
        SparqlTraversalSource sparqlTraversalSource = new SparqlTraversalSource(graph);

        String query = "SELECT *\n" +
                "         WHERE {\n" +
                "           {?person e:created ?software .}\n" +
                "           UNION\n" +
                "           {?software v:lang \"java\" .} }";
        GraphTraversal result = sparqlTraversalSource.sparql(query);
        Iterator<Map> itr = result.toSet().iterator();
        while(itr.hasNext())
        {
            Map map = itr.next();
            Vertex software = (Vertex) map.get("software");
            Vertex person = (Vertex) map.get("person");
            System.out.println(map);
            System.out.println("Software: "+software.property("name").value());
            if(person != null)
            {
                System.out.println("Person: "+person.property("name").value());
            }
        }
    }

    @Test
    public void prototypeUnionReal() throws Exception
    {
        Graph tinkerGraph = TinkerGraph.open();
        final Vertex aus = tinkerGraph.addVertex(T.id, 1, T.label, "airport", "code", "aus", "description", "AUS", "size", 100);
        final Vertex lax = tinkerGraph.addVertex(T.id, 2, T.label, "airport", "code", "lax", "description", "LAX", "size", 1000);
        final Vertex ausToLax = tinkerGraph.addVertex(T.id, 3, T.label, "flight", "flightId", "123", "description", "SouthWest");
        aus.addEdge("departure", ausToLax, T.id, 4, "weight", 0.5d);
        lax.addEdge("arrival",ausToLax,T.id, 5, "weight", 0.5d);
        SparqlTraversalSource sparqlTraversalSource = new SparqlTraversalSource(tinkerGraph);

        /*String query = "SELECT *\n" +
                "         WHERE {\n" +
                "         {?airport e:departure ?flight . }" +
                "         UNION\n"+
                "         {?airport v:code \"aus\" .} }";
       */

        JsonObject departureCriteria = new JsonObject();
        departureCriteria.addProperty("code","aus");
        String query = this.graphQueryGenerator.generateNavigationQuery("airport","flight",
                "departure",departureCriteria);
        GraphTraversal result = sparqlTraversalSource.sparql(query);
        Iterator<Map> itr = result.toSet().iterator();
        while(itr.hasNext())
        {
            Map map = itr.next();
            Vertex flight = (Vertex) map.get("flight");
            Vertex airport = (Vertex) map.get("airport");
            if(flight == null)
            {
                continue;
            }

            System.out.println(map);
            System.out.println("Flight: "+flight.property("flightId").value());
            System.out.println("Airport: "+airport.property("code").value());
        }

        /*query = "SELECT *\n" +
                "         WHERE {\n" +
                "         {?airport e:arrival ?flight . }" +
                "         UNION\n"+
                "         {?airport v:code \"lax\" .} }";*/
        JsonObject arrivalCriteria = new JsonObject();
        arrivalCriteria.addProperty("code","lax");
        query = this.graphQueryGenerator.generateNavigationQuery("airport","flight",
                "arrival",arrivalCriteria);
        result = sparqlTraversalSource.sparql(query);
        itr = result.toSet().iterator();
        while(itr.hasNext())
        {
            Map map = itr.next();
            Vertex flight = (Vertex) map.get("flight");
            Vertex airport = (Vertex) map.get("airport");

            if(flight == null)
            {
                continue;
            }

            System.out.println(map);
            System.out.println("Flight: "+flight.property("flightId").value());
            System.out.println("Airport: "+airport.property("code").value());
        }
    }

    @Test
    public void prototypeStarShape() throws Exception
    {
        Graph graph = TinkerFactory.createModern();
        SparqlTraversalSource sparqlTraversalSource = new SparqlTraversalSource(graph);

        String query = "SELECT * \n" +
                "         WHERE {\n" +
                "           ?person v:name \"josh\" .\n" +
                "           ?person v:age ?age .\n" +
                "           ?person e:created ?software .\n" +
                "           ?software v:lang ?lang .\n" +
                "           ?software v:name ?name . }";
        GraphTraversal result = sparqlTraversalSource.sparql(query);
        Iterator<Map> itr = result.toSet().iterator();
        while(itr.hasNext())
        {
            Map map = itr.next();
            Vertex software = (Vertex) map.get("software");
            Vertex person = (Vertex) map.get("person");
            System.out.println(map);
            System.out.println("Software: "+software.property("name").value());
            if(person != null)
            {
                System.out.println("Person: "+person.property("name").value());
            }
        }
    }
}
