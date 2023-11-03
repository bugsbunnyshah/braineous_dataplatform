package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.configuration.FrameworkServices;
import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.pipeline.Registry;

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

    private Registry registry;

    @ConfigProperty(name = "flinkHost")
    private String flinkHost;

    @ConfigProperty(name = "flinkPort")
    private String flinkPort;

    @PostConstruct
    public void start(){
        this.mapper = new SchemalessMapper();
        this.registry = Registry.getInstance();
    }

    public void ingest(SecurityToken securityToken, String entity, String jsonString){
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
                    //decompose the object into its fields
                    String json = jsonArray.get(i).getAsJsonObject().toString();

                    Map<String,Object> flatObject = this.mapper.mapAll(json);

                    JsonObject inputJson = new JsonObject();
                    Set<Map.Entry<String, Object>> entries = flatObject.entrySet();
                    for(Map.Entry<String, Object> entry: entries){
                        String name = entry.getKey();
                        Object value = entry.getValue();
                        inputJson.addProperty(name, value.toString());
                    }
                    input.add(inputJson.toString());
                }
            }else if(jsonElement.isJsonObject()){
                String json = jsonElement.toString();

                Map<String,Object> flatObject = this.mapper.mapAll(json);

                JsonObject inputJson = new JsonObject();
                Set<Map.Entry<String, Object>> entries = flatObject.entrySet();
                for(Map.Entry<String, Object> entry: entries){
                    String name = entry.getKey();
                    Object value = entry.getValue();
                    inputJson.addProperty(name, value.toString());
                }
                input.add(inputJson.toString());
            }


            DataStream<String> dataEvents = env.fromCollection(input);
            DataLakeSinkFunction sinkFunction = new DataLakeSinkFunction(securityToken);
            dataEvents.addSink(sinkFunction);
            env.execute();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
