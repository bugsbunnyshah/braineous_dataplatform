package com.appgallabs.dataplatform.infrastructure.security;

import com.appgallabs.dataplatform.infrastructure.Http;
import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class JWTTokenManagerTests {
    private static Logger logger = LoggerFactory.getLogger(JWTTokenManagerTests.class);

    @Inject
    private Http http;

    @Test
    public void issueEndpoint() throws Exception{
        JsonObject input = new JsonObject();
        input.addProperty("tenant","random_tenant");
        input.addProperty("username","test@test.com");
        input.addProperty("password","password");

        Response response = given().body(input.toString()).when().post("/data/security/issue")
                .andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");

        String generatedToken = response.body().toString();
        assertEquals(200, response.getStatusCode());
        assertNotNull(generatedToken);
    }

    //@Test
    public void testAuthenticationMissingToken() throws Exception{
        String restUrl = "http://localhost:8080/data/security/issue";

        //Create the Experiment
        HttpClient httpClient = http.getHttpClient();

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .GET()
                .build();


        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String responseJson = httpResponse.body();
        int status = httpResponse.statusCode();
        int expectedStatus = 200;
        assertEquals(expectedStatus,status);

        String generatedToken = responseJson;
        logger.info(generatedToken);

        String endpoint = "http://localhost:8080/data/microservice";
        httpRequestBuilder = HttpRequest.newBuilder();
        httpRequest = httpRequestBuilder.uri(new URI(endpoint))
                .GET()
                .build();


        httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        responseJson = httpResponse.body();
        status = httpResponse.statusCode();
        expectedStatus = 401;
        assertEquals(expectedStatus,status);
    }

    //@Test
    public void testAuthenticationSuccess() throws Exception{
        String restUrl = "http://localhost:8080/data/security/issue";

        //Create the Experiment
        HttpClient httpClient = http.getHttpClient();

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .GET()
                .build();


        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String responseJson = httpResponse.body();
        int status = httpResponse.statusCode();
        int expectedStatus = 200;
        assertEquals(expectedStatus,status);

        String generatedToken = responseJson;
        logger.info(generatedToken);

        String endpoint = "http://localhost:8080/data/microservice";
        httpRequestBuilder = HttpRequest.newBuilder();
        httpRequest = httpRequestBuilder.uri(new URI(endpoint))
                .header("Authorization", "Bearer "+generatedToken)
                .GET()
                .build();


        httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        responseJson = httpResponse.body();
        status = httpResponse.statusCode();
        expectedStatus = 200;
        assertEquals(expectedStatus,status);
        logger.info(responseJson);
    }

    //@Test
    public void testAuthenticationFailed() throws Exception{
        String restUrl = "http://localhost:8080/data/security/issue";

        //Create the Experiment
        HttpClient httpClient = http.getHttpClient();

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .GET()
                .build();


        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String responseJson = httpResponse.body();
        int status = httpResponse.statusCode();
        int expectedStatus = 200;
        assertEquals(expectedStatus,status);

        String generatedToken = responseJson;
        logger.info(generatedToken);

        String endpoint = "http://localhost:8080/data/microservice";
        httpRequestBuilder = HttpRequest.newBuilder();
        httpRequest = httpRequestBuilder.uri(new URI(endpoint))
                .header("Authorization", "Bearer "+generatedToken)
                .GET()
                .build();


        httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        responseJson = httpResponse.body();
        status = httpResponse.statusCode();
        expectedStatus = 200;
        assertEquals(expectedStatus,status);
        logger.info(responseJson);
    }
}
