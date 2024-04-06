package prototype.infrastructure.datalake;

import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.*;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.catalog.CatalogDatabaseImpl;
import org.apache.flink.table.catalog.ResolvedSchema;
import org.apache.flink.table.catalog.hive.HiveCatalog;
import org.apache.flink.types.Row;
import org.apache.flink.table.api.Schema.Builder;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class DataLakeServiceTests {
    private static Logger logger = LoggerFactory.getLogger(DataLakeServiceTests.class);

    @Inject
    private SchemalessMapper schemalessMapper;

    private ExecutorService executorService = Executors.newFixedThreadPool(25);
    private ExecutorService retryService = Executors.newFixedThreadPool(25);

    @Test
    public void testDataLifeCycle() throws Exception{
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createRemoteEnvironment(
                "localhost",
                Integer.parseInt("8081"),
                "dataplatform-1.0.0-cr2-runner.jar"
        );
        /*env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                3, // number of restart attempts
                Time.of(10, TimeUnit.SECONDS) // delay
        ));*/
        logger.info("**************");

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

        String table = this.createTable(env);
        this.addData(env, table, flatArray);
        this.query(env, table);
    }

    @Test
    public void testPerformance() throws Exception{
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createRemoteEnvironment(
                "localhost",
                Integer.parseInt("8081"),
                "dataplatform-1.0.0-cr2-runner.jar"
        );
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                3, // number of restart attempts
                Time.of(10, TimeUnit.SECONDS) // delay
        ));

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

        String table = this.createTable(env);

        logger.info("*****STARTING_PERFORMANCE_TEST********");

        int number_of_jobs = 1000; //localhost / baremetal
        for(int i=0; i<number_of_jobs; i++) {
            this.submitJob(env, table, flatArray);
        }

        while(true){
            Thread.sleep(10000);
        }
    }

    private void submitJob(StreamExecutionEnvironment env, String table, List<Map<String, Object>> flatArray){
        this.executorService.execute(() -> {
            try {
                this.addData(env, table, flatArray);
                logger.info("****JOB_SUCCESS*****");
            }catch(Exception e){
                this.retryJob(env, table, flatArray);
            }
        });
    }

    private void retryJob(StreamExecutionEnvironment env,String table, List<Map<String, Object>> flatArray){
        this.retryService.execute(() -> {
            while(true){
                try {
                    this.addData(env, table, flatArray);
                    logger.info("****JOB_RETRY_SUCCESS*****");
                    break;
                }catch(Exception e){

                }
            }
        });
    }

    private String createTable(StreamExecutionEnvironment env) throws Exception{
        // set up the Java Table API
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

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

        // Create a catalog table
        final Schema schema = Schema.newBuilder()
                .column("name", DataTypes.STRING())
                .column("age", DataTypes.INT())
                .build();

        TableDescriptor tableDescriptor = TableDescriptor.forConnector("filesystem")
                .option("path", "file:///Users/babyboy/datalake/"+tableName)
                .option("format", "csv")
                .schema(schema)
                .build();

        tableEnv.createTable(table, tableDescriptor);

        return table;
    }

    private void addData(StreamExecutionEnvironment env, String table, List<Map<String, Object>> flatArray) throws Exception
    {
        // set up the Java Table API
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // Create a HiveCatalog
        String name            = "myhive";
        String hiveConfDir     = "/Users/babyboy/mumma/braineous/infrastructure/apache-hive-3.1.3-bin/conf";

        HiveCatalog hive = new HiveCatalog(name, null, hiveConfDir);
        tableEnv.registerCatalog(name, hive);

        // set the HiveCatalog as the current catalog of the session
        tableEnv.useCatalog(name);


        String sql = "INSERT INTO " + table + " VALUES"
                + "  ('shah', 46), "
                + "  ('blah', 55)";

        final TableResult insertionResult =
                tableEnv.executeSql(sql);

        // since all cluster operations of the Table API are executed asynchronously,
        // we need to wait until the insertion has been completed,
        // an exception is thrown in case of an error
        insertionResult.await();
    }

    private void query(StreamExecutionEnvironment env, String table){
        // set up the Java Table API
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // Create a HiveCatalog
        String name            = "myhive";
        String hiveConfDir     = "/Users/babyboy/mumma/braineous/infrastructure/apache-hive-3.1.3-bin/conf";

        HiveCatalog hive = new HiveCatalog(name, null, hiveConfDir);
        tableEnv.registerCatalog(name, hive);

        // set the HiveCatalog as the current catalog of the session
        tableEnv.useCatalog(name);


        String selectSql = "SELECT * FROM "
                + table;

        final Table result =
                tableEnv.sqlQuery(selectSql);

        result.execute().print();
    }

    private List<Row> rows(List<Map<String, Object>> flatArray){
        List<Row> rows = new ArrayList<>();

        for(Map<String,Object> flatJson:flatArray) {
            Row row = this.row(flatJson);
            rows.add(row);
        }

        return rows;
    }

    private Row row(Map<String,Object> flatJson){
        Collection<Object> values = flatJson.values();

        Row row = new Row(values.size());

        int i =0;
        Set<Map.Entry<String,Object>> entrySet = flatJson.entrySet();
        for(Map.Entry<String,Object> entry: entrySet){
            String name = entry.getKey();
            Object value = entry.getValue();
            row.setField(i, value);
            i++;
        }

        return row;
    }
}
