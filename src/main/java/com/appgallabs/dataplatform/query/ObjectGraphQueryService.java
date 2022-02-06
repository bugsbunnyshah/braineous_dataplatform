package com.appgallabs.dataplatform.query;

import com.appgallabs.dataplatform.ingestion.service.MapperService;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.tinkerpop.gremlin.process.remote.RemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.sparql.process.traversal.dsl.sparql.SparqlTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

@ApplicationScoped
public class ObjectGraphQueryService {
    private static Logger logger = LoggerFactory.getLogger(ObjectGraphQueryService.class);

    @Inject
    private MapperService mapperService;

    @Inject
    private GraphQueryGenerator graphQueryGenerator;

    @Inject
    private  GraphQueryProcessor graphQueryProcessor;


    private Graph g;

    private GraphData graphData;

    private SparqlTraversalSource server;

    @PostConstruct
    public void onStart()
    {
        //TODO: instantiate with a RemoteGraphData
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

        /*RemoteConnection remoteConnection = RemoteConnection.from(configuration);
        this.server = new SparqlTraversalSource(remoteConnection);
        this.g = TinkerGraph.open();*/

        this.g = TinkerGraph.open();
        this.server = new SparqlTraversalSource(g);
        this.graphData = new LocalGraphData(this.server);
    }

    @PreDestroy
    public void onStop(){
        this.g = null;
        this.server = null;
        this.graphData = null;
    }

    public void setGraphData(GraphData graphData)
    {
        this.graphData = graphData;
    }

    public GraphData getGraphData()
    {
        return this.graphData;
    }

    public JsonArray queryByCriteria(String entity, JsonObject criteria)
    {
        JsonArray response = new JsonArray();
        String query = this.graphQueryGenerator.generateQueryByCriteria(entity,criteria);

        GraphTraversal result = this.graphQueryProcessor.query(this.graphData, query);
        Iterator<Vertex> itr = result.toSet().iterator();
        while(itr.hasNext())
        {
            Vertex vertex = itr.next();
            JsonObject vertexJson = JsonParser.parseString(vertex.property("source").value().toString()).getAsJsonObject();
            response.add(vertexJson);
        }
        return response;
    }

    public JsonArray navigateByCriteria(String entity, String relationship, JsonObject criteria) throws Exception
    {
        JsonArray response = new JsonArray();

        String navQuery = this.graphQueryGenerator.generateNavigationQuery(entity,
                relationship,criteria);

        GraphTraversal result = this.graphQueryProcessor.navigate(this.graphData,navQuery);
        //result = result.flatMap(result);
        //System.out.println(result);
        Iterator<Map> itr = result.toSet().iterator();
        while(itr.hasNext())
        {
            Map map = itr.next();
            Vertex edge = (Vertex) map.get(entity);
            if(edge != null) {
                //System.out.println(edge.label());
                if (edge.label().equals(entity)) {
                    JsonObject edgeJson = JsonParser.parseString(edge.property("source").value().toString()).getAsJsonObject();
                    response.add(edgeJson);
                }
            }
            /*else
            {
                System.out.println("NOT_fOUND");
            }*/
        }

        return response;
    }

    public Vertex saveObjectGraph(String entity,
                                  JsonObject parent,JsonObject child,boolean isProperty)
    {
        Vertex vertex;
        if(!isProperty)
        {
            vertex = this.g.addVertex(T.label,entity);
        }
        else
        {
            vertex = this.g.addVertex(T.label, entity);
        }

        JsonObject json = parent;
        if(child != null)
        {
            json = child;
        }

        Set<String> properties = json.keySet();
        List<Vertex> children = new ArrayList<>();
        for(String property:properties)
        {
            if(json.get(property).isJsonObject())
            {
                JsonObject propertyObject = json.getAsJsonObject(property);
                Vertex propertyVertex = this.saveObjectGraph(property,parent,propertyObject,true);
                children.add(propertyVertex);
            }
            else if(json.get(property).isJsonPrimitive())
            {
                String value = json.get(property).getAsString();
                vertex.property(property,value);
            }
        }

        vertex.property("source",json.toString());
        vertex.property("vertexId",UUID.randomUUID().toString());

        for(Vertex local:children)
        {
            vertex.addEdge("edge_"+local.label(), local, T.id, UUID.randomUUID().toString(), "weight", 0.5d);
        }

        return vertex;
    }
}
