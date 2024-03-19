package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.configuration.ConfigurationService;
import com.appgallabs.dataplatform.configuration.FrameworkServices;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private ConfigurationService configurationService;

    private StreamExecutionEnvironment env;

    private ExecutorService threadpool = Executors.newCachedThreadPool();

    public String getFlinkHost() {
        return this.configurationService.getProperty("flink_host");
    }

    public String getFlinkPort() {
        return this.configurationService.getProperty("flink_port");
    }

    public String getStreamComponents(){
        return this.configurationService.getProperty("braineous_stream_components");
    }

    @PostConstruct
    public void start(){
        this.mapper = new SchemalessMapper();

        this.env = StreamExecutionEnvironment.createRemoteEnvironment(
                this.getFlinkHost(),
                Integer.parseInt(this.getFlinkPort()),
                this.getStreamComponents()
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

    public StreamExecutionEnvironment getEnv() {
        return env;
    }
}
