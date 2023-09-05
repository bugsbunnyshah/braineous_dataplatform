package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.configuration.FrameworkServices;
import com.appgallabs.dataplatform.datalake.DataLakeDriver;
import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.inject.Inject;
import java.util.*;

@ApplicationScoped
public class PipelineService {
    private static Logger logger = LoggerFactory.getLogger(PipelineService.class);

    @Inject
    private FrameworkServices frameworkServices;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private Instance<DataLakeDriver> dataLakeDriverInstance;

    private String dataLakeDriverName;
    private DataLakeDriver dataLakeDriver;

    private MetaDataDecoratorMapFunction metaDataDecoratorMapFunction;

    private DataLakeSinkFunction dataLakeSinkFunction;

    private SchemalessMapper mapper;

    @PostConstruct
    public void start(){
        Config config = ConfigProvider.getConfig();

        this.mapper = new SchemalessMapper();

        this.dataLakeDriverName = config.getValue("datalake_driver_name", String.class);
        this.dataLakeDriver = dataLakeDriverInstance.select(NamedLiteral.of(dataLakeDriverName)).get();

        this.metaDataDecoratorMapFunction = new MetaDataDecoratorMapFunction(this.securityTokenContainer.getSecurityToken());
        this.dataLakeSinkFunction = new DataLakeSinkFunction(this.securityTokenContainer.getSecurityToken(),
                this.dataLakeDriver);
    }

    public void ingest(SecurityToken securityToken, String entity, String jsonString){
        try {
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            List<DataEvent> inputEvents = new ArrayList<>();

            JsonElement jsonElement = JsonParser.parseString(jsonString);
            JsonUtil.printStdOut(jsonElement);

            if(jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();

                for (int i = 0; i < jsonArray.size(); i++) {
                    //decompose the object into its fields
                    String json = jsonArray.get(i).getAsJsonObject().toString();

                    List<DataEvent> objectEvents = this.flattenObject(entity, json);
                    inputEvents.addAll(objectEvents);
                }
            }else if(jsonElement.isJsonObject()){
                String json = jsonElement.toString();
                List<DataEvent> objectEvents = this.flattenObject(entity, json);
                inputEvents.addAll(objectEvents);
            }

            DataLakeSinkFunction sinkFunction = new DataLakeSinkFunction(securityToken,
                    this.dataLakeDriver);

            DataStream<DataEvent> dataEvents = env.fromCollection(inputEvents);
            dataEvents.addSink(sinkFunction);

            env.execute();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public void ingest(String entity,String jsonString){
        try {
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            List<DataEvent> inputEvents = new ArrayList<>();

            JsonElement jsonElement = JsonParser.parseString(jsonString);
            JsonUtil.printStdOut(jsonElement);

            if(jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();

                for (int i = 0; i < jsonArray.size(); i++) {
                    //decompose the object into its fields
                    String json = jsonArray.get(i).getAsJsonObject().toString();

                    List<DataEvent> objectEvents = this.flattenObject(entity, json);
                    inputEvents.addAll(objectEvents);
                }
            }else if(jsonElement.isJsonObject()){
                String json = jsonElement.toString();
                List<DataEvent> objectEvents = this.flattenObject(entity, json);
                inputEvents.addAll(objectEvents);
            }

            DataStream<DataEvent> dataEvents = env.fromCollection(inputEvents);
            dataEvents.addSink(this.dataLakeSinkFunction);

            env.execute();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public void ingest(String entity,String jsonString, List<String> jsonPathExpressions){
        try {
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            List<DataEvent> inputEvents = new ArrayList<>();

            JsonElement jsonElement = JsonParser.parseString(jsonString);
            if(jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();

                for (int i = 0; i < jsonArray.size(); i++) {
                    //decompose the object into its fields
                    String json = jsonArray.get(i).getAsJsonObject().toString();

                    List<DataEvent> objectEvents = this.flattenObjectSubset(entity, json, jsonPathExpressions);
                    inputEvents.addAll(objectEvents);
                }
            }else if(jsonElement.isJsonObject()){
                String json = jsonElement.toString();
                List<DataEvent> objectEvents = this.flattenObjectSubset(entity, json, jsonPathExpressions);
                inputEvents.addAll(objectEvents);
            }

            DataStream<DataEvent> dataEvents = env.fromCollection(inputEvents);

            dataEvents.addSink(this.dataLakeSinkFunction);

            env.execute();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private List<DataEvent> flattenObject(String entity,String json){
        List<DataEvent> inputEvents = new ArrayList<>();

        //decompose the object into its fields
        Map<String,Object> flatObject = mapper.mapAll(json);

        Set<Map.Entry<String, Object>> entries = flatObject.entrySet();
        for(Map.Entry<String, Object> entry:entries){
            String key = entry.getKey();
            Object value = entry.getValue();
            inputEvents.add(new DataEvent(entity,json,key,value));
        }

        return inputEvents;
    }

    private List<DataEvent> flattenObjectSubset(String entity,String json, List<String> jsonPathExpressions){
        List<DataEvent> inputEvents = new ArrayList<>();

        //decompose the object into its fields
        Map<String,Object> flatObject = mapper.mapSubset(json,jsonPathExpressions);
        Gson gson = this.frameworkServices.getGson();
        String flattenedJsonString = gson.toJson(flatObject, LinkedHashMap.class);
        String jsonSubset = JsonUnflattener.unflatten(flattenedJsonString);


        Set<Map.Entry<String, Object>> entries = flatObject.entrySet();
        for(Map.Entry<String, Object> entry:entries){
            String key = entry.getKey();
            Object value = entry.getValue();
            inputEvents.add(new DataEvent(entity,jsonSubset,key,value));
        }

        return inputEvents;
    }
}
