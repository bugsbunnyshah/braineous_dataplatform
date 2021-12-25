package org.mitre.harmony.matchers;

import org.junit.jupiter.api.Test;
import org.mitre.harmony.matchers.matchers.Matcher;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.FilteredSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.SchemaInfo;
import org.mitre.schemastore.model.schemaInfo.model.RelationalSchemaModel;
import org.mitre.schemastore.model.schemaInfo.model.SchemaModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AirlineDataTests {
    private static Logger logger = LoggerFactory.getLogger(AirlineDataTests.class);

    @Test
    public void testMatchingScenerioSimple() throws Exception
    {
        try
        {
            Schema schema1 = new Schema();
            schema1.setName("destination");

            SchemaModel schemaModel1 = new RelationalSchemaModel();
            schemaModel1.setName("destinationModel");
            SchemaInfo schemaInfo1 = new SchemaInfo(schema1, new ArrayList<>(), new ArrayList<>());
            HierarchicalSchemaInfo info1 = new HierarchicalSchemaInfo(schemaInfo1);
            info1.setModel(schemaModel1);

            this.addElement(info1, 0, "Id", "Id");
            this.addElement(info1, 1, "Rcvr", "Rcvr");
            this.addElement(info1, 2, "HasSig", "HasSig");
            FilteredSchemaInfo f1 = new FilteredSchemaInfo(info1);

            logger.info("*******");
            logger.info(info1.getModel().getChildElements(info1, 0).toString());
            logger.info(info1.getModel().getChildElements(info1, 1).toString());
            logger.info(info1.getModel().getChildElements(info1, 2).toString());
            logger.info("*******");

            Schema schema2 = new Schema();
            schema2.setName("source");

            SchemaModel schemaModel2 = new RelationalSchemaModel();
            schemaModel2.setName("sourceModel");
            SchemaInfo schemaInfo2 = new SchemaInfo(schema2, new ArrayList<>(), new ArrayList<>());
            HierarchicalSchemaInfo info2 = new HierarchicalSchemaInfo(schemaInfo2);
            info2.setModel(schemaModel2);

            this.addElement(info2, 0, "id", "id");
            this.addElement(info2, 1, "rcvr", "rcvr");
            this.addElement(info2, 2, "hassig", "hassig");
            FilteredSchemaInfo f2 = new FilteredSchemaInfo(info2);

            logger.info("*******");
            logger.info(info2.getModel().getChildElements(info2, 0).toString());
            logger.info(info2.getModel().getChildElements(info2, 1).toString());
            logger.info(info2.getModel().getChildElements(info2, 2).toString());
            logger.info("*******");

            Matcher quickMatcher = MatcherManager.getMatcher("org.mitre.harmony.matchers.matchers.QuickMatcher");
            assertNotNull(quickMatcher);
            quickMatcher.initialize(f1, f2);

            MatcherScores matcherScores = quickMatcher.match();
            logger.info(matcherScores.getScoreCeiling().toString());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    private void addElement(HierarchicalSchemaInfo info1, int id, String name, String description)
    {
        SchemaElement element = new SchemaElement();
        element.setId(id);
        element.setName(name);
        element.setDescription(description);
        element.setBase(id);
        info1.getModel().getChildElements(info1, id).add(element);
    }
}
