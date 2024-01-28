package prototype.infrastructure.datalake;

import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class DataLakeServiceTests {
    private static Logger logger = LoggerFactory.getLogger(DataLakeServiceTests.class);

    @Inject
    private SchemalessMapper schemalessMapper;

    @Test
    public void testJsonTableCreation() throws Exception{
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createRemoteEnvironment(
                "localhost",
                Integer.parseInt("8081"),
                "dataplatform-1.0.0-cr2-runner.jar"
        );
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                3, // number of restart attempts
                Time.of(10, TimeUnit.SECONDS) // delay
        ));

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

        boolean success = this.submitJob(env, flatArray);

        logger.info("******JOB_STATUS****");
        logger.info("SUCCESS: "+success);
        assertTrue(success);
    }

    private boolean submitJob(StreamExecutionEnvironment env, List<Map<String, Object>> flatArray) throws Exception
    {
        // set up the Java Table API
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // create a table with example data without a connector required
        List<Row> rows = this.rows(flatArray);
        final Table ingestionTable = tableEnv.fromValues(rows);

        String sql = "SELECT * FROM "
                + ingestionTable;

        final Table result =
                tableEnv.sqlQuery(sql);

        tableEnv.toDataStream(result, Row.class).print();


        // after the table program is converted to a DataStream program,
        // we must use `env.execute()` to submit the job
        boolean success = true;
        try {
            env.execute();
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            success = false;
        }
        return success;
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
