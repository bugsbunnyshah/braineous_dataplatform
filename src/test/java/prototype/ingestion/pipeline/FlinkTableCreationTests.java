package prototype.ingestion.pipeline;

import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.ingestion.pipeline.DataLakeSessionManager;
import com.appgallabs.dataplatform.ingestion.pipeline.DataLakeSqlGenerator;
import com.appgallabs.dataplatform.ingestion.pipeline.DataLakeTableGenerator;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
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

    private StreamExecutionEnvironment env;

    private String tableName = "unit_test";

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

        String table = this.createTable(this.tableName,row);
        this.addData(table, flatArray);
    }

    private String createTable(String tableName, Map<String,Object> row) throws Exception{
        String name  = "myhive";
        String database = "mydatabase";
        final StreamTableEnvironment tableEnv = this.dataLakeSessionManager.newDataLakeSessionWithNewDatabase(
                this.env,
                name,
                database
        );


        tableName = RandomStringUtils.randomAlphabetic(5).toLowerCase();
        String table = name + "." + database + "." + tableName;
        String objectPath = database + "." + tableName;
        String filePath = "file:///Users/babyboy/datalake/"+tableName;
        String format = "csv";

        String currentCatalog = tableEnv.getCurrentCatalog();
        Optional<Catalog> catalog = tableEnv.getCatalog(currentCatalog);
        boolean tableExists = catalog.get().tableExists(ObjectPath.fromString(objectPath));

        if(!tableExists) {
            System.out.println("TABLE: "+table);
            TableDescriptor tableDescriptor = this.dataLakeTableGenerator.createFileSystemTable(row,
                    filePath,
                    format);

            tableEnv.createTable(table, tableDescriptor);
        }

        return table;
    }

    private void addData(String table, List<Map<String,Object>> rows) throws Exception{
        String name  = "myhive";
        final StreamTableEnvironment tableEnv = this.dataLakeSessionManager.newDataLakeCatalogSession(
                this.env,
                name
        );

        String insertSql = this.dataLakeSqlGenerator.generateInsertSql(table, rows);
        // insert some example data into the table
        final TableResult insertionResult =
                tableEnv.executeSql(insertSql);

        // since all cluster operations of the Table API are executed asynchronously,
        // we need to wait until the insertion has been completed,
        // an exception is thrown in case of an error
        insertionResult.await();

        String queryTable = "scenariowitharray" + "." + "scenariowitharray";
        String sql = "select * from "+queryTable;
        Table result = tableEnv.sqlQuery(sql);
        result.execute().print();
    }
}
