package com.appgallabs.dataplatform.query;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;

@Singleton
public class GraphQueryGenerator {
    private static Logger logger = LoggerFactory.getLogger(GraphQueryGenerator.class);

    public String generateQueryByCriteria(String entity, JsonObject criteria)
    {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * WHERE {?element v:label \""+entity+"\" . ");
        Set<Map.Entry<String, JsonElement>> entrySet = criteria.entrySet();
        for(Map.Entry<String,JsonElement> entry:entrySet)
        {
            String key = entry.getKey();
            JsonElement element = entry.getValue();
            String left = "v:"+key;
            Object right;
            if(element.isJsonPrimitive())
            {
                JsonPrimitive primitive = element.getAsJsonPrimitive();
                right = primitive.getAsString();
                queryBuilder.append("?element "+left+" \""+right+"\" . ");
            }
        }
        queryBuilder.append("}");
        return queryBuilder.toString();
    }

    public String generateNavigationQuery(String entity, String relationship, JsonObject criteria)
    {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * WHERE {");
        queryBuilder.append("?"+entity+" v:label \""+entity+"\" . ");
        queryBuilder.append("?"+entity+" e:edge_"+relationship+" ?"+relationship+" . ");
        StringBuilder criteriaBuilder = new StringBuilder();
        Set<Map.Entry<String, JsonElement>> entrySet = criteria.entrySet();
        for(Map.Entry<String,JsonElement> entry:entrySet)
        {
            String key = entry.getKey();
            JsonElement element = entry.getValue();
            String left = "v:"+key;
            Object right;
            if(element.isJsonPrimitive())
            {
                JsonPrimitive primitive = element.getAsJsonPrimitive();
                right = primitive.getAsString();
                criteriaBuilder.append("?"+relationship+" "+left+" \""+right+"\" . ");
            }
        }
        queryBuilder.append(criteriaBuilder);
        queryBuilder.append("}");

        return queryBuilder.toString();
    }
}
