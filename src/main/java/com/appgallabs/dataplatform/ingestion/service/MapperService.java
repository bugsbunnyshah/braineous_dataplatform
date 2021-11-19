package com.appgallabs.dataplatform.ingestion.service;

import com.appgallabs.dataplatform.history.service.DataReplayService;
import com.appgallabs.dataplatform.ingestion.util.CSVDataUtil;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.query.ObjectGraphQueryService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.sparql.process.traversal.dsl.sparql.SparqlTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.mitre.harmony.matchers.ElementPair;
import org.mitre.harmony.matchers.MatcherManager;
import org.mitre.harmony.matchers.MatcherScore;
import org.mitre.harmony.matchers.MatcherScores;
import org.mitre.harmony.matchers.matchers.Matcher;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.FilteredSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.SchemaInfo;
import org.mitre.schemastore.model.schemaInfo.model.RelationalSchemaModel;
import org.mitre.schemastore.model.schemaInfo.model.SchemaModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class MapperService {
    private static Logger logger = LoggerFactory.getLogger(MapperService.class);

    private TinkerGraph tg;
    private GraphTraversalSource g;
    private CSVDataUtil csvDataUtil = new CSVDataUtil();

    @Inject
    private ObjectGraphQueryService objectGraphQueryService;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private DataReplayService dataReplayService;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void start()
    {
        //JanusGraphManager.getInstance(true);
        //JanusGraphFactory.open("janus.properties");
        try {
            this.tg = TinkerGraph.open();
            this.g = tg.traversal(); //TODO add remoteconnection

            logger.info("******************");
            logger.info("OBJECT_GRAPH_QUERY_SERVICE_WITH_OBJECT_GRAPH_SUPPORT: ACTIVE");
            logger.info("******************");
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

    public JsonObject map(String entity,JsonArray sourceData)
    {
        Tenant tenant = this.securityTokenContainer.getTenant();
        JsonObject result = StreamIngesterContext.getStreamIngester().submit(
                tenant,
                this.securityTokenContainer,
                this.mongoDBJsonStore,
                this.dataReplayService,
                sourceData);
        return result;

        /*logger.info("********SOURCE_DATA************");
        JsonUtil.print(sourceData);
        JsonArray result = new JsonArray();
        try
        {
            int size = sourceData.size();
            for(int i=0; i<size; i++)
            {
                JsonElement root = sourceData.get(i);
                if(root.isJsonPrimitive())
                {
                    continue;
                }

                HierarchicalSchemaInfo sourceSchemaInfo = this.populateHierarchialSchema(root.toString(),
                        root.toString(), null);
                HierarchicalSchemaInfo destinationSchemaInfo = this.populateHierarchialSchema(root.toString(),
                        root.toString(), null);


                FilteredSchemaInfo f1 = new FilteredSchemaInfo(sourceSchemaInfo);
                f1.addElements(sourceSchemaInfo.getElements(Entity.class));
                FilteredSchemaInfo f2 = new FilteredSchemaInfo(destinationSchemaInfo);
                f2.addElements(destinationSchemaInfo.getElements(Entity.class));

                Map<SchemaElement, Double> scores = this.findMatches(f1, f2, sourceSchemaInfo.getElements(Entity.class));
                logger.info("*************SCORES************************");
                //logger.info(scores.toString());
                //logger.info("*************************************");

                JsonObject local = this.performMapping(scores, root.toString());

                //ObjectGraph/Gremlin integration
                logger.info("OQS: "+this.objectGraphQueryService);
                logger.info("GD: "+this.objectGraphQueryService.getGraphData());
                Vertex vertex = this.saveObjectGraph(entity,local,null, this.
                                objectGraphQueryService.getGraphData().getTraversalSource(),
                        false);
                String vertexId = vertex.property("vertexId").value().toString();
                local.addProperty("vertexId", vertexId);
                result.add(local);
            }

            return result;
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }*/
    }

    static JsonObject performMapping(Map<SchemaElement, Double> scores, String json) throws IOException
    {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        JsonObject result = new JsonObject();
        Set<Map.Entry<SchemaElement, Double>> entrySet = scores.entrySet();
        for(Map.Entry<SchemaElement, Double> entry: entrySet)
        {
            SchemaElement schemaElement = entry.getKey();
            Double score = entry.getValue();
            String field = schemaElement.getName();
            StringTokenizer tokenizer = new StringTokenizer(field, ".");
            while(tokenizer.hasMoreTokens())
            {
                String local = tokenizer.nextToken();
                if(!jsonObject.has(local))
                {
                    continue;
                }

                result.add(local, jsonObject.get(local));
            }
        }

        return result;
    }


    public JsonObject mapXml(String entity,JsonObject sourceData)
    {
        /*JsonArray result = new JsonArray();

        this.traverse(sourceData, result);

        result = this.map(entity,result);
        return result;*/
        JsonArray result = new JsonArray();
        this.traverse(sourceData, result);
        return this.map(entity,result);
    }
    //---------------------------------------------------------------------------------------------------------------------
    static HierarchicalSchemaInfo createHierachialSchemaInfo(String schemaName)
    {
        Schema schema = new Schema();
        schema.setName(schemaName);

        SchemaModel schemaModel = new RelationalSchemaModel();
        schemaModel.setName(schemaName+"Model");
        SchemaInfo schemaInfo1 = new SchemaInfo(schema, new ArrayList<>(), new ArrayList<>());
        HierarchicalSchemaInfo schemaInfo = new HierarchicalSchemaInfo(schemaInfo1);
        schemaInfo.setModel(schemaModel);

        return schemaInfo;
    }

    static HierarchicalSchemaInfo populateHierarchialSchema(String object, String sourceData, String parent)
    {
        HierarchicalSchemaInfo schemaInfo = createHierachialSchemaInfo(object);
        JsonElement sourceElement = JsonParser.parseString(sourceData);
        JsonObject jsonObject = new JsonObject();
        if(!sourceElement.isJsonPrimitive())
        {
            jsonObject = sourceElement.getAsJsonObject();
        }
        else
        {
            jsonObject = new JsonObject();
            jsonObject.addProperty(sourceData, sourceElement.toString());
        }

        Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
        for(Map.Entry<String, JsonElement> entry:entrySet)
        {
            String field = entry.getKey();
            JsonElement jsonElement = entry.getValue();

            if(jsonElement.isJsonObject())
            {
                Entity element = new Entity();
                element.setId(field.hashCode());
                element.setName(field);
                element.setDescription(field);
                schemaInfo.addElement(element);
                HierarchicalSchemaInfo fieldInfos = populateHierarchialSchema(field,
                        jsonElement.getAsJsonObject().toString(), object);

                ArrayList<SchemaElement> blah = fieldInfos.getElements(Entity.class);
                for(SchemaElement local:blah)
                {
                    schemaInfo.addElement(local);
                }

                continue;
            }
            else if(jsonElement.isJsonArray())
            {
                JsonElement top = jsonElement.getAsJsonArray().get(0);
                HierarchicalSchemaInfo fieldInfos = populateHierarchialSchema(field,
                        top.toString(), object);

                ArrayList<SchemaElement> blah = fieldInfos.getElements(Entity.class);
                for(SchemaElement local:blah)
                {
                    schemaInfo.addElement(local);
                }

                continue;
            }
            else
            {
                String objectLocation = parent + "." + object + "." + field;
                Entity element = new Entity();
                element.setId(objectLocation.hashCode());
                element.setName(objectLocation);
                element.setDescription(objectLocation);
                schemaInfo.addElement(element);
            }
        }

        return schemaInfo;
    }

    static Map<SchemaElement,Double> findMatches(FilteredSchemaInfo f1, FilteredSchemaInfo f2,
                                                  ArrayList<SchemaElement> sourceElements)
    {
        Map<SchemaElement, Double> result = new HashMap<>();
        Matcher matcher = MatcherManager.getMatcher(
                "org.mitre.harmony.matchers.matchers.EditDistanceMatcher");
        matcher.initialize(f1, f2);

        MatcherScores matcherScores = matcher.match();
        Set<ElementPair> elementPairs = matcherScores.getElementPairs();
        for (ElementPair elementPair : elementPairs) {
            MatcherScore matcherScore = matcherScores.getScore(elementPair);
            Double score = 0d;
            if(matcherScore != null) {
                score = matcherScore.getTotalEvidence();
            }
            for(SchemaElement schemaElement: sourceElements)
            {
                if(schemaElement.getId() == elementPair.getSourceElement())
                {
                    result.put(schemaElement, score);
                }
            }
        }

        return result;
    }

    private void traverse(JsonObject currentObject, JsonArray result)
    {
        String vertexId = UUID.randomUUID().toString();
        final GraphTraversal<Vertex, Vertex> currentVertex = this.g.addV();
        currentVertex.property("vertexId",vertexId);

        Iterator<String> allProps = currentObject.keySet().iterator();
        while(allProps.hasNext())
        {
            String nextObject = allProps.next();
            JsonElement resolve = currentObject.get(nextObject);
            if(resolve.isJsonObject())
            {
                JsonObject resolveJson = resolve.getAsJsonObject();
                if(resolveJson.keySet().size()==0)
                {
                    //EMPTY TAG...skip it
                    continue;
                }
                if(resolveJson.keySet().size()==1) {
                    //logger.info(nextObject+": RESOLVING");
                    this.resolve(nextObject, resolveJson, result);
                }
                else
                {
                    //logger.info(nextObject+": TRAVERSING");
                    this.traverse(resolveJson, result);
                }
            }
            else
            {
                if(resolve.isJsonPrimitive())
                {
                    logger.info("PRIMITIVE_FOUND");
                    currentVertex.property(nextObject, resolve.getAsString());
                }
            }
        }
    }

    private void resolve(String parent, JsonObject leaf, JsonArray result)
    {
        //logger.info("*********************************");
        //logger.info("PARENT: "+parent);
        //logger.info("*********************************");
        JsonArray finalResult=null;
        if (leaf.isJsonObject()) {
            String child = leaf.keySet().iterator().next();
            JsonElement childElement = leaf.get(child);
            if(childElement.isJsonArray()) {
                //logger.info(parent+": CHILD_ARRAY");
                finalResult = childElement.getAsJsonArray();
            }
            else
            {
                //logger.info(parent+": CHILD_OBJECT");
                finalResult = new JsonArray();
                finalResult.add(childElement);
                //this.traverse(childElement.getAsJsonObject(), result);
            }
        } else {
            //logger.info(parent+": LEAF_ARRAY");
            finalResult = leaf.getAsJsonArray();
        }


        if(finalResult != null) {
            //logger.info(parent+": CALCULATING");
            Iterator<JsonElement> itr = finalResult.iterator();
            JsonArray jsonArray = new JsonArray();
            while (itr.hasNext())
            {
                JsonElement jsonElement = itr.next();
                if(jsonElement.isJsonPrimitive())
                {
                    JsonObject primitive = new JsonObject();
                    primitive.addProperty(parent,jsonElement.toString());
                    jsonArray.add(primitive);
                }
                else {
                    jsonArray.add(jsonElement);
                }
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.add(parent,jsonArray);
            result.add(jsonObject);
        }
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
