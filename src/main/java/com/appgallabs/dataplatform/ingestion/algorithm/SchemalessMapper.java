package com.appgallabs.dataplatform.ingestion.algorithm;


import com.github.wnameless.json.flattener.JsonFlattener;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.EvaluationListener;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class SchemalessMapper {
    private static Logger logger = LoggerFactory.getLogger(SchemalessMapper.class);

    public Map<String,Object> mapAll(String json){
        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
        return flattenJson;
    }

    public Map<String,Object> mapSubset(String json, List<String> queries){
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

        for(String query:queries){
            readContext.withListeners(evaluationListener).read(query);
        }

        return flattenedJson;
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
