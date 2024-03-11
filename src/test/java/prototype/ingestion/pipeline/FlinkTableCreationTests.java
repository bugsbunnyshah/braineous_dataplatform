package prototype.ingestion.pipeline;

import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.ingestion.pipeline.DataLakeSessionManager;
import com.appgallabs.dataplatform.ingestion.pipeline.DataLakeSqlGenerator;
import com.appgallabs.dataplatform.ingestion.pipeline.DataLakeTableGenerator;
import com.appgallabs.dataplatform.ingestion.pipeline.PipelineService;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;

import org.apache.flink.table.api.*;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import io.quarkus.test.junit.QuarkusTest;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.catalog.ObjectPath;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.util.*;


@QuarkusTest
public class FlinkTableCreationTests {
    private static Logger logger = LoggerFactory.getLogger(FlinkTableCreationTests.class);

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

    private String tableName = "updt_l";

    @Test
    public void testCreateTable() throws Exception{
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
            break;
        }

        Map<String, Object> row = flatArray.get(0);

        this.createTable(row);
    }

    @Test
    public void testUpdateTable() throws Exception{
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
            break;
        }

        Map<String, Object> row = flatArray.get(0);

        this.createTable(row);

        for(int i=0; i<1; i++) {
            this.updateTable(row);
        }

        String table = this.getTable();
        for(int i=0; i<10; i++){
            this.addData(table, flatArray);
            this.printData(table);
        }
    }

    @Test
    public void testSelectTable() throws Exception{
        String table = this.getTable();
        this.printData(table);
    }

    private String getTable(){
        String name  = "myhive";
        String database = "mydatabase";
        String table = name + "." + database + "." + this.tableName;
        return table;
    }

    private String createTable(Map<String,Object> row) throws Exception{
        String name  = "myhive";
        String database = "mydatabase";
        final StreamTableEnvironment tableEnv = this.dataLakeSessionManager.newDataLakeSessionWithNewDatabase(
                this.pipelineService.getEnv(),
                name,
                database
        );

        //String tableName = RandomStringUtils.randomAlphabetic(5).toLowerCase();
        String table = name + "." + database + "." + this.tableName;
        String objectPath = database + "." + this.tableName;
        String filePath = "file:///Users/babyboy/datalake/"+ this.tableName;
        String format = "csv";

        String currentCatalog = tableEnv.getCurrentCatalog();
        Optional<Catalog> catalog = tableEnv.getCatalog(currentCatalog);
        boolean tableExists = catalog.get().tableExists(ObjectPath.fromString(objectPath));

        System.out.println("TABLE: "+table);
        if(!tableExists) {
            TableDescriptor tableDescriptor = this.dataLakeTableGenerator.createFileSystemTable(row,
                    filePath,
                    format);

            tableEnv.createTable(table, tableDescriptor);
        }

        return table;
    }

    private String updateTable(Map<String,Object> row) throws Exception{
        String newColumn = RandomStringUtils.randomAlphabetic(5).toLowerCase();

        String name  = "myhive";
        String database = "mydatabase";
        final StreamTableEnvironment tableEnv = this.dataLakeSessionManager.newDataLakeCatalogSession(
                this.pipelineService.getEnv(),
                name
        );

        String table = name + "." + database + "." + this.tableName;
        String objectPath = database + "." + this.tableName;

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


        row.put(newColumn, "update_success");

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

        Table data = tableEnv.from(table);
        data.execute().print();


        /*String selectSql = "select * from "+table;
        System.out.println(selectSql);
        // insert some example data into the table
        final TableResult result =
                tableEnv.executeSql(selectSql);

        // since all cluster operations of the Table API are executed asynchronously,
        // we need to wait until the insertion has been completed,
        // an exception is thrown in case of an error
        result.await();
        result.print();*/
    }
}
