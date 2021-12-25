package com.appgallabs.dataplatform.query;

import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.tinkerpop.gremlin.process.remote.RemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.sparql.process.traversal.dsl.sparql.SparqlTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;

@QuarkusTest
public class SparqlTest {

    @Test
    public void testQuery() throws Exception
    {
        BaseConfiguration configuration = new BaseConfiguration();
        configuration.addProperty("port", 8182);
        configuration.addProperty("hosts", Arrays.asList("gremlin-server"));
        configuration.addProperty("gremlin.remote.remoteConnectionClass","org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection");
        configuration.addProperty("connectionPool.maxContentLength", 131072);
        configuration.addProperty("connectionPool.enableSsl", false);
        configuration.addProperty("connectionPool.maxSize", 80);
        configuration.addProperty("connectionPool.minSize", 10);
        configuration.addProperty("connectionPool.maxInProcessPerConnection", 16);
        configuration.addProperty("connectionPool.minInProcessPerConnection", 8);
        configuration.addProperty("connectionPool.maxWaitForConnection", 10000);
        configuration.addProperty("connectionPool.minSimultaneousUsagePerConnection", 10);
        configuration.addProperty("connectionPool.maxSimultaneousUsagePerConnection", 10);
        //configuration.addProperty("serializer.className", "org.apache.tinkerpop.gremlin.driver.ser.AbstractGryoMessageSerializerV3d0");
        configuration.addProperty("serializer.config.serializeResultToString", "true");
        //configuration.addProperty("serializer.className",
        //        "org.apache.tinkerpop.gremlin.driver.ser.GraphSONMessageSerializerV3d0");

        RemoteConnection remoteConnection = RemoteConnection.from(configuration);

        //Graph tinkerGraph = TinkerFactory.createModern();
        Graph tinkerGraph = TinkerGraph.open();

        /*final Vertex marko = tinkerGraph.addVertex(T.id, 1, T.label, "person", "name", "marko", "age", 29);
        final Vertex vadas = tinkerGraph.addVertex(T.id, 2, T.label, "person", "name", "vadas", "age", 29);
        final Vertex lop = tinkerGraph.addVertex(T.id, 3, T.label, "software", "name", "lop", "lang", "java");
        final Vertex josh = tinkerGraph.addVertex(T.id, 4, T.label, "person", "name", "josh", "age", 32);
        final Vertex ripple = tinkerGraph.addVertex(T.id, 5, T.label, "software", "name", "ripple", "lang", "java");
        final Vertex peter = tinkerGraph.addVertex(T.id, 6, T.label, "person", "name", "peter", "age", 35);
        marko.addEdge("knows", vadas, T.id, 7, "weight", 0.5d);
        marko.addEdge("knows", josh, T.id, 8, "weight", 1.0d);
        marko.addEdge("created", lop, T.id, 9, "weight", 0.4d);
        josh.addEdge("created", ripple, T.id, 10, "weight", 1.0d);
        josh.addEdge("created", lop, T.id, 11, "weight", 0.4d);
        peter.addEdge("created", lop, T.id, 12, "weight", 0.2d);*/

        final Vertex aus = tinkerGraph.addVertex(T.id, 1, T.label, "airport", "code", "aus", "description", "AUS", "size", 100);
        final Vertex lax = tinkerGraph.addVertex(T.id, 2, T.label, "airport", "code", "lax", "description", "LAX", "size", 1000);


        SparqlTraversalSource server = new SparqlTraversalSource(tinkerGraph);

        /*String query = "SELECT * WHERE {}";
        GraphTraversal result = server.sparql(query);
        Iterator<Vertex> itr = result.toSet().iterator();
        while(itr.hasNext())
        {
            Vertex vertex = itr.next();
            System.out.println("VALUE: "+vertex.property("name"));
        }*/

        String value = "marko";
        String label = "person";
        GraphQueryGenerator queryGenerator = new GraphQueryGenerator();
        //String query = "SELECT * WHERE { ?element v:label ?label . }";
        //String query = "SELECT * WHERE {?element v:label \"airport\" . ?element v:code \"lax\"}";
        //query = "SELECT ?name WHERE { ?person  v:name \""+value+"\" . }";
        JsonObject criteria = new JsonObject();
        //criteria.addProperty("code","aus");
        //criteria.addProperty("size", 100);
        String query = queryGenerator.generateQueryByCriteria("airport",criteria);
        System.out.println(query);
        GraphTraversal result = server.sparql(query);
        Iterator<Vertex> itr = result.toSet().iterator();
        while(itr.hasNext())
        {
            Vertex vertex = itr.next();
            System.out.println("CODE: "+vertex.property("code").value());
            System.out.println("DESCRIPTION: "+vertex.property("description").value());
        }
    }

