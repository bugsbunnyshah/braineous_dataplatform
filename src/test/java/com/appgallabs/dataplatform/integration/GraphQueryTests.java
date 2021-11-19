package com.appgallabs.dataplatform.integration;

import com.appgallabs.dataplatform.ingestion.service.MapperService;
import com.appgallabs.dataplatform.query.LocalGraphData;
import com.appgallabs.dataplatform.query.ObjectGraphQueryService;
import test.components.BaseTest;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.sparql.process.traversal.dsl.sparql.SparqlTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@QuarkusTest
public class GraphQueryTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(GraphQueryTests.class);

    @Inject
    private MapperService mapperService;

    @Inject
    private ObjectGraphQueryService queryService;

    @Test
    public void testMapAirlineData() throws Exception
    {
        String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "aviation/flights.json"),
                StandardCharsets.UTF_8);
        JsonArray jsonArray = JsonParser.parseString(sourceData).getAsJsonObject().getAsJsonArray("data");

        JsonArray input = new JsonArray();
        for(int i=0; i<5; i++)
        {
            input.add(jsonArray.get(0));
        }

        TinkerGraph graph = TinkerGraph.open();
        SparqlTraversalSource server = new SparqlTraversalSource(graph);

        for(int i=0;i<5; i++) {
            JsonObject json = input.get(i).getAsJsonObject();
            json.addProperty("id",UUID.randomUUID().toString());
            //JsonUtil.print(json);
            Vertex vertex = this.saveObjectGraph("flight",json,null, server,false);
            //logger.info(vertex.keys().toString());
        }

        this.queryService.setGraphData(new LocalGraphData(server));

        JsonArray data = this.queryService.queryByCriteria("flightEntity", new JsonObject());
        //JsonUtil.print(data);

        data = this.queryService.queryByCriteria("departure", new JsonObject());
        //JsonUtil.print(data);

        data = this.queryService.queryByCriteria("flight", new JsonObject());
        JsonUtil.print(data);

        data = this.queryService.queryByCriteria("arrival", new JsonObject());
        //JsonUtil.print(data);

        data = this.queryService.queryByCriteria("airline", new JsonObject());
        //JsonUtil.print(data);

        data = this.queryService.queryByCriteria("id", new JsonObject());
        //JsonUtil.print(data);

        JsonObject departureCriteria = new JsonObject();
        departureCriteria.addProperty("flight_status","scheduled");
        data = this.queryService.navigateByCriteria("entity_flight","flight","has"
        ,departureCriteria);
        JsonUtil.print(data);
    }

    private Vertex saveObjectGraph(String entity,JsonObject parent,JsonObject child,SparqlTraversalSource server,boolean isProperty)
    {
        Vertex vertex;

        String vertexId = UUID.randomUUID().toString();

        GraphTraversal<Vertex,Vertex> traversal = null;
        if(isProperty)
        {
            traversal = server.addV(entity);
        }
        else
        {
            traversal = server.addV("entity_"+entity);
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
                Vertex propertyVertex = this.saveObjectGraph(property,parent,propertyObject,server,true);
                children.add(propertyVertex);
            }
            else if(json.get(property).isJsonPrimitive())
            {
                String value = json.get(property).getAsString();
                traversal = traversal.property(property,value+":"+vertexId);
            }
        }
        traversal.property("vertexId",UUID.randomUUID().toString());
        traversal.property("source",json.toString());
        vertex = traversal.next();

        for(Vertex local:children)
        {
            vertex.addEdge("has", local, T.id, UUID.randomUUID().toString(), "weight", 0.5d);
        }

        return vertex;
    }
}