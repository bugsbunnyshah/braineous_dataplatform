package com.appgallabs.dataplatform.configuration;

import io.quarkus.test.junit.QuarkusTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@QuarkusTest
public class AIPlatformConfigTests
{
    private static Logger logger = LoggerFactory.getLogger(AIPlatformConfigTests.class);

    @Inject
    private AIPlatformConfig aiPlatformConfig;

    //@Test
    public void testGetConfiguration() throws Exception
    {
        logger.info(this.aiPlatformConfig.getConfiguration().toString());
    }
}
