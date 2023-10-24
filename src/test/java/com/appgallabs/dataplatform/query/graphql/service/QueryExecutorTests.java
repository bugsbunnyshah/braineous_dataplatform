package com.appgallabs.dataplatform.query.graphql.service;


import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonArray;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.BaseTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.inject.Inject;

@QuarkusTest
public class QueryExecutorTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(QueryExecutorTests.class);

    @Inject
    private QueryExecutor queryExecutor;

    @Test
    public void executeQueryNoCriteria() throws Exception{
        String entity = "books";

        String querySql = "query findTeas{\n" +
                "  teas{\n" +
                "    name\n" +
                "    value\n" +
                "    diff\n" +
                "  }\n" +
                "}";

        JsonArray result = this.queryExecutor.executeQueryNoCriteria(entity, querySql);
        JsonUtil.printStdOut(result);

        //TODO assert
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    public void executeQueryByANDCriteria() throws Exception{
        String entity = "books";

        String querySql = "query findTeas{\n" +
                "  teas{\n" +
                "    name\n" +
                "    value\n" +
                "    diff\n" +
                "  }\n" +
                "}";

        JsonArray result = this.queryExecutor.executeQueryByANDCriteria(entity, querySql);
        JsonUtil.printStdOut(result);

        //TODO assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void executeQueryByORCriteria() throws Exception{
        String entity = "books";

        String querySql = "query findTeas{\n" +
                "  teas{\n" +
                "    name\n" +
                "    value\n" +
                "    diff\n" +
                "  }\n" +
                "}";

        JsonArray result = this.queryExecutor.executeQueryByORCriteria(entity, querySql);
        JsonUtil.printStdOut(result);

        //TODO assert
        assertNotNull(result);
        assertEquals(3, result.size());
    }
}
