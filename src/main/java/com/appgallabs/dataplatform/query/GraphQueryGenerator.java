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

    public String generateWhereClause(String entity,JsonObject criteria)
    {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("WHERE ");
        StringBuilder criteriaBuilder = new StringBuilder();
        Set<Map.Entry<String, JsonElement>> entrySet = criteria.entrySet();
        for(Map.Entry<String,JsonElement> entry:entrySet)
        {
            String key = entry.getKey();
            JsonElement element = entry.getValue();
            String left = entity+"."+key+"=";
            Object right;
            if(element.isJsonPrimitive())
            {
                JsonPrimitive primitive = element.getAsJsonPrimitive();
                right = primitive.getAsString();
                criteriaBuilder.append(left+"'"+right+"'");
            }
        }
        queryBuilder.append(criteriaBuilder);
        return queryBuilder.toString();
    }
}
