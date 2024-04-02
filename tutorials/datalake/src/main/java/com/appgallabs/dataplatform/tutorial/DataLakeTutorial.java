package com.appgallabs.dataplatform.tutorial;

import com.appgallabs.dataplatform.client.sdk.api.Configuration;
import com.appgallabs.dataplatform.client.sdk.api.DataPlatformService;

import com.appgallabs.dataplatform.ingestion.util.JobManagerUtil;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.flink.table.catalog.hive.HiveCatalog;

import java.util.concurrent.TimeUnit;

public class DataLakeTutorial {

    public static void main(String[] args) throws Exception{
        DataPlatformService dataPlatformService = DataPlatformService.getInstance();

        String apiKey = "ffb2969c-5182-454f-9a0b-f3f2fb0ebf75";
        String apiSecret = "5960253b-6645-41bf-b520-eede5754196e";

        String datasetLocation = "dataset/data.json";
        String json = Util.loadResource(datasetLocation);
        JsonElement datasetElement = JsonUtil.validateJson(json);
        System.out.println("*****DATA_SET******");
        JsonUtil.printStdOut(datasetElement);

        String configLocation = "pipe_config/pipe_config.json";
        String pipeConfigJson = Util.loadResource(configLocation);
        JsonObject configJson = JsonUtil.validateJson(pipeConfigJson).getAsJsonObject();
        String pipeId = configJson.get("pipeId").getAsString();
        String entity = configJson.get("entity").getAsString();
        System.out.println("*****PIPE_CONFIGURATION******");
        JsonUtil.printStdOut(configJson);

        //configure the DataPipeline Client
        Configuration configuration = new Configuration().
                ingestionHostUrl("http://localhost:8080/").
                apiKey(apiKey).
                apiSecret(apiSecret).
                streamSizeInObjects(0);
        dataPlatformService.configure(configuration);

        //register pipe
        dataPlatformService.registerPipe(configJson);
        System.out.println("*****PIPE_REGISTRATION_SUCCESS******");

        //send source data through the pipeline
        dataPlatformService.sendData(pipeId, entity,datasetElement.toString());
        System.out.println("*****DATA_INGESTION_SUCCESS******");

        //check for datalake record
        Thread.sleep(15000);

        System.out.println("*****CHECK_DATALAKE*******");
        String catalog = JobManagerUtil.getCatalog(apiKey, pipeId);
        String table = JobManagerUtil.getTable(apiKey, pipeId, entity);
        StreamTableEnvironment tableEnv = getTableEnvironment(catalog);
        String selectSql = "select * from "+table;
        printData(tableEnv, table, selectSql);
    }
    //----------------------------------------------------------------------------------------------------------
    private static StreamExecutionEnvironment getStreamExecutionEnvironment(){
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createRemoteEnvironment(
                "127.0.0.1",
                Integer.parseInt("8081"),
                "dataplatform-1.0.0-cr2-runner.jar"
        );
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                3, // number of restart attempts
                Time.of(10, TimeUnit.SECONDS) // delay
        ));
        return env;
    }

    private static StreamTableEnvironment newDataLakeCatalogSession(StreamExecutionEnvironment env,
                                                            String catalog){
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        HiveCatalog hive = new HiveCatalog(catalog, null, "./services/hive_3.1.3/conf");
        tableEnv.registerCatalog(catalog, hive);

        // set the HiveCatalog as the current catalog of the session
        tableEnv.useCatalog(catalog);

        return tableEnv;
    }

    private static StreamTableEnvironment getTableEnvironment(String catalogName){
        StreamExecutionEnvironment env = getStreamExecutionEnvironment();
        final StreamTableEnvironment tableEnv = newDataLakeCatalogSession(
                env,
                catalogName
        );

        return tableEnv;
    }

    private static void printData(StreamTableEnvironment tableEnv, String table, String selectSql) throws Exception{
        // insert some example data into the table
        final TableResult result =
                tableEnv.executeSql(selectSql);

        // since all cluster operations of the Table API are executed asynchronously,
        // we need to wait until the insertion has been completed,
        // an exception is thrown in case of an error
        result.await();

        System.out.println("********DATA**********");
        System.out.println(selectSql);
        result.print();
        System.out.println("**********************");
    }
}
