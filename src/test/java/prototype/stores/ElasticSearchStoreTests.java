package prototype.stores;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class ElasticSearchStoreTests {
    private static Logger logger = LoggerFactory.getLogger(ElasticSearchStoreTests.class);

    @Test
    public void testBulkWrite() throws Exception{
        String bulkPostUrl = "http://localhost:9200/_bulk?pretty";
        String username = "elastic";
        String password = "password";

        //setup payload
        String payloadString = "{ \"index\" : { \"_index\" : \"educative\"} }\n" +
                "{ \"articleName\" : \"elasticsearch-intro\" }\n" +
                "{ \"index\" : { \"_index\" : \"educative\"} }\n" +
                "{ \"articleName\" : \"elasticsearch-insert-data\" }\n" +
                "{ \"index\" : { \"_index\" : \"educative\"} }\n" +
                "{ \"articleName\" : \"elasticsearch-test\" }\n";
        System.out.println(payloadString);

        //send request
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(bulkPostUrl))
                .header("Authorization", basicAuth(username, password))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payloadString))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String statusCode = "" + httpResponse.statusCode();
        String result = httpResponse.body();
        System.out.println(statusCode);
        System.out.println(result);
    }

    @Test
    public void testRead() throws Exception{
        String id = "USLyiJABKUp0mf7dDWPv";
        String readUrl = "http://localhost:9200/educative/_doc/"+id+"?pretty";
        String username = "elastic";
        String password = "password";

        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(readUrl))
                .header("Authorization", basicAuth(username, password))
                .GET()
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String statusCode = "" + httpResponse.statusCode();
        String result = httpResponse.body();
        System.out.println(statusCode);
        System.out.println(result);
    }

    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }
}
