package com.appgallabs.dataplatform.query.graphql;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.ingestion.pipeline.PipelineService;
import com.appgallabs.dataplatform.ingestion.pipeline.PipelineServiceTests;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.text.MessageFormat;

import static org.junit.jupiter.api.Assertions.*;

import com.appgallabs.dataplatform.util.JsonUtil;
import test.components.BaseTest;

import javax.inject.Inject;


@QuarkusTest
public class GraphQLTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(GraphQLTests.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private PipelineService pipelineService;

    @Inject
    private QueryService queryService;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Test
    public void testAll() throws Exception{
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("graphql/input.json"),
                StandardCharsets.UTF_8
        );
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        String originalObjectHash = JsonUtil.getJsonHash(jsonObject);

        String entity = "books";
        this.pipelineService.ingest(this.securityTokenContainer.getSecurityToken(),entity,jsonString);

        JsonArray ingestion = this.mongoDBJsonStore.readIngestion(this.securityTokenContainer.getTenant(),
                originalObjectHash);

        JsonObject storedJson = ingestion.get(0).getAsJsonObject();
        JsonUtil.printStdOut(storedJson);

        //JsonArray document = this.queryService.getDocumentByLakeId(originalObjectHash);
        //JsonUtil.printStdOut(document);

        String restUrl = "http://localhost:8080/graphql/";
        JsonObject documentQueryJson = new JsonObject();
        //documentQueryJsonString = "\"{documentByLakeId {dataLakeId}}\"";


        String query = "query documentByLakeId {documentByLakeId(dataLakeId: \""+originalObjectHash+"\") {data}}";
        logger.info(query);

        String queryJsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("graphql/getDocumentByLakeId.json"),
                StandardCharsets.UTF_8
        );
        JsonObject queryJsonObject = JsonParser.parseString(queryJsonString).getAsJsonObject();
        queryJsonObject.addProperty("query",query);
        String input = queryJsonObject.toString();
        logger.info(input);

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
