package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.configuration.FrameworkServices;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
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

    @Inject
    private JobManager jobManager;

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

    //TODO: Make this a Offset based implementation (NOW)
    private Map<String, List<String>> readyBuffer = new HashMap<>();

    @PostConstruct
    public void start(){
        this.mapper = new SchemalessMapper();

        //TODO: pull implementation from configuration (NOW)
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
                       String pipeId, long offset, String entity, String jsonString){
        this.jobManager.submit(this.env, securityToken, driverConfiguration, entity, pipeId, offset, jsonString);
    }
}
