package com.appgallabs.dataplatform.ingestion.pipeline;


import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableDescriptor;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.catalog.ObjectPath;
import org.junit.jupiter.api.Test;
import test.components.BaseTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@QuarkusTest
public class DataLifeCycleTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(DataLifeCycleTests.class);

    @Inject
    private SchemalessMapper schemalessMapper;

    @Inject
    private DataLakeTableGenerator dataLakeTableGenerator;

    @Inject
    private DataLakeSessionManager dataLakeSessionManager;

    @Inject
    private DataLakeSqlGenerator dataLakeSqlGenerator;

    @Inject
    private PipelineService pipelineService;

    @Test
    public void updateTableSchema() throws Exception{
        //get base object
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/pipeline/dynamic_table.json"),
                StandardCharsets.UTF_8
        );

        //prepare a progressing payload
        JsonObject baseObject = JsonUtil.validateJson(jsonString).getAsJsonObject();
        JsonArray payload = new JsonArray();
        payload.add(baseObject);
        for(int i=0; i<3; i++) {
            JsonObject jsonObject = JsonUtil.validateJson(jsonString).getAsJsonObject();
            this.preparePayload(payload, jsonObject, (i+1));
        }
        JsonUtil.printStdOut(payload);

        Map<String, Object> baseRow = this.schemalessMapper.mapAll(baseObject.toString());

        //create a table
        String tableName = RandomStringUtils.randomAlphabetic(5).toLowerCase();
        String table = this.createTable(tableName, baseRow);
        this.printSchema(table);

        //update the table
        table = this.updateTable(table, baseRow);
        this.printSchema(table);
    }

    private void preparePayload(JsonArray payload, JsonObject jsonObject,
                                     int numberOfNewColumns){
        for(int i=0; i<numberOfNewColumns; i++) {
            String newColumn = RandomStringUtils.randomAlphabetic(5).toLowerCase();
            jsonObject.addProperty(newColumn, "update_success");
        }
        payload.add(jsonObject);
    }

    private String createTable(String tableName, Map<String,Object> row) throws Exception{
        String name  = "myhive";
        String database = "mydatabase";
        final StreamTableEnvironment tableEnv = this.dataLakeSessionManager.newDataLakeSessionWithNewDatabase(
                this.pipelineService.getEnv(),
                name,
                database
        );

        String table = name + "." + database + "." + tableName;
        String objectPath = database + "." + tableName;
        String filePath = "file:///Users/babyboy/datalake/"+ tableName;
        String format = "csv";

        String currentCatalog = tableEnv.getCurrentCatalog();
        Optional<Catalog> catalog = tableEnv.getCatalog(currentCatalog);
        boolean tableExists = catalog.get().tableExists(ObjectPath.fromString(objectPath));

        System.out.println("**********************************");
        System.out.println("TABLE: "+table);
        if(!tableExists) {
            TableDescriptor tableDescriptor = this.dataLakeTableGenerator.createFileSystemTable(row,
                    filePath,
                    format);

            tableEnv.createTable(table, tableDescriptor);
            System.out.println("TABLE: CREATE_SUCCESS");
        }else{
            System.out.println("TABLE: EXISTS_ALREADY");
        }
        System.out.println("**********************************");

        return table;
    }

    private void printSchema(String table){
        String name  = "myhive";
        final StreamTableEnvironment tableEnv = this.dataLakeSessionManager.newDataLakeCatalogSession(
                this.pipelineService.getEnv(),
                name
        );

        Table data = tableEnv.from(table);
        System.out.println(data.getResolvedSchema().toString());
    }

    private String updateTable(String table,Map<String,Object> row) throws Exception{
        String newColumn = RandomStringUtils.randomAlphabetic(5).toLowerCase();

        String name  = "myhive";
        String database = "mydatabase";
        final StreamTableEnvironment tableEnv = this.dataLakeSessionManager.newDataLakeCatalogSession(
                this.pipelineService.getEnv(),
                name
        );

        //String objectPath = database + "." + this.tableName;

        String currentCatalog = tableEnv.getCurrentCatalog();
        Optional<Catalog> catalog = tableEnv.getCatalog(currentCatalog);

        final TableResult updateTableResult = tableEnv.
                executeSql("ALTER TABLE "+table+" ADD `" +newColumn+ "` String NULL");
        // since all cluster operations of the Table API are executed asynchronously,
        // we need to wait until the insertion has been completed,
        // an exception is thrown in case of an error
        updateTableResult.await();

        //CatalogBaseTable tableToBeAltered = catalog.get().getTable(ObjectPath.fromString(objectPath));
        /*Table tableToBeAltered = tableEnv.from(table);

        Table result = tableToBeAltered.addColumns($(newColumn).as(newColumn));
        System.out.println(result.getResolvedSchema().toString());*/


        //catalog.get().alterTable(ObjectPath.fromString(objectPath),
        //        (CatalogBaseTable) result, false);

        return table;
    }
}
