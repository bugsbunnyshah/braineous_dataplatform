package com.appgallabs.dataplatform.infrastructure.security;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class JWTTokenManagerTests {
    private static Logger logger = LoggerFactory.getLogger(JWTTokenManagerTests.class);

    @Inject
    private JWTTokenManager jwtTokenManager;

    @Test
    public void issueServiceComponent() throws Exception{
        String token = this.jwtTokenManager.issueToken();
        logger.info(token);
    }

    @Test
    public void issueEndpoint() throws Exception{
        Response response = given().get("/data/security/issue").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");

        String generatedToken = response.body().toString();
        assertEquals(200, response.getStatusCode());
        assertNotNull(generatedToken);
    }
}
