package com.appgallabs.dataplatform.endpoint;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class OAuthAuthenticateTests
{
    private static Logger logger = LoggerFactory.getLogger(OAuthAuthenticateTests.class);

    @Test
    public void testOAuthGetStarted() throws Exception
    {
        String credentials = IOUtils.resourceToString("oauth/credentials.json",
                StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        Response response = given().body(credentials.toString()).when().post("/oauth/token/").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
    }
}
