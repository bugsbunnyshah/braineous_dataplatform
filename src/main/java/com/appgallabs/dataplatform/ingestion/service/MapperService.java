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
import org.eclipse.microprofile.config.inject.ConfigProperty;
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


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class MapperService {
    private static Logger logger = LoggerFactory.getLogger(MapperService.class);

    private CSVDataUtil csvDataUtil = new CSVDataUtil();

    @ConfigProperty(name = "environment")
    private String environment;

    @Inject
    private ObjectGraphQueryService objectGraphQueryService;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private DataReplayService dataReplayService;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private IngestionService ingestionService;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public JsonObject map(String entity,JsonArray sourceData)
    {
        System.out.println("MAPPER_SERVICE");
        Tenant tenant = this.securityTokenContainer.getTenant();
        JsonObject result = StreamIngesterContext.getStreamIngester().submit(
                tenant,
                this.environment,
                this.securityTokenContainer,
                this.mongoDBJsonStore,
                this.dataReplayService,
                this.objectGraphQueryService,
                this.ingestionService,
                entity,
                sourceData);
        return result;
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
        JsonArray result = new JsonArray();
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
}
