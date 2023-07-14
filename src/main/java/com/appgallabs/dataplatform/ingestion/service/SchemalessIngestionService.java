package com.appgallabs.dataplatform.ingestion.service;

import com.appgallabs.dataplatform.datalake.DataLakeDriver;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonArray;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.EvaluationListener;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;

@ApplicationScoped
public class SchemalessIngestionService {
    private static Logger logger = LoggerFactory.getLogger(SchemalessIngestionService.class);

    @Inject
    Instance<DataLakeDriver> dataLakeDriverInstance;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    public JsonArray readIngestion(String dataLakeId){
        DataLakeDriver dataLakeDriver = dataLakeDriverInstance.select(NamedLiteral.of("braineous://datalake/mongodb")).get();
        Tenant tenant = securityTokenContainer.getTenant();
        JsonArray result = dataLakeDriver.readIngestion(tenant,dataLakeId);
        return result;
    }

    public String processFull(String jsonString){
        DataLakeDriver dataLakeDriver = dataLakeDriverInstance.select(NamedLiteral.of("braineous://datalake/mongodb")).get();
       Tenant tenant = securityTokenContainer.getTenant();

        Map<String, Object> flattenedJsonMap = JsonFlattener.flattenAsMap(jsonString);

       String datalakeId = dataLakeDriver.storeIngestion(tenant, flattenedJsonMap);

       return datalakeId;
    }

    public String processSubset(String jsonString, List<String> jsonPathExpressions){
        DataLakeDriver dataLakeDriver = dataLakeDriverInstance.select(NamedLiteral.of("braineous://datalake/mongodb")).get();
        Tenant tenant = securityTokenContainer.getTenant();

        Object document = Configuration.defaultConfiguration().jsonProvider().parse(jsonString);
        ReadContext readContext = JsonPath.parse(document);

        Map<String,Object> flattenedJsonMap = new LinkedHashMap<>();
        EvaluationListener evaluationListener = foundResult -> {
            String dotNotationPath = convertPathToDotNotation(foundResult.path());
            if(dotNotationPath.equalsIgnoreCase("ignore")){
                return EvaluationListener.EvaluationContinuation.CONTINUE;
            }

            Object value = foundResult.result();

            flattenedJsonMap.put(dotNotationPath, value);
            return EvaluationListener.EvaluationContinuation.CONTINUE;
        };

        for(String jsonPathExpression:jsonPathExpressions){
            readContext.withListeners(evaluationListener).read(jsonPathExpression);
        }

        String datalakeId = dataLakeDriver.storeIngestion(tenant, flattenedJsonMap);

        return datalakeId;
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
