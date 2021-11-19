package org.mitre.harmony.matchers;

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

public class MatchingTests {
    private static Logger logger = LoggerFactory.getLogger(MatchingTests.class);

    @Test
    public void testMatchingScenerioSimple() throws Exception
    {
        try
        {
            Schema schema2 = new Schema();
            schema2.setName("blah");

            SchemaModel schemaModel2 = new RelationalSchemaModel();
            schemaModel2.setName("blahModel2");
            SchemaInfo schemaInfo2 = new SchemaInfo(schema2, new ArrayList<>(), new ArrayList<>());
            HierarchicalSchemaInfo info2 = new HierarchicalSchemaInfo(schemaInfo2);
            info2.setModel(schemaModel2);
            for (int i = 0; i < 10; i++) {
                if (i == 0 || i == 1) {
                    SchemaElement element = new SchemaElement();
                    element.setId(i);
                    element.setName("name" + i);
                    element.setDescription("description" + i);
                    element.setBase(i);
                    info2.getModel().getRootElements(info2).add(element);
                } else {
                    SchemaElement element = new SchemaElement();
                    element.setId(i);
                    element.setName("name" + i);
                    element.setDescription("description" + i);
                    element.setBase(i);
                    info2.getModel().getChildElements(info2, i).add(element);
                }
            }
            FilteredSchemaInfo f2 = new FilteredSchemaInfo(info2);

            Schema schema1 = new Schema();
            schema1.setName("blah2");

            SchemaModel schemaModel1 = new RelationalSchemaModel();
            schemaModel1.setName("blahModel2");
            SchemaInfo schemaInfo1 = new SchemaInfo(schema1, new ArrayList<>(), new ArrayList<>());
            HierarchicalSchemaInfo info1 = new HierarchicalSchemaInfo(schemaInfo1);
            info1.setModel(schemaModel1);
            for (int i = 0; i < 10; i++) {
                if (i == 0 || i == 1) {
                    SchemaElement element = new SchemaElement();
                    element.setId(i);
                    element.setName("name" + i);
                    element.setDescription("description" + i);
                    element.setBase(i);
                    info1.getModel().getRootElements(info1).add(element);
                } else {
                    SchemaElement element = new SchemaElement();
                    element.setId(i);
                    element.setName("name" + i);
                    element.setDescription("description" + i);
                    element.setBase(i);
                    info1.getModel().getChildElements(info1, i).add(element);
                }
            }
            FilteredSchemaInfo f1 = new FilteredSchemaInfo(info1);

            logger.info("*******");
            logger.info(info1.getModel().getRootElements(info1).size() + "");
            logger.info(info1.getModel().getRootElements(info1).toString());
            logger.info(info1.getModel().getChildElements(info1, 2).size() + "");
            logger.info(info1.getModel().getChildElements(info1, 2).toString());
            logger.info(f1.getFilteredElements().toString());


            logger.info(info2.getModel().getRootElements(info2).size() + "");
            logger.info(info2.getModel().getRootElements(info2).toString());
            logger.info(info2.getModel().getChildElements(info2, 2).size() + "");
            logger.info(info2.getModel().getChildElements(info2, 2).toString());
            logger.info(f2.getFilteredElements().toString());
            logger.info("*******");

            Matcher quickMatcher = MatcherManager.getMatcher("org.mitre.harmony.matchers.matchers.EntityMatcher");
            assertNotNull(quickMatcher);
            quickMatcher.initialize(f1, f2);

            //Setup the MatcherParameters
            /*ArrayList<MatcherParameter> matcherParameters = exactMatcher.getParameters();
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

            MatcherScores matcherScores = quickMatcher.match();
            logger.info(matcherScores.getScoreCeiling().toString());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    //@Test
    public void testFirstEndToEndMapping() throws Exception
    {
        ArrayList<SchemaElement> schemaElements = this.parseSchemaElements();

        HierarchicalSchemaInfo info1 = this.getHierarchialSchema("source", schemaElements);
        FilteredSchemaInfo f1 = new FilteredSchemaInfo(info1);

        HierarchicalSchemaInfo info2 = this.getHierarchialSchema("destination", schemaElements);
        FilteredSchemaInfo f2 = new FilteredSchemaInfo(info2);


        Map<SchemaElement, Double> scores  = this.findMatches(f1, f2, schemaElements);
        logger.info("Scores: "+scores.toString());
        this.performMapping(scores);
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

    private ArrayList<SchemaElement> parseSchemaElements() throws IOException
    {
        ArrayList<SchemaElement> schemaElements = new ArrayList<>();

        String json = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("airlinesData.json"),
                StandardCharsets.UTF_8);

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        Set<String> fields = jsonObject.keySet();
        for(String field:fields)
        {
            SchemaElement schemaElement = new SchemaElement();
            schemaElement.setId(field.hashCode());
            schemaElement.setName(field);
            schemaElement.setDescription(field);
            schemaElements.add(schemaElement);
        }

        return schemaElements;
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
