package com.appgallabs.dataplatform.util;

import com.appgallabs.dataplatform.endpoint.OAuthAuthenticateTests;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
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

    public static JsonElement apiPostRequest(String endpoint, String body, SecurityToken securityToken){
        try {
            String apiKey = securityToken.getPrincipal();
            String apiKeySecret = securityToken.getToken();

            Response response = given().body(body).
                    when().
                    header("x-api-key", apiKey).
                    header("x-api-key-secret", apiKeySecret).
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


    public static JsonElement apiGetRequest(String endpoint, SecurityToken securityToken){
        try {
            String apiKey = securityToken.getPrincipal();
            String apiKeySecret = securityToken.getToken();

            Response response = given().
                    when().
                    header("x-api-key", apiKey).
                    header("x-api-key-secret", apiKeySecret).
                    get(endpoint).
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

    public static JsonElement apiDeleteRequest(String endpoint, SecurityToken securityToken){
        try {
            String apiKey = securityToken.getPrincipal();
            String apiKeySecret = securityToken.getToken();

            Response response = given().
                    when().
                    header("x-api-key", apiKey).
                    header("x-api-key-secret", apiKeySecret).
                    delete(endpoint).
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
