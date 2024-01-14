package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.configuration.FrameworkServices;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
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

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    public String getFlinkHost() {
        return flinkHost;
    }

    public void setFlinkHost(String flinkHost) {
        this.flinkHost = flinkHost;
    }

    public String getFlinkPort() {
        return flinkPort;
    }

    public void setFlinkPort(String flinkPort) {
        this.flinkPort = flinkPort;
    }

    private StreamExecutionEnvironment env;

    private ExecutorService threadpool = Executors.newCachedThreadPool();

    //TODO: Make this a Offset based implementation (CR2)
    private Map<String, List<String>> readyBuffer = new HashMap<>();

    @PostConstruct
    public void start(){
        this.mapper = new SchemalessMapper();
        this.env = StreamExecutionEnvironment.createRemoteEnvironment(
                this.flinkHost,
                Integer.parseInt(this.flinkPort),
                "dataplatform-1.0.0-cr2-runner.jar"
        );
        this.env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                3, // number of restart attempts
                Time.of(10, TimeUnit.SECONDS) // delay
        ));
    }

    public void ingest(SecurityToken securityToken, String driverConfiguration,
                       String pipeId, String entity, String jsonString){
        try {
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

            /*Debug.out("*********FLINK_INPUT***************");
            JsonUtil.printStdOut(JsonUtil.validateJson(input.toString()));
            Debug.out("************************");*/

            String tenant = securityToken.getPrincipal();
            List<String> stream = this.readyBuffer.get(tenant);
            if(stream == null){
                stream = new ArrayList<>();
                this.readyBuffer.put(tenant,stream);
            }

            stream = this.readyBuffer.get(tenant);
            stream.addAll(input);

            if(stream.size() < 1000){
                System.out.println("**STREAM_SIZE***");
                System.out.println(stream.size());
                System.out.println("************");
                return;
            }

            synchronized (stream) {
                List<String> copy = new ArrayList<>(stream);
                if(copy.isEmpty()){
                    return;
                }

                int batchSize = 500;
                int batchIndex = 1;
                List<String> batch = new ArrayList<>();
                for(String entry:copy){
                    batch.add(entry);
                    if(batch.size() < batchSize){
                        continue;
                    }

                    List<String> batchCopy = new ArrayList<>(batch);
                    submitBatch(batchIndex,batchCopy,securityToken,driverConfiguration,pipeId,entity);

                    batch.clear();
                    batchIndex++;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            //throw new RuntimeException(e);
        }
    }

    private void submitBatch(int batchIndex,List<String> batch,
                             SecurityToken securityToken, String driverConfiguration,
                             String pipeId, String entity){
        /*this.threadpool.execute(() -> {
            try {
                SystemStore systemStore = this.mongoDBJsonStore.getSystemStore();
                DataLakeSinkFunction sinkFunction = new DataLakeSinkFunction(securityToken,
                        systemStore,
                        driverConfiguration,
                        pipeId,
                        entity);

                DataStream<String> dataEvents = this.env.fromCollection(batch);
                System.out.println("**BATCH_SIZE***");
                System.out.println(batch.size());
                System.out.println("************");

                dataEvents.addSink(sinkFunction);
                this.env.execute();

                System.out.println("****JOB_SUCCESS****");
                System.out.println("BATCH_INDEX: "+batchIndex);
                System.out.println("*******************");
            }catch(Exception e){
                e.printStackTrace();
            }
        });*/

        try {
            SystemStore systemStore = this.mongoDBJsonStore.getSystemStore();
            DataLakeSinkFunction sinkFunction = new DataLakeSinkFunction(securityToken,
                    systemStore,
                    driverConfiguration,
                    pipeId,
                    entity);

            DataStream<String> dataEvents = this.env.fromCollection(batch);
            System.out.println("**BATCH_SIZE***");
            System.out.println(batch.size());
            System.out.println("************");

            dataEvents.addSink(sinkFunction);
            this.env.execute();

            System.out.println("****JOB_SUCCESS****");
            System.out.println("BATCH_INDEX: "+batchIndex);
            System.out.println("*******************");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
