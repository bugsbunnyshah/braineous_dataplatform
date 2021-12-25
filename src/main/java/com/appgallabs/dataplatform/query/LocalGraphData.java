package com.appgallabs.dataplatform.query;

import org.apache.tinkerpop.gremlin.sparql.process.traversal.dsl.sparql.SparqlTraversalSource;

public class LocalGraphData implements GraphData{
    private SparqlTraversalSource traversalSource;

    public LocalGraphData(SparqlTraversalSource traversalSource)
    {
        this.traversalSource = traversalSource;
    }

    @Override
    public SparqlTraversalSource getTraversalSource() {
        return this.traversalSource;
    }
}
