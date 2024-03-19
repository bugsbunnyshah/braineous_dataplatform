package com.appgallabs.dataplatform.configuration;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.BaseTest;

import javax.inject.Inject;

@QuarkusTest
public class ConfigurationServiceTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(ConfigurationServiceTests.class);

    @Inject
    private ConfigurationService configurationService;

    @Test
    public void testConfigurationService() throws Exception{
        String property = "mongodb_host";
        String value = this.configurationService.getProperty(property);
        logger.info(value);
        assertEquals("localhost", value);

        property = "poll_beat";
        value = this.configurationService.getProperty(property);
        logger.info(value);
        assertEquals("30000", value);

        property = "hive_conf_directory";
        value = this.configurationService.getProperty(property);
        logger.info(value);
        assertEquals("/Users/babyboy/mumma/braineous/infrastructure/apache-hive-3.1.3-bin/conf", value);

        property = "table_directory";
        value = this.configurationService.getProperty(property);
        logger.info(value);
        assertEquals("file:///Users/babyboy/datalake/", value);

        property = "thread_pool_size";
        value = this.configurationService.getProperty(property);
        logger.info(value);
        assertEquals("25", value);

        property = "flink_host";
        value = this.configurationService.getProperty(property);
        logger.info(value);
        assertEquals("127.0.0.1", value);

        property = "flink_port";
        value = this.configurationService.getProperty(property);
        logger.info(value);
        assertEquals("8081", value);
    }
}
