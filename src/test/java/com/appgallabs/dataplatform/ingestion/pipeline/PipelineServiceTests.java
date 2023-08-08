package com.appgallabs.dataplatform.ingestion.pipeline;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prototype.infrastructure.DataEvent;
import prototype.infrastructure.Phase1MapFunction;
import prototype.infrastructure.Phase2MapFunction;
import prototype.infrastructure.Phase3SinkFunction;
import prototype.infrastructure.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

//TODO
//@QuarkusTest
public class PipelineServiceTests {
    private static Logger logger = LoggerFactory.getLogger(PipelineServiceTests.class);

    @Inject
    private PipelineService pipelineService = new PipelineService();

    @Test
    public void ingest() {
        this.pipelineService.ingest();
    }
}
