package com.appgallabs.dataplatform.client.sdk.service;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AuthenticationServiceTests {
    private static Logger logger = LoggerFactory.getLogger(AuthenticationServiceTests.class);

    //TODO: solidify CR2
    @Test
    public void sendData() throws Exception{
        AuthenticationService service = AuthenticationService.getInstance();

        JsonObject response = service.authenticate();

        //assertions
        JsonUtil.printStdOut(response);
        assertNotNull(response);
    }
}
