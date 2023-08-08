package com.appgallabs.dataplatform.ingestion.pipeline;

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

            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

            List<DataEvent> inputEvents = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                inputEvents.add(new DataEvent(jsonArray.get(i).getAsJsonObject().toString()));
            }

            DataStream<DataEvent> dataEvents = env.fromCollection(inputEvents);

            DataStream<DataEvent> mappedStream = dataEvents.map(
                    new DecomposeObjectMapFunction()
            );

            mappedStream.map(new MetaDataDecoratorMapFunction())
                    .addSink(new DataLakeSinkFunction());

            env.execute();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
