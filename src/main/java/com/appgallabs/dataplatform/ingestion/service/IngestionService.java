package com.appgallabs.dataplatform.ingestion.service;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class IngestionService implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(IngestionService.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private MapperService mapperService;

    private Map<String,FetchAgent> fetchAgents;

    private Map<String,PushAgent> pushAgents;

    public IngestionService(){
        this.fetchAgents = new HashMap<>();
        this.pushAgents = new HashMap<>();
    }

    @PostConstruct
    public void onStart(){
        try {
            String agentRegistrationJson = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                            getResourceAsStream("ingestionAgents.json"),
                    StandardCharsets.UTF_8);
            JsonArray jsonArray = JsonParser.parseString(agentRegistrationJson).getAsJsonArray();
            for(int i=0; i<jsonArray.size(); i++){
                JsonObject json = jsonArray.get(i).getAsJsonObject();
                String id = json.get("id").getAsString();
                String fetchAgent = json.get("fetchAgent").getAsString();
                String pushAgent = json.get("pushAgent").getAsString();

                Class fetchAgentClass = Thread.currentThread().getContextClassLoader().loadClass(fetchAgent);
                Class pushAgentClass = Thread.currentThread().getContextClassLoader().loadClass(pushAgent);

                this.fetchAgents.put(id, (FetchAgent) fetchAgentClass.getDeclaredConstructor().newInstance());
                this.pushAgents.put(id, (PushAgent) pushAgentClass.getDeclaredConstructor().newInstance());
            }
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public JsonObject ingestDevModelData(String data)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("data", data);

        //long dataLakeId = this.mongoDBJsonStore.storeIngestion(jsonObject);
        //jsonObject.addProperty("dataLakeId", dataLakeId);

        return jsonObject;
    }

    public JsonArray readDataLakeData(String dataLakeId)
    {
        JsonArray ingestion = this.mongoDBJsonStore.getIngestion(this.securityTokenContainer.getTenant(), dataLakeId);
        return ingestion;
    }

    public void ingestData(String agentId, String entity){
        FetchAgent fetchAgent = this.fetchAgents.get(agentId);
        if(!fetchAgent.isStarted()) {
            fetchAgent.setSecurityTokenContainer(this.securityTokenContainer);
            fetchAgent.setTenant(this.securityTokenContainer.getTenant());
            fetchAgent.setEntity(entity);
            fetchAgent.setMapperService(this.mapperService);
            fetchAgent.startFetch();
        }
    }

    public void ingestData(String agentId, String entity,JsonArray data){
        PushAgent pushAgent = this.pushAgents.get(agentId);
        pushAgent.setSecurityTokenContainer(securityTokenContainer);
        pushAgent.setTenant(this.securityTokenContainer.getTenant());
        pushAgent.setEntity(entity);
        pushAgent.setMapperService(this.mapperService);
        pushAgent.receiveData(data);
    }
}
