package com.appgallabs.dataplatform.cdc.engine;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@QuarkusTest
public class CDCConductprTests {
    private static Logger logger = LoggerFactory.getLogger(CDCConductprTests.class);

    @Inject
    private CDCConductor cdcConductor;

    @Test
    public void orchestrate() throws Exception {
        this.cdcConductor.orchestrate();
    }
}
