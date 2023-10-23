package com.appgallabs.dataplatform.query.graphql.engine;

import com.appgallabs.dataplatform.infrastructure.DataLakeStore;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.PipelineStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;

import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import graphql.language.*;
import graphql.parser.Parser;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.BaseTest;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@QuarkusTest
public class QueryGeneratorTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(QueryGeneratorTests.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private SchemalessMapper schemalessMapper;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Test
    public void queryByAll() throws Exception{
        Tenant tenant = this.securityTokenContainer.getTenant();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();
        DataLakeStore dataLakeStore = this.mongoDBJsonStore.getDataLakeStore();

        String querySql = "query findTeas{\n" +
                "  teas{\n" +
                "    id\n" +
                "    name2\n" +
                "    description\n" +
                "  }\n" +
                "}";
        Parser parser = new Parser();
        Document document = parser.parseDocument(querySql);

        List<Definition> definitions = document.getDefinitions();

        OperationDefinition operationDefinition = (OperationDefinition) definitions.get(0);
        SelectionSet selectionSet = operationDefinition.getSelectionSet();

        List<String> fieldNames = new ArrayList<>();
        List<Selection> selections = selectionSet.getSelections();
        for(Selection selection:selections){
            Field field = (Field) selection;
            SelectionSet whereSet = field.getSelectionSet();
            List<Selection> whereSelections = whereSet.getSelections();
            for(Selection whereSelection:whereSelections){
                Field whereField = (Field) whereSelection;
                fieldNames.add(whereField.getName());
            }
        }

        //MongoDB finalAll query: db.datalake.find({},{name: 1})
        //JsonUtil.printStdOut(JsonUtil.validateJson(fieldNames.toString()));
        JsonObject queryJson = new JsonObject();
        JsonObject projectionJson = new JsonObject();
        for(String fieldName:fieldNames){
            projectionJson.addProperty(fieldName,1);
        }
        //JsonUtil.printStdOut(queryJson);
        //JsonUtil.printStdOut(projectionJson);

        //Execute the query
        String entity = "books";
        JsonArray result = dataLakeStore.readByEntity(tenant,mongoClient,
                entity, fieldNames);
        JsonUtil.printStdOut(result);

        //Process response
    }
}
