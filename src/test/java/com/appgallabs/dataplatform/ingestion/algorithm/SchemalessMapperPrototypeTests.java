package com.appgallabs.dataplatform.ingestion.algorithm;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.*;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class SchemalessMapperPrototypeTests {
    private static Logger logger = LoggerFactory.getLogger(SchemalessMapperPrototypeTests.class);

    @Test
    public void mapAll() throws Exception {
        String jsonString = IOUtils.toString(Thread.currentThread().
                getContextClassLoader().getResourceAsStream("ingestion/algorithm/input.json"),
                StandardCharsets.UTF_8
        );

        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(jsonString);
        JsonUtil.printStdOut(JsonParser.parseString(flattenJson.toString()));

        System.out.println(flattenJson);
        String nestedJson = JsonUnflattener.unflatten(flattenJson.toString());
        JsonUtil.printStdOut(JsonParser.parseString(nestedJson));
    }

    @Test
    public void prototypeJsonPath() throws Exception{
        String json = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/input.json"),
                StandardCharsets.UTF_8
        );

        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
        ReadContext readContext = JsonPath.parse(document);

        Map<String,Object> flattenedJson = new LinkedHashMap<>();
        EvaluationListener evaluationListener = foundResult -> {
            String dotNotationPath = convertPathToDotNotation(foundResult.path());
            if(dotNotationPath.equalsIgnoreCase("ignore")){
                return EvaluationListener.EvaluationContinuation.CONTINUE;
            }

            Object value = foundResult.result();

            flattenedJson.put(dotNotationPath, value);
            return EvaluationListener.EvaluationContinuation.CONTINUE;
        };

        //readContext.withListeners(evaluationListener).read("$.store.book");
        //readContext.withListeners(evaluationListener).read("$.store.book[1].author");

        //TODO
        readContext.withListeners(evaluationListener).
                read("$.store.book[?(@.price < 9)]");

        Gson gson = new Gson();
        String flattenedJsonString = gson.toJson(flattenedJson,LinkedHashMap.class);
        JsonUtil.printStdOut(JsonParser.parseString(flattenedJsonString));

        System.out.println("*************************************************");

        String nestedJson = JsonUnflattener.unflatten(flattenedJsonString);
        JsonElement jsonElement = JsonParser.parseString(nestedJson);
        JsonUtil.printStdOut(jsonElement);
    }

    private String convertPathToDotNotation(String path){
        if(path.startsWith("@")){
            return "ignore";
        }
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<path.length(); i++){
            int token = path.charAt(i);
            switch(token){
                case '[':
                    if(Character.isDigit(path.charAt(i+1))){
                        builder.deleteCharAt(builder.toString().length()-1);
                        builder.append("["+path.charAt(i+1)+"].");
                    }else{
                        int startIndex = i+2;
                        int endIndex = path.indexOf('\'',startIndex);
                        String variable = path.substring(startIndex,endIndex);
                        builder.append(variable+".");
                    }
                break;

                default:
                    //ignore this character
            }
        }

        String dotNotation = builder.toString();
        if(dotNotation.endsWith(".")){
            dotNotation = dotNotation.substring(0,dotNotation.length()-1);
        }

        return dotNotation;
    }
}