    @Test
    public void testRemoteQuery() throws Exception
    {
        BaseConfiguration configuration = new BaseConfiguration();
        configuration.addProperty("port", 8182);
        configuration.addProperty("hosts", Arrays.asList("gremlin-server"));
        configuration.addProperty("gremlin.remote.remoteConnectionClass","org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection");
        configuration.addProperty("connectionPool.maxContentLength", 131072);
        configuration.addProperty("connectionPool.enableSsl", false);
        configuration.addProperty("connectionPool.maxSize", 80);
        configuration.addProperty("connectionPool.minSize", 10);
        configuration.addProperty("connectionPool.maxInProcessPerConnection", 16);
        configuration.addProperty("connectionPool.minInProcessPerConnection", 8);
        configuration.addProperty("connectionPool.maxWaitForConnection", 10000);
        configuration.addProperty("connectionPool.minSimultaneousUsagePerConnection", 10);
        configuration.addProperty("connectionPool.maxSimultaneousUsagePerConnection", 10);
        //configuration.addProperty("serializer.className", "org.apache.tinkerpop.gremlin.driver.ser.AbstractGryoMessageSerializerV3d0");
        //configuration.addProperty("serializer.config.serializeResultToString", "true");
        configuration.addProperty("serializer.className",
                "org.apache.tinkerpop.gremlin.driver.ser.GraphSONMessageSerializerV3d0");

        RemoteConnection remoteConnection = RemoteConnection.from(configuration);

        //Graph tinkerGraph = TinkerFactory.createModern();
        //Graph tinkerGraph = EmptyGraph.instance();
        //SparqlTraversalSource g = tinkerGraph.traversal(SparqlTraversalSource.class);
        //SparqlTraversalSource g = new SparqlTraversalSource(remoteConnection);

        /*final Vertex marko = g.addVertex(T.id, 1, T.label, "person", "name", "marko", "age", 29);
        final Vertex vadas = g.addVertex(T.id, 2, T.label, "person", "name", "vadas", "age", 27);
        final Vertex lop = g.addVertex(T.id, 3, T.label, "software", "name", "lop", "lang", "java");
        final Vertex josh = g.addVertex(T.id, 4, T.label, "person", "name", "josh", "age", 32);
        final Vertex ripple = g.addVertex(T.id, 5, T.label, "software", "name", "ripple", "lang", "java");
        final Vertex peter = g.addVertex(T.id, 6, T.label, "person", "name", "peter", "age", 35);
        marko.addEdge("knows", vadas, T.id, 7, "weight", 0.5d);
        marko.addEdge("knows", josh, T.id, 8, "weight", 1.0d);
        marko.addEdge("created", lop, T.id, 9, "weight", 0.4d);
        josh.addEdge("created", ripple, T.id, 10, "weight", 1.0d);
        josh.addEdge("created", lop, T.id, 11, "weight", 0.4d);
        peter.addEdge("created", lop, T.id, 12, "weight", 0.2d);*/


        TinkerGraph g = TinkerGraph.open();
        //SparqlTraversalSource server = new SparqlTraversalSource(remoteConnection);
        SparqlTraversalSource server = new SparqlTraversalSource(g);
        server.addV("person").property("name","marko");

        String query = "SELECT * WHERE { }";
        GraphTraversal result = server.sparql(query);
        Iterator<Vertex> itr = result.toSet().iterator();
        while(itr.hasNext())
        {
            Vertex vertex = itr.next();
            System.out.println("VALUE: "+vertex.property("name"));
        }
        System.out.println(result.toSet());

        /*query = "SELECT ?name ?age\n" +
                "                     WHERE { ?person v:name ?name . ?person v:age ?age }\n" +
                "                     ORDER BY ASC(?age)";
        result = server.sparql(query);
        System.out.println(result.toSet());*/
    }

}
