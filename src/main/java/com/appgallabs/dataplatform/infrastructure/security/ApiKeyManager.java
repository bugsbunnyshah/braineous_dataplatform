package com.appgallabs.dataplatform.infrastructure.security;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.infrastructure.TenantStore;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class ApiKeyManager {

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    public JsonObject generareApiKey(){
        JsonObject jsonObject = new JsonObject();

        String apiKey = UUID.randomUUID().toString();
        String apiSecret = UUID.randomUUID().toString();

        jsonObject.addProperty("apiKey", apiKey);
        jsonObject.addProperty("apiSecret", apiSecret);

        return jsonObject;
    }

    public boolean authenticate(String apiKey, String apiSecret){
        Tenant adminTenant = this.securityTokenContainer.getTenant();
        TenantStore tenantStore = this.mongoDBJsonStore.getTenantStore();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        //get from tenant store
        Tenant tenant = tenantStore.getTenant(adminTenant,
                mongoClient,
                apiKey);
        String storedApiSecret = tenant.getApiSecret();
        String storedApiKey = tenant.getPrincipal();

        if((apiKey.equals(storedApiKey)) && (apiSecret.equals(storedApiSecret))){
            return true;
        }

        return false;
    }
}
