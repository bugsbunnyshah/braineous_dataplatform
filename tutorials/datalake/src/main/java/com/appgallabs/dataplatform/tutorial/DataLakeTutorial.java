package com.appgallabs.dataplatform.tutorial;

import com.appgallabs.dataplatform.client.sdk.api.Configuration;
import com.appgallabs.dataplatform.client.sdk.api.DataPlatformService;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
    }
    //----------------------------------------------------------------------------------------------------------
    /*private StreamTableEnvironment getTableEnvironment(String catalogName){
        StreamExecutionEnvironment env = this.pipelineService.getEnv();
        final StreamTableEnvironment tableEnv = this.dataLakeSessionManager.newDataLakeCatalogSession(
                env,
                catalogName
        );

        return tableEnv;
    }

    private void printData(StreamTableEnvironment tableEnv, String table, String selectSql) throws Exception{
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
    }*/
}
