package com.appgallabs.dataplatform.ingestion.pipeline;


import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableDescriptor;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.catalog.ObjectPath;
import org.apache.flink.table.catalog.ResolvedSchema;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.components.BaseTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.Util;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Tenant tenant = this.securityTokenContainer.getTenant();

        String jsonString = Util.loadResource("pipeline/mongodb_config_1.json");

        Registry registry = Registry.getInstance();
        registry.registerPipe(tenant, JsonUtil.validateJson(jsonString).getAsJsonObject());
    }

    @Test
    public void dataLifeCycle() throws Exception{
        String originalObjectHash = null;
        try {
            //get base object
            String jsonString = IOUtils.toString(Thread.currentThread().
                            getContextClassLoader().getResourceAsStream("ingestion/pipeline/dynamic_table.json"),
                    StandardCharsets.UTF_8
            );
            String pipeId = RandomStringUtils.randomAlphabetic(5).toLowerCase();
            String entity = RandomStringUtils.randomAlphabetic(5).toLowerCase();
            JsonObject datalakeDriverConfiguration = Registry.getInstance().getDatalakeConfiguration();


            //create
            this.pipelineService.ingest(this.securityTokenContainer.getSecurityToken(),
                    datalakeDriverConfiguration.toString(),
                    pipeId, 0,
                    entity, jsonString);

            //prepare a progressing payload
            JsonObject baseObject = JsonUtil.validateJson(jsonString).getAsJsonObject();
            JsonArray payload = new JsonArray();
            payload.add(baseObject);
            Map<String, Object> baseRow = this.schemalessMapper.mapAll(baseObject.toString());

            //update
            for(int i=0; i<3; i++) {
                //Thread.sleep(5000);

                int payloadSize = payload.size();
                JsonObject jsonObject = payload.get(payloadSize-1).getAsJsonObject();
                String columnValue = RandomStringUtils.randomAlphabetic(5).toLowerCase();
                String newColumn = this.preparePayload(payload, jsonObject, columnValue);

                //update the table
                String newObject = jsonObject.toString();
                this.pipelineService.ingest(this.securityTokenContainer.getSecurityToken(),
                        datalakeDriverConfiguration.toString(),
                        pipeId, 0,
                        entity, newObject);
            }


            while(true) {
                Thread.sleep(120000l);
            }
        }finally{
            System.out.println(originalObjectHash);
        }
    }

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
        Map<String, Object> baseRow = this.schemalessMapper.mapAll(baseObject.toString());

        //create a table
        String tableName = RandomStringUtils.randomAlphabetic(5).toLowerCase();
        String table = this.createTable(tableName, baseRow);
        this.printSchema(table);

        for(int i=0; i<3; i++) {
            int payloadSize = payload.size();
            JsonObject jsonObject = payload.get(payloadSize-1).getAsJsonObject();
            String columnValue = RandomStringUtils.randomAlphabetic(5).toLowerCase();
            String newColumn = this.preparePayload(payload, jsonObject, columnValue);

            Map<String, Object> flatJson = this.schemalessMapper.mapAll(jsonObject.toString());

            //update the table
            table = this.updateTable(table, newColumn);
            this.printSchema(table);

            //bridge and insert
            this.unstructuredToStructureBridge(table, flatJson);
        }

        //print the data
        this.printData(table);
    }

    private String preparePayload(JsonArray payload, JsonObject jsonObject, String value){
        String newColumn = RandomStringUtils.randomAlphabetic(5).toLowerCase();
        jsonObject.addProperty(newColumn, value);
        payload.add(jsonObject);
        return newColumn;
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
        ResolvedSchema resolvedSchema = data.getResolvedSchema();
        List<String> columnNames = resolvedSchema.getColumnNames();
        System.out.println(data.getResolvedSchema().toString());
    }

    private String updateTable(String table,String newColumn) throws Exception{
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

    private void addData(String table, List<Map<String,Object>> rows) throws Exception{
        String name  = "myhive";
        final StreamTableEnvironment tableEnv = this.dataLakeSessionManager.newDataLakeCatalogSession(
                this.pipelineService.getEnv(),
                name
        );

        Table data = tableEnv.from(table);
        System.out.println(data.getResolvedSchema().toString());

        String insertSql = this.dataLakeSqlGenerator.generateInsertSql(table, rows);
        System.out.println(insertSql);
        // insert some example data into the table
        final TableResult insertionResult =
                tableEnv.executeSql(insertSql);

        // since all cluster operations of the Table API are executed asynchronously,
        // we need to wait until the insertion has been completed,
        // an exception is thrown in case of an error
        insertionResult.await();
    }

    private void printData(String table) throws Exception{
        String name  = "myhive";
        final StreamTableEnvironment tableEnv = this.dataLakeSessionManager.newDataLakeCatalogSession(
                this.pipelineService.getEnv(),
                name
        );

        /*Table data = tableEnv.from(table);
        data.execute().print();*/


        String selectSql = "select name,expensive from "+table;
        System.out.println(selectSql);
        // insert some example data into the table
        final TableResult result =
                tableEnv.executeSql(selectSql);

        // since all cluster operations of the Table API are executed asynchronously,
        // we need to wait until the insertion has been completed,
        // an exception is thrown in case of an error
        result.await();
        result.print();
    }

    private void unstructuredToStructureBridge(String table, Map<String, Object> row) throws Exception{
        String name  = "myhive";
        final StreamTableEnvironment tableEnv = this.dataLakeSessionManager.newDataLakeCatalogSession(
                this.pipelineService.getEnv(),
                name
        );

        Table data = tableEnv.from(table);
        ResolvedSchema resolvedSchema = data.getResolvedSchema();


        //should contain all payloadColums via Alter table earlier
        List<String> currentColumns = resolvedSchema.getColumnNames();

        for(String currentColum: currentColumns){
            if(!row.containsKey(currentColum)){
                //missing data
                row.put(currentColum, "empty_string");
            }
        }

        List<Map<String, Object>> insert = new ArrayList<>();
        insert.add(row);
        this.addData(table, insert);
    }
}
