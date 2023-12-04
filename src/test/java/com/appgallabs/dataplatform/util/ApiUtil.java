package com.appgallabs.dataplatform.util;

import com.appgallabs.dataplatform.endpoint.OAuthAuthenticateTests;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;

public class ApiUtil {
    private static Logger logger = LoggerFactory.getLogger(ApiUtil.class);

    public static JsonElement apiPostRequest(String endpoint, String body){
        try {
            //get OAuth Token
            String credentials = IOUtils.resourceToString("oauth/credentials.json",
                    StandardCharsets.UTF_8,
                    Thread.currentThread().getContextClassLoader());
            JsonObject credentialsJson = JsonParser.parseString(credentials).getAsJsonObject();
            String tenant = credentialsJson.get("client_id").getAsString();

            String token = IOUtils.resourceToString("oauth/jwtToken.json",
                    StandardCharsets.UTF_8,
                    Thread.currentThread().getContextClassLoader());
            JsonObject securityTokenJson = JsonParser.parseString(token).getAsJsonObject();
            String generatedToken = securityTokenJson.get("access_token").getAsString();

            Response response = given().body(body).
                    when().
                    header("tenant", tenant).
                    header("token", "Bearer "+generatedToken).
                    post(endpoint).
                    andReturn();

            logger.info("************************");
            logger.info(response.statusLine());
            String responseJsonString = response.body().prettyPrint();
            logger.info("************************");

            return JsonUtil.validateJson(responseJsonString);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
