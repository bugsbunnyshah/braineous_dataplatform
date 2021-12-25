package com.appgallabs.dataplatform.datalake;

//import org.apache.tinkerpop.gremlin.process.traversal.TextP;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.sparql.process.traversal.dsl.sparql.SparqlTraversalSource;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TinkerPopStartedTests {
    private static Logger logger = LoggerFactory.getLogger(TinkerPopStartedTests.class);

    @Test
    public void testStart() throws Exception
    {
        TinkerGraph tg = TinkerGraph.open();

        //tinkerGraph.addVertex("0","1","3");
        // Create a Traversal source object
        SparqlTraversalSource g = (SparqlTraversalSource)
                tg.traversal(SparqlTraversalSource.class);

        // Add some nodes and vertices - Note the use of "iterate".
        GraphTraversal traversal = g.addV("airport").property("code","AUS").as("aus").
                addV("airport").property("code","DFW").as("dfw").
                addV("airport").property("code","LAX").as("lax").
                addV("airport").property("code","JFK").as("jfk").
                addV("airport").property("code","ATL").as("atl").
                addE("route").from("aus").to("dfw").
                addE("route").from("aus").to("atl").
                addE("route").from("atl").to("dfw").
                addE("route").from("atl").to("jfk").
                addE("route").from("dfw").to("jfk").
                addE("route").from("dfw").to("lax").
                addE("route").from("lax").to("jfk").
                addE("route").from("lax").to("aus").
                addE("route").from("lax").to("dfw").
                iterate();

        logger.info("******************************");
        logger.info(traversal.toString());
        logger.info("******************************");

        //sparql("""SELECT ?name ?age
        //     WHERE { ?person v:name ?name . ?person v:age ?age }
        //    ORDER BY ASC(?age)""");
        GraphTraversal gt = g.sparql("SELECT ?airport WHERE { ?airportValue v:aus }");
        GraphTraversal map = gt.group();
        logger.info(map.toString());

        //Map<String, Long> result = (Map<String, Long>) gt.outE().groupCount().by("routeKey").next();
        //System.out.println(result);
        //logger.info(blah.sparql("SELECT ?name ?age WHERE { ?person v:name ?name . ?person v:age ?age } ORDER BY ASC(?age)").getClass().getName());
        //logger.info(blah.sparql("SELECT ?name ?age WHERE { ?person v:name ?name . ?person v:age ?age } ORDER BY ASC(?age)").toString());
    }

    @Test
    public void testGetAllVerticesInTheGraphDefault() throws Exception
    {
        TinkerGraph tg = TinkerGraph.open();

        //tinkerGraph.addVertex("0","1","3");
        // Create a Traversal source object
        GraphTraversalSource g = tg.traversal();

        // Add some nodes and vertices - Note the use of "iterate".
        GraphTraversal traversal = g.V();


        //Get all the vertices in the graph
        GraphTraversal vertices = traversal.V();
        logger.info(vertices.toString());

        //has
        /*Predicate<String> left = new TextP(new BiPredicate<String, String>() {
            @Override
            public boolean test(String s, String s2) {
                return s.equals(s2);
            }
        },"marko");
        Predicate<String> right = new TextP(new BiPredicate<String, String>() {
            @Override
            public boolean test(String s, String s2) {
                return s.equals(s2);
            }
        },"josh");*/
        //Predicate<String> where = left.or(right);
        GraphTraversal has = traversal.has("person", "name");
        logger.info(has.toString());

        //g.V().
        //        has('person', 'name', within('marko', 'josh')).
        //        outE().
        //        groupCount().
        //        by(label()).next()
        System.out.println(has.outE().groupCount().next());
    }

    @Test
    public void testGetAllVerticesInTheGraphCustomOr() throws Exception
    {
        TinkerGraph tg = TinkerGraph.open();

        //tinkerGraph.addVertex("0","1","3");
        // Create a Traversal source object
        GraphTraversalSource g = tg.traversal();

        // Add some nodes and vertices - Note the use of "iterate".
        GraphTraversal traversal = g.addV("airport").property("code","AUS").as("aus").
                addV("airport").property("code","DFW").as("dfw").
                addV("airport").property("code","LAX").as("lax").
                addV("airport").property("code","JFK").as("jfk").
                addV("airport").property("code","ATL").as("atl").
                addE("route").from("aus").to("dfw").property("routeKey","2").
                addE("route").from("dfw").to("aus").property("routeKey","3").
                addE("route").from("dfw").to("jfk").property("routeKey","0").
                addE("route").from("jfk").to("lax").property("routeKey","1");
                //addE("route").from("aus").to("atl").
                //addE("route").from("atl").to("dfw").
                //addE("route").from("atl").to("jfk").
                //addE("route").from("dfw").to("jfk").
                //addE("route").from("dfw").to("lax").
                //addE("route").from("lax").to("jfk").
                //addE("route").from("lax").to("aus").
                //addE("route").from("lax").to("dfw");


        //Get all the vertices in the graph
        GraphTraversal vertices = traversal.V();
        logger.info(vertices.toString());

        //has (or)
        /*Predicate<String> left = new TextP(new BiPredicate<String, String>() {
            @Override
            public boolean test(String s, String s2) {
                return s.equals(s2);
            }
        },"AUS");
        Predicate<String> right = new TextP(new BiPredicate<String, String>() {
            @Override
            public boolean test(String s, String s2) {
                return s.equals(s2);
            }
        },"DFW");
        Predicate<String> where = left.or(right);*/
        GraphTraversal has = traversal.has("airport", "code");
        logger.info(has.toString());

        //g.V().
        //        has('person', 'name', within('marko', 'josh')).
        //        outE().
        //        groupCount().
        //        by(label()).next()
        //logger.info(has.outE().next().toString());
        //logger.info(has.outE().groupCount().next().toString());
        Map<String, Long> result = (Map<String, Long>) has.outE().groupCount().by("routeKey").next();
        //Object result = has.outE().groupCount().next();
        System.out.println(result);
        System.out.println(result.getClass());
    }

    @Test
    public void testGetAllVerticesInTheGraphCustomAnd() throws Exception
    {
        TinkerGraph tg = TinkerGraph.open();

        //tinkerGraph.addVertex("0","1","3");
        // Create a Traversal source object
        GraphTraversalSource g = tg.traversal();

        // Add some nodes and vertices - Note the use of "iterate".
        GraphTraversal traversal = g.addV("airport").property("code","AUS").as("aus").
                addV("airport").property("code","DFW").as("dfw").
                addV("airport").property("code","LAX").as("lax").
                addV("airport").property("code","JFK").as("jfk").
                addV("airport").property("code","ATL").as("atl").
                addE("route").from("aus").to("dfw").
                addE("route").from("dfw").to("aus").
                addE("route").from("dfw").to("jfk").
                addE("route").from("jfk").to("lax");
        //addE("route").from("aus").to("atl").
        //addE("route").from("atl").to("dfw").
        //addE("route").from("atl").to("jfk").
        //addE("route").from("dfw").to("jfk").
        //addE("route").from("dfw").to("lax").
        //addE("route").from("lax").to("jfk").
        //addE("route").from("lax").to("aus").
        //addE("route").from("lax").to("dfw");


        //Get all the vertices in the graph
        GraphTraversal vertices = traversal.V();
        logger.info(vertices.toString());

        //has (or)
        /*Predicate<String> left = new TextP(new BiPredicate<String, String>() {
            @Override
            public boolean test(String s, String s2) {
                return s.equals(s2);
            }
        },"AUS");
        Predicate<String> right = new TextP(new BiPredicate<String, String>() {
            @Override
            public boolean test(String s, String s2) {
                return s.equals(s2);
            }
        },"DFW");
        Predicate<String> where = left.and(right);*/
        GraphTraversal has = traversal.has("airport", "code");
        logger.info(has.toString());

        //g.V().
        //        has('person', 'name', within('marko', 'josh')).
        //        outE().
        //        groupCount().
        //        by(label()).next()
        //logger.info(has.outE().next().toString());
        //logger.info(has.outE().groupCount().next().toString());
        //Object result = has.outE().groupCount().by("code").next();
        Object result = has.outE().groupCount().next();
        System.out.println(result);
    }
}
