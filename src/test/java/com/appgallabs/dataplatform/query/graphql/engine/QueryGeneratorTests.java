package com.appgallabs.dataplatform.query.graphql.engine;

import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;

import graphql.language.Document;
import graphql.parser.Parser;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

@QuarkusTest
public class QueryGeneratorTests {
    private static Logger logger = LoggerFactory.getLogger(QueryGeneratorTests.class);

    @Inject
    private SchemalessMapper schemalessMapper;

    @Test
    public void test() throws Exception{
        String querySql = "query findTeas{\n" +
                "  teas{\n" +
                "    id\n" +
                "    name\n" +
                "    description\n" +
                "  }\n" +
                "}";
        Parser parser = new Parser();
        Document document = parser.parseDocument(querySql);

        System.out.println(document);
    }
}
