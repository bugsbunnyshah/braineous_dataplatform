package com.appgallabs.dataplatform.query.graphql;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;


@QuarkusTest
public class GraphQLTests {
    private static Logger logger = LoggerFactory.getLogger(GraphQLTests.class);

    @Test
    public void testAllProducts() throws Exception{
        String queryJson = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("graphql/datafetcher.json"),
                StandardCharsets.UTF_8);
        JsonElement jsonElement = JsonParser.parseString(queryJson);
        JsonUtil.printStdOut(jsonElement);
    }
}
