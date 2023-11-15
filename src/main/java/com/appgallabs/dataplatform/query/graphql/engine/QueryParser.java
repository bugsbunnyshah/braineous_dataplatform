package com.appgallabs.dataplatform.query.graphql.engine;

import graphql.language.*;
import graphql.parser.Parser;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class QueryParser {

    public QueryParserResult parseQuery(String entity, String graphSqlQuery){
        QueryParserResult result = new QueryParserResult();

        //Prepare Document
        Parser parser = new Parser();
        Document document = parser.parseDocument(graphSqlQuery);

        //parse to GraphQL-Java components
        List<Definition> definitions = document.getDefinitions();
        OperationDefinition operationDefinition = (OperationDefinition) definitions.get(0);
        SelectionSet selectionSet = operationDefinition.getSelectionSet();

        //Parse the projection fields
        List<String> projectionFields = new ArrayList<>();
        List<Selection> selections = selectionSet.getSelections();
        for(Selection selection:selections){
            Field field = (Field) selection;
            SelectionSet projectionSet = field.getSelectionSet();
            List<Selection> projectionSelections = projectionSet.getSelections();
            for(Selection projectionSelection:projectionSelections){
                Field projectionField = (Field) projectionSelection;
                projectionFields.add(projectionField.getName());
            }
        }

        result.setEntity(entity);
        result.setProjectionFields(projectionFields);

        //TODO: add criteria
        Map<String, String> criteria  = new HashMap<String, String>() {{
            put("diff", "0");
            put("name", "hello");
        }};
        result.setCriteria(criteria);

        return result;
    }

}
