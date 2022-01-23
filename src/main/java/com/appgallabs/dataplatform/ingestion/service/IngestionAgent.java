package com.appgallabs.dataplatform.ingestion.service;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class IngestionAgent extends TimerTask implements Serializable, FetchAgent, PushAgent {
    private static Logger logger = LoggerFactory.getLogger(IngestionAgent.class);

    private Timer timer;
    private String entity;
    private MapperService mapperService;
    private Tenant tenant;
    private SecurityTokenContainer securityTokenContainer;

    public IngestionAgent() {
    }

    @Override
    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    @Override
    public void setSecurityTokenContainer(SecurityTokenContainer securityTokenContainer) {
        this.securityTokenContainer = securityTokenContainer;
    }

    @Override
    public void setEntity(String entity) {
        this.entity = entity;
    }

    @Override
    public void setMapperService(MapperService mapperService) {
        this.mapperService = mapperService;
    }



    public void receiveData(JsonArray data){
        try {
            /*this.receiveData(data);

            JsonObject input = new JsonObject();
            input.addProperty("sourceSchema", "");
            input.addProperty("destinationSchema", "");
            input.addProperty("sourceData", data.toString());
            input.addProperty("entity",entity);
            //Response response = given().body(input.toString()).when().post("/dataMapper/map/")
            //        .andReturn();
            //JsonObject ingestionResult = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
            //System.out.println("***************INGESTION_RESULT*******************");
            //System.out.println(ingestionResult);
            //System.out.println("**************************************************");*/
        }
        catch(Exception pushException){
            throw new RuntimeException(pushException);
        }
    }

    @Override
    public boolean isStarted() {
        if(this.entity == null) {
            return false;
        }
        return true;
    }

    @Override
    public void startFetch() {
        //TODO: FINALIZE_INGESTION_AGENT
        this.timer = new Timer(true);
        //this.timer.schedule(this, 1000, 60*60*1000);
        this.timer.schedule(this, 1000, 60000);
    }

    @Override
    public void run() {
        /*try {
            System.out.println("STARTING_FETCHER");
            System.out.println(this.mapperService);


            SecurityToken securityToken = new SecurityToken();
            securityToken.setPrincipal(this.tenant.getPrincipal());
            this.securityTokenContainer.setSecurityToken(securityToken);

            String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "crew/optimizeCrew.json"),
                    StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseString(sourceData).getAsJsonObject();
            JsonArray array = json.get("data").getAsJsonArray();

            String restUrl = "https://api.aviationstack.com/v1/flights?access_key=680da0736176cb1218acdb0d6e1cc10e";
            this.getFlights(restUrl);


            /*JsonUtil.printStdOut(array);

            BackgroundProcessListener.getInstance().setThreshold(array.size());


            JsonObject result = this.mapperService.map("flight",array);
            System.out.println(result);*/

            /*System.out.println("*****WAITING********");
            synchronized (BackgroundProcessListener.getInstance().getReceiver()) {
                BackgroundProcessListener.getInstance().getReceiver().wait();
            }

            System.out.println("*****TERMINATING********");
            System.out.println(BackgroundProcessListener.getInstance().getReceiver().getData());
        }
        catch (Exception fetchException)
        {
            fetchException.printStackTrace();
            logger.error(fetchException.getMessage(),fetchException);
        }*/
        try {
            SecurityToken securityToken = new SecurityToken();
            securityToken.setPrincipal(this.tenant.getPrincipal());
            this.securityTokenContainer.setSecurityToken(securityToken);

            System.out.println("INGESTING_DATA");
            String data = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                    getResourceAsStream("crew/optimizeCrew.json"), StandardCharsets.UTF_8);

            JsonObject json = JsonParser.parseString(data).getAsJsonObject();
            JsonArray dataArray = json.get("data").getAsJsonArray();
            JsonUtil.print(dataArray);

            JsonObject result = this.mapperService.map("flight", dataArray);
            System.out.println(result);
        }
        catch (Exception fetchException)
        {
            fetchException.printStackTrace();
            logger.error(fetchException.getMessage(),fetchException);
        }
    }

    private void getFlights(String restUrl)
    {
        try {
            JsonArray jsonArray = new JsonArray();
            int offset = 0;
            int limit = 100;
            int total = 0;
            List<Thread> workers = new ArrayList<>();

            HttpClient httpClient = HttpClient.newBuilder().build();
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                    .GET()
                    .build();

            System.out.println(restUrl);
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String data = httpResponse.body();
            System.out.println(data);
            JsonObject pagination = JsonParser.parseString(data).getAsJsonObject().get("pagination").getAsJsonObject();
            total = pagination.get("total").getAsInt();
            System.out.println("TOTAL_NUMBER: "+total);

            while(true) {
                offset += limit;
                if(offset >= (total/100)) {
                    break;
                }

                Thread worker = new Thread(new FetchData(restUrl, 1000, total,offset, limit,jsonArray));
                workers.add(worker);
            }

            //start the workers
            System.out.println("********STARTING_WORKERS********");
            System.out.println("# of Workers: "+workers.size());
            for(Thread worker:workers){
                worker.start();
            }
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

    private class FetchData implements Runnable{
        private String restUrl;
        private int offset;
        private int limit;
        private JsonArray jsonArray;
        private int iterations;

        private FetchData(String restUrl, int numberOfWorkers, int total,int offset, int limit,JsonArray jsonArray){
            this.restUrl = restUrl;
            this.offset = offset;
            this.limit = limit;
            this.jsonArray = jsonArray;
            this.iterations = (total/numberOfWorkers);
        }

        @Override
        public void run() {
            for(int i=0; i<iterations;i++) {
                try {
                    HttpClient httpClient = HttpClient.newBuilder().build();
                    if (this.restUrl.contains("&offset")) {
                        int offsetIndex = this.restUrl.indexOf("&offset");
                        this.restUrl = this.restUrl.substring(0, offsetIndex);
                    }
                    this.restUrl += "&offset=" + offset;

                    HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
                    HttpRequest httpRequest = httpRequestBuilder.uri(new URI(this.restUrl))
                            .GET()
                            .build();

                    HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                    String data = httpResponse.body();
                    if(httpResponse.statusCode() == 200) {
                        JsonArray array = JsonParser.parseString(data).getAsJsonObject().get("data").getAsJsonArray();
                        jsonArray.addAll(array);
                    }

                    System.out.println("OFFSET: " + offset);
                    System.out.println("FETCHED_SO_FAR: " + jsonArray.size());

                    offset += limit;
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }
    }
}
