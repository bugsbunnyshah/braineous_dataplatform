package com.appgallabs.dataplatform.configuration;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import test.components.BaseTest;

import javax.inject.Inject;

@QuarkusTest
public class ConfigurationServiceTests extends BaseTest {

    @Inject
    private ConfigurationService configurationService;

    @Test
    public void testConfigurationService() throws Exception{
        String confLocation = "localhost-services/braineous/conf/braineous.config";
        this.configurationService.configure(confLocation);

        String property = "colors.background";
        System.out.println(this.configurationService.getProperty(property));
    }
}
