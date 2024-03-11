package com.appgallabs.dataplatform.ingestion.pipeline;


import io.quarkus.test.junit.QuarkusTest;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.Test;
import test.components.BaseTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@QuarkusTest
public class DataLifeCycleTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(DataLifeCycleTests.class);

    @Inject
    private DataLakeTableGenerator dataLakeTableGenerator;

    @Inject
    private DataLakeSessionManager dataLakeSessionManager;

    @Inject
    private DataLakeSqlGenerator dataLakeSqlGenerator;

    private StreamExecutionEnvironment env;

    @Test
    public void updateTableSchema() throws Exception{

    }
}
