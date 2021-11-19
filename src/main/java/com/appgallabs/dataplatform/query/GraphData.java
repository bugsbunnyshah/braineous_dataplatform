package com.appgallabs.dataplatform.query;

import org.apache.tinkerpop.gremlin.sparql.process.traversal.dsl.sparql.SparqlTraversalSource;

public interface GraphData {
    SparqlTraversalSource getTraversalSource();
}
