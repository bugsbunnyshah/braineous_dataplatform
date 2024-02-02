package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.*;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.catalog.CatalogDatabaseImpl;
import org.apache.flink.table.catalog.hive.HiveCatalog;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;

@QuarkusTest
public class FlinkTableCreationTests {
    private static Logger logger = LoggerFactory.getLogger(FlinkTableCreationTests.class);

    @Inject
    private SchemalessMapper schemalessMapper;

    private StreamExecutionEnvironment env;

    @Test
    public void testDynamicSchema() throws Exception{
        this.env = StreamExecutionEnvironment.createRemoteEnvironment(
                "localhost",
                Integer.parseInt("8081"),
                "dataplatform-1.0.0-cr2-runner.jar"
        );


        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/input_array.json"),
                StandardCharsets.UTF_8
        );

        JsonArray jsonArray = JsonUtil.validateJson(jsonString).getAsJsonArray();

        List<Map<String, Object>> flatArray = new ArrayList();
        for(int i=0; i<jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            Map<String, Object> flatJson = this.schemalessMapper.mapAll(jsonObject.toString());
            flatArray.add(flatJson);
        }

        Map<String, Object> row = flatArray.get(0);

        String table = this.createTable(row);
        this.addData(table, flatArray);
    }

    private String createTable(Map<String,Object> row) throws Exception{
        // set up the Java Table API
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(this.env);

        // Create a HiveCatalog
        String name            = "myhive";
        String database = "mydatabase";
        String hiveConfDir     = "/Users/babyboy/mumma/braineous/infrastructure/apache-hive-3.1.3-bin/conf";
        String tableName = RandomStringUtils.randomAlphabetic(5).toLowerCase();
        String table = name + "." + database + "." + tableName;

        HiveCatalog hive = new HiveCatalog(name, null, hiveConfDir);
        tableEnv.registerCatalog(name, hive);

        // set the HiveCatalog as the current catalog of the session
        tableEnv.useCatalog(name);

        // Create a catalog database
        hive.createDatabase(database,
                new CatalogDatabaseImpl(new HashMap<>(), "db_metadata"), true);

        Schema.Builder schemaBuilder = Schema.newBuilder();
        Set<String> columnNames = row.keySet();

        for(String columnName: columnNames){
            schemaBuilder.column(columnName, DataTypes.STRING());
        }

        Schema schema = schemaBuilder.build();
        logger.info(schema.toString());

        TableDescriptor tableDescriptor = TableDescriptor.forConnector("filesystem")
                .option("path", "file:///Users/babyboy/datalake/"+tableName)
                .option("format", "csv")
                .schema(schema)
                .build();

        tableEnv.createTable(table, tableDescriptor);

        return table;
    }

    private void addData(String table, List<Map<String,Object>> rows) throws Exception{
        // set up the Java Table API
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(this.env);

        // Create a HiveCatalog
        String name            = "myhive";
        String hiveConfDir     = "/Users/babyboy/mumma/braineous/infrastructure/apache-hive-3.1.3-bin/conf";

        HiveCatalog hive = new HiveCatalog(name, null, hiveConfDir);
        tableEnv.registerCatalog(name, hive);

        // set the HiveCatalog as the current catalog of the session
        tableEnv.useCatalog(name);

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO " + table + " VALUES {0}");
        String insertSqlTemplate = sqlBuilder.toString();

        StringBuilder batchBuilder = new StringBuilder();
        for(Map<String,Object> row:rows) {
            Collection<Object> values = row.values();
            StringBuilder rowBuilder = new StringBuilder();
            rowBuilder.append("(");
            StringBuilder valueBuilder = new StringBuilder();
            for (Object value : values) {
                String insert = "'" + value + "',";
                valueBuilder.append(insert);
            }
            String valueBuilderStr = valueBuilder.toString();
            String rowValue = valueBuilderStr.substring(0, valueBuilderStr.length()-1);
            rowBuilder.append(rowValue);
            rowBuilder.append("),");
            batchBuilder.append(rowBuilder+"\n");
        }

        String batchBuilderStr = batchBuilder.toString();
        String insertValues = batchBuilderStr.substring(0, batchBuilderStr.length()-2);

        String insertSql = MessageFormat.format(insertSqlTemplate, insertValues);
        logger.info("*********************************");
        logger.info(insertSql);
        logger.info("*********************************");

        // insert some example data into the table
        final TableResult insertionResult =
                tableEnv.executeSql(insertSql);

        // since all cluster operations of the Table API are executed asynchronously,
        // we need to wait until the insertion has been completed,
        // an exception is thrown in case of an error
        insertionResult.await();
    }
}
