package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.datalake.DataLakeDriver;
import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.apache.commons.io.IOUtils;

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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class PipelineService {
    private static Logger logger = LoggerFactory.getLogger(PipelineService.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private Instance<DataLakeDriver> dataLakeDriverInstance;

    private String dataLakeDriverName;
    private DataLakeDriver dataLakeDriver;

    private MetaDataDecoratorMapFunction metaDataDecoratorMapFunction;

    private DataLakeSinkFunction dataLakeSinkFunction;

    @PostConstruct
    public void start(){
        Config config = ConfigProvider.getConfig();
        this.dataLakeDriverName = config.getValue("datalake_driver_name", String.class);
        this.dataLakeDriver = dataLakeDriverInstance.select(NamedLiteral.of(dataLakeDriverName)).get();

        this.metaDataDecoratorMapFunction = new MetaDataDecoratorMapFunction(this.securityTokenContainer.getSecurityToken());
        this.dataLakeSinkFunction = new DataLakeSinkFunction(this.securityTokenContainer.getSecurityToken(),
                this.dataLakeDriver);
    }

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
                    this.metaDataDecoratorMapFunction
            ).addSink(this.dataLakeSinkFunction);

            env.execute();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
