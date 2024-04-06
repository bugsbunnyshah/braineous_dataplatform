package com.appgallabs.dataplatform.ingestion.util;

import com.appgallabs.dataplatform.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class JobManagerUtilTests {
    private static Logger logger = LoggerFactory.getLogger(JobManagerUtilTests.class);

    @Test
    public void generateAlphabetString() throws Exception{
        String input = "ffb2969c-5182-454f-9a0b-f3f2fb0ebf75";
        String expected = "ffbaaaacaaaaaaaaafaaaabafafafbaebfaa";

        String alphabeticString = JobManagerUtil.generateAlphabetString(input);

        logger.info(alphabeticString);

        //asserts
        assertEquals(expected, alphabeticString);
    }
}
