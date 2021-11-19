package com.appgallabs.dataplatform.history;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.flattener.JsonifyLinkedHashMap;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class ObjectDiffAlgorithm
{
    private static Logger logger = LoggerFactory.getLogger(ObjectDiffAlgorithm.class);

    public JsonObject diff(JsonObject left, JsonObject right)
    {
        Map<String, Object> leftMap = JsonFlattener.flattenAsMap(left.toString());
        Map<String, Object> rightMap = JsonFlattener.flattenAsMap(right.toString());

        Map<String, Object> diffMap = new JsonifyLinkedHashMap();
        if(leftMap.size() > rightMap.size() || leftMap.size() == rightMap.size())
        {
            Set<Map.Entry<String, Object>> entrySet = leftMap.entrySet();
            for (Map.Entry<String, Object> entry : entrySet)
            {
                String key = entry.getKey();
                boolean doesRightMapHaveTheKey = rightMap.containsKey(key);

                //Check for a FIELD Update
                if (doesRightMapHaveTheKey)
                {
                    Object leftValue = entry.getValue();
                    Object rightValue = rightMap.get(key);
                    if(rightValue != null)
                    {
                        if(leftValue == null)
                        {

                        }
                        if (leftValue == null ||
                                (leftValue.hashCode() != rightValue.hashCode()))
                        {
                            diffMap.put(key, rightValue);
                        }
                    }
                }
                else
                    {
                    //This means a FIELD was DELETED, then DO_NOTHING
                }
            }
        }
        else
        {
            Set<Map.Entry<String, Object>> entrySet = rightMap.entrySet();
            for (Map.Entry<String, Object> entry : entrySet)
            {
                String key = entry.getKey();
                boolean doesLeftMapHaveTheKey = leftMap.containsKey(key);

                //Check for a FIELD Update
                if (doesLeftMapHaveTheKey)
                {
                    Object rightValue = entry.getValue();
                    Object leftValue = leftMap.get(key);
                    if(leftValue != null)
                    {
                        if(rightValue == null)
                        {

                        }
                        if (rightValue == null ||
                                (rightValue.hashCode() != leftValue.hashCode()))
                        {
                            diffMap.put(key, rightValue);
                        }
                    }
                }
                else
                {
                    //This means a FIELD was ADDED
                    diffMap.put(key, rightMap.get(key));
                }
            }
        }

        JsonObject diff = JsonParser.parseString(JsonUnflattener.unflatten(diffMap.toString())).getAsJsonObject();

        return diff;
    }

    public JsonObject merge(JsonObject left, JsonObject right)
    {
        JsonObject jsonObject;

        Map<String, Object> leftMap = JsonFlattener.flattenAsMap(left.toString());
        Map<String, Object> rightMap = JsonFlattener.flattenAsMap(right.toString());
        Map<String, Object> mergeMap = new JsonifyLinkedHashMap();

        Set<Map.Entry<String, Object>> entrySet = leftMap.entrySet();
        for (Map.Entry<String, Object> entry : entrySet)
        {
            String key = entry.getKey();
            boolean doesRightMapHaveTheKey = rightMap.containsKey(key);
            if(entry.getValue() != null && doesRightMapHaveTheKey) {
                mergeMap.put(entry.getKey(), entry.getValue());
            }
            else{
                //THIS MEANS the FIELD WAS DELETED
            }
        }

        //Process the diff
        entrySet = rightMap.entrySet();
        for (Map.Entry<String, Object> entry : entrySet)
        {
            String key = entry.getKey();
            boolean doesRightMapHaveTheKey = rightMap.containsKey(key);
            boolean doesLeftMapHaveTheKey = leftMap.containsKey(key);


            if (doesRightMapHaveTheKey)
            {
                if(!leftMap.containsKey(key))
                {
                    //This means a FIELD was ADDED
                    if(rightMap.get(key) != null) {
                        mergeMap.put(key, rightMap.get(key));
                    }
                }
                else {
                    int valueHash = entry.getValue().hashCode();
                    int compareHash = leftMap.get(key).hashCode();
                    if (valueHash != compareHash) {
                        //This means a FIELD was UPDATED
                        if (entry.getValue() != null) {
                            mergeMap.put(key, entry.getValue());
                        }
                    }
                }
            }
        }

        System.out.println(mergeMap.toString());

        jsonObject = JsonParser.parseString(JsonUnflattener.unflatten(mergeMap.toString())).getAsJsonObject();

        return jsonObject;
    }
}
