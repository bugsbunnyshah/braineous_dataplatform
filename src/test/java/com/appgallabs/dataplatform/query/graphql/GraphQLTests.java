package com.appgallabs.dataplatform.query.graphql;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.commons.io.IOUtils;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

import static org.junit.jupiter.api.Assertions.*;

import com.appgallabs.dataplatform.util.JsonUtil;


@QuarkusTest
public class GraphQLTests {
    private static Logger logger = LoggerFactory.getLogger(GraphQLTests.class);

    @Test
    public void testAllProducts() throws Exception{
        String restUrl = "http://localhost:8080/graphql/";

        String queryJson = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("graphql/datafetcher.json"),
                StandardCharsets.UTF_8);
        JsonElement jsonElement = JsonParser.parseString(queryJson);
        JsonUtil.printStdOut(jsonElement);

        String input = jsonElement.toString();

        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .POST(HttpRequest.BodyPublishers.ofString(input))
                .build();


        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String responseJson = httpResponse.body();
        int statusCode = httpResponse.statusCode();
        assertEquals(200, statusCode);

        JsonElement responseJsonElement = JsonParser.parseString(responseJson);
        JsonUtil.printStdOut(responseJsonElement);
    }
}
