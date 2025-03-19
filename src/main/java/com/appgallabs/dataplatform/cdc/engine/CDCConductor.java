package com.appgallabs.dataplatform.cdc.engine;

import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

public class CDCConductor {
    private static Logger logger = LoggerFactory.getLogger(CDCConductor.class);

    private static CDCConductor singleton = new CDCConductor();

    private CDCConductor(){

    }

    public static CDCConductor getInstance(){
        if(CDCConductor.singleton == null){
            CDCConductor.singleton = new CDCConductor();
        }
        return CDCConductor.singleton;
    }

    public void orchestrate(JsonObject cdcConfig, JsonArray sourceData){
        try {
            logger.info("*************************************");
            logger.info("CONDUCTOR_ORCHESTRATION_START_SUCCESS");
            logger.info("*************************************");

            final String cdcConfigStr = cdcConfig.toString();

            //1 - process the dataset
            Collection<String> dataSet = new ArrayList<>();
            for (int i = 0; i < sourceData.size(); i++) {
                JsonObject objJson = sourceData.get(i).getAsJsonObject();
                String objJsonStr = objJson.toString();
                dataSet.add(objJsonStr);
            }

            //2 - //process each record in parallel to achieve O(1) time complexity using Flink
            //TODo: convert to the actual remote flink environment (integration_phase)
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

            DataStream<String> sourceDataStream = env.fromCollection(
                    dataSet
            );

            DataStream<String> parallel = sourceDataStream.map(new MapFunction<String,String>() {
                @Override
                public String map(String s) throws Exception {
                    CDCProcess cdcProcess = CDCProcess.getInstance();

                    //generate a unified input of configuration + record
                    JsonObject input = new JsonObject();
                    input.add("cdcConfig", JsonUtil.validateJson(cdcConfigStr).getAsJsonObject());
                    input.add("record", JsonUtil.validateJson(s).getAsJsonObject());

                    //process
                    cdcProcess.process(input.toString());

                    //provide response
                    JsonObject response = new JsonObject();
                    response.addProperty("status_code", 200);
                    return response.toString();
                }
            });
            //parallel.print();

            //3- execute CDC process
            env.execute();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
