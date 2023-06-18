package com.appgallabs.dataplatform.ingestion.algorithm;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.*;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeclarativeMapperTests {
    private static Logger logger = LoggerFactory.getLogger(DeclarativeMapperTests.class);

    @Test
    public void mapAll() throws Exception {
        String jsonString = IOUtils.toString(Thread.currentThread().
                getContextClassLoader().getResourceAsStream("ingestion/algorithm/mapAll.json"),
                StandardCharsets.UTF_8
        );

        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(jsonString);
        JsonUtil.printStdOut(JsonParser.parseString(flattenJson.toString()));

        String nestedJson = JsonUnflattener.unflatten(flattenJson.toString());
        JsonUtil.printStdOut(JsonParser.parseString(nestedJson));
    }

    @Test
    public void processJsonSubset() throws Exception{
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/mapAll.json"),
                StandardCharsets.UTF_8
        );

        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
        JsonUtil.printStdOut(json);
    }

    @Test
    public void prototypeJsonPath() throws Exception{
        String json = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/subset.json"),
                StandardCharsets.UTF_8
        );

        Set<Option> options = Configuration.defaultConfiguration().getOptions();

        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
        ReadContext readContext = JsonPath.parse(document);

        EvaluationListener evaluationListener = new EvaluationListener() {
            @Override
            public EvaluationContinuation resultFound(FoundResult foundResult) {
                String dotNotationPath = convertPathToDotNotation(foundResult.path());
                System.out.println(dotNotationPath);
                System.out.println(foundResult.result());
                System.out.println("************");
                return EvaluationListener.EvaluationContinuation.CONTINUE;
            }
        };

        String author0 = readContext.withListeners(evaluationListener).read("$.store.book[0].author");
        String author1 = readContext.withListeners(evaluationListener).read("$.store.book[1].author");

        List<Map<String, Object>> books =  readContext.withListeners(evaluationListener).
                read("$.store.book[?(@.price < 10)]");
    }

    private String convertPathToDotNotation(String path){
        String dotNotationPath = path;
        return dotNotationPath;
    }
}
