package com.appgallabs.dataplatform.query;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.sparql.process.traversal.dsl.sparql.SparqlTraversalSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
public class GraphQueryProcessor {
    private static Logger logger = LoggerFactory.getLogger(GraphQueryProcessor.class);

    public GraphTraversal query(GraphData graphData, String sparqlQuery)
    {
        SparqlTraversalSource server = graphData.getTraversalSource();
        GraphTraversal result = server.sparql(sparqlQuery);
        return result;
    }

    public GraphTraversal navigate(GraphData graphData, String sparqlQuery)
    {
        SparqlTraversalSource server = graphData.getTraversalSource();
        GraphTraversal result = server.sparql(sparqlQuery);
        return result;
    }
}
