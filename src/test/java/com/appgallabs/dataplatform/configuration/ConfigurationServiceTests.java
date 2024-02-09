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
        String confLocation = "localhost-services/braineous/conf/braineous.config";
        this.configurationService.configure(confLocation);

        String property = "colors.background";
        String value = this.configurationService.getProperty(property);
        logger.info(value);
        assertEquals("#FFFFFF", value);
    }
}
