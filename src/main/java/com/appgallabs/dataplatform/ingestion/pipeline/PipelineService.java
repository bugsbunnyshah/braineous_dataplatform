package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.configuration.FrameworkServices;
import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.pipeline.Registry;

import com.appgallabs.dataplatform.util.Debug;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

@ApplicationScoped
public class PipelineService {
    private static Logger logger = LoggerFactory.getLogger(PipelineService.class);
    private SchemalessMapper mapper;
    @Inject
    private FrameworkServices frameworkServices;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @ConfigProperty(name = "flinkHost")
    private String flinkHost;

    @ConfigProperty(name = "flinkPort")
    private String flinkPort;

    @PostConstruct
    public void start(){
        this.mapper = new SchemalessMapper();
    }

    public void ingest(SecurityToken securityToken, String driverConfiguration,
                       String pipeId, String entity, String jsonString){
        try {
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.createRemoteEnvironment(
                    this.flinkHost,
                    Integer.parseInt(this.flinkPort),
                    "dataplatform-1.0.0-runner.jar"
            );

            JsonElement jsonElement = JsonParser.parseString(jsonString);

            List<String> input = new ArrayList<>();
            if(jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject inputJson = jsonArray.get(i).getAsJsonObject();
                    input.add(inputJson.toString());
                }
            }else if(jsonElement.isJsonObject()){
                input.add(jsonElement.toString());
            }

            Debug.out("*********FLINK_INPUT***************");
            JsonUtil.printStdOut(JsonUtil.validateJson(input.toString()));
            Debug.out("************************");


            DataStream<String> dataEvents = env.fromCollection(input);
            DataLakeSinkFunction sinkFunction = new DataLakeSinkFunction(securityToken,
                    driverConfiguration,pipeId);
            dataEvents.addSink(sinkFunction);
            env.execute();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
