package org.mitre.schemaInfo;

import org.junit.jupiter.api.Test;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.SchemaInfo;
import org.mitre.schemastore.model.schemaInfo.model.RelationalSchemaModel;
import org.mitre.schemastore.model.schemaInfo.model.SchemaModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class HierarchialSchemaInfoTests {
    private static Logger logger = LoggerFactory.getLogger(HierarchialSchemaInfoTests.class);

    @Test
    public void testHierarchialSchemaInfo() throws Exception
    {
        Schema schema = new Schema();
        schema.setName("blah");

        SchemaModel schemaModel = new RelationalSchemaModel();
        schemaModel.setName("blahModel");
        SchemaInfo schemaInfo = new SchemaInfo(schema, new ArrayList<>(), new ArrayList<>());
        HierarchicalSchemaInfo info = new HierarchicalSchemaInfo(schemaInfo);
        info.setModel(schemaModel);
        for(int i=0; i<10; i++) {
            if(i == 0 || i == 1)
            {
                SchemaElement element = new SchemaElement();
                element.setId(i);
                element.setName("name"+i);
                element.setDescription("description"+i);
                element.setBase(i);
                info.getModel().getRootElements(info).add(element);
            }
            else
            {
                SchemaElement element = new SchemaElement();
                element.setId(i);
                element.setName("name"+i);
                element.setDescription("description"+i);
                element.setBase(i);
                info.getModel().getChildElements(info, i).add(element);
            }
        }

        logger.info("*******");
        logger.info(info.getModel().getRootElements(info).size()+"");
        logger.info(info.getModel().getRootElements(info).toString());
        logger.info(info.getModel().getChildElements(info, 2).size()+"");
        logger.info(info.getModel().getChildElements(info, 2).toString());
        logger.info("*******");
    }
}
