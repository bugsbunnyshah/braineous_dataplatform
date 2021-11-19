package org.mitre.harmony.matchers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DataIngestionTests {
    private static Logger logger = LoggerFactory.getLogger(DataIngestionTests.class);

    @Test
    public void testFirstEndToEndMapping() throws Exception
    {
        ArrayList<SchemaElement> schemaElements = this.parseSchemaElements();
        logger.info(schemaElements.toString());
    }

    private ArrayList<SchemaElement> parseSchemaElements() throws IOException
    {
        ArrayList<SchemaElement> schemaElements = new ArrayList<>();

        String json = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("people.json"),
                StandardCharsets.UTF_8);

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
        for(Map.Entry<String, JsonElement> entry:entrySet)
        {
            String field = entry.getKey();
            JsonElement jsonElement = entry.getValue();
            SchemaElement schemaElement = new SchemaElement();
            schemaElement.setId(field.hashCode());
            schemaElement.setName(field);
            schemaElement.setDescription(field);
            if(jsonElement.isJsonArray())
            {
                continue;
            }
            schemaElements.add(schemaElement);
        }

        return schemaElements;
    }

    private HierarchicalSchemaInfo getHierarchialSchema(String schemaName, ArrayList<SchemaElement> schemaElements)
    {
        Schema schema = new Schema();
        schema.setName(schemaName);

        SchemaModel schemaModel = new RelationalSchemaModel();
        schemaModel.setName(schemaName+"Model");
        SchemaInfo schemaInfo1 = new SchemaInfo(schema, new ArrayList<>(), new ArrayList<>());
        HierarchicalSchemaInfo schemaInfo = new HierarchicalSchemaInfo(schemaInfo1);
        schemaInfo.setModel(schemaModel);

        for(SchemaElement schemaElement:schemaElements)
        {
            this.addElement(schemaInfo, schemaElement.getId(), schemaElement.getName(), schemaElement.getDescription());
        }

        return schemaInfo;
    }

    private void addElement(HierarchicalSchemaInfo info1, int id, String name, String description)
    {
        Entity element = new Entity();
        element.setId(id);
        element.setName(name);
        element.setDescription(description);
        element.setBase(id);
        info1.addElement(element);
        info1.getModel().getChildElements(info1, id).add(element);
    }

    private Map<SchemaElement,Double> findMatches(FilteredSchemaInfo f1, FilteredSchemaInfo f2,
                                                  List<SchemaElement> schemaElements)
    {
        Map<SchemaElement, Double> result = new HashMap<>();
        Matcher matcher = MatcherManager.getMatcher(
                "org.mitre.harmony.matchers.matchers.EditDistanceMatcher");
        assertNotNull(matcher);
        matcher.initialize(f1, f2);

        /*ArrayList<MatcherParameter> matcherParameters = matcher.getParameters();
        MatcherParameter name = null;
        for(MatcherParameter parameter:matcherParameters)
        {
            logger.info(parameter.getName());
            if(parameter.getName().equalsIgnoreCase("UseName"))
            {
                name = parameter;
                break;
            }
        }
        ((MatcherCheckboxParameter)name).setSelected(Boolean.TRUE);*/

        MatcherScores matcherScores = matcher.match();
        for(SchemaElement schemaElement:schemaElements) {
            Integer id = schemaElement.getId();
            Double score = matcherScores.getScore(new ElementPair(id, id)).getTotalEvidence();
            result.put(schemaElement, score);
        }
        return result;
    }

    private void performMapping(Map<SchemaElement, Double> scores) throws IOException
    {
        ArrayList<SchemaElement> schemaElements = new ArrayList<>();

        String json = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("airlinesData.json"),
                StandardCharsets.UTF_8);

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        JsonObject result = new JsonObject();
        Set<Map.Entry<SchemaElement, Double>> entrySet = scores.entrySet();
        for(Map.Entry<SchemaElement, Double> entry: entrySet)
        {
            SchemaElement schemaElement = entry.getKey();
            Double score = entry.getValue();
            String field = schemaElement.getName();

            result.addProperty(field, jsonObject.get(field).getAsString());
        }

        logger.info("*******");
        logger.info(result.toString());
        logger.info("*******");
    }
}
