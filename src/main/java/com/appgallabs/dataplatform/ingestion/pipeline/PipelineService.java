package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.apache.commons.io.IOUtils;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class PipelineService {
    private static Logger logger = LoggerFactory.getLogger(PipelineService.class);

    public void ingest(){
        try {
            String jsonString = IOUtils.toString(Thread.currentThread().
                            getContextClassLoader().getResourceAsStream("ingestion/algorithm/input_array.json"),
                    StandardCharsets.UTF_8
            );
            JsonArray jsonArray = JsonParser.parseString(jsonString).getAsJsonArray();

            //TODO: Quarkus
            SchemalessMapper mapper = new SchemalessMapper();
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

            List<DataEvent> inputEvents = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                //decompose the object into its fields
                String json = jsonArray.get(i).getAsJsonObject().toString();
                Map<String,Object> flatObject = mapper.mapAll(json);

                Set<Map.Entry<String, Object>> entries = flatObject.entrySet();
                for(Map.Entry<String, Object> entry:entries){
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    inputEvents.add(new DataEvent(json,key,value));
                }
            }

            DataStream<DataEvent> dataEvents = env.fromCollection(inputEvents);

            dataEvents.map(
                    new MetaDataDecoratorMapFunction()
            ).addSink(new DataLakeSinkFunction());

            env.execute();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
