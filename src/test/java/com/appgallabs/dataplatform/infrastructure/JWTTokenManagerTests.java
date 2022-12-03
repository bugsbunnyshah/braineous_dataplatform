package com.appgallabs.dataplatform.infrastructure;

import com.appgallabs.dataplatform.endpoint.OAuthAuthenticateTests;
import com.appgallabs.dataplatform.infrastructure.security.JWTTokenManager;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

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
        String token = this.jwtTokenManager.issueToken();
        logger.info(token);
    }
}
