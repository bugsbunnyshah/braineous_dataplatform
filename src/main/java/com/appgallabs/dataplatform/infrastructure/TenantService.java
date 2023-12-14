package com.appgallabs.dataplatform.infrastructure;

import com.appgallabs.dataplatform.infrastructure.security.ApiKeyManager;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.google.gson.JsonObject;

import com.mongodb.client.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TenantService {
    private static Logger logger = LoggerFactory.getLogger(TenantService.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private ApiKeyManager apiKeyManager;

    public Tenant createTenant(String name, String email){
        Tenant adminTenant = this.securityTokenContainer.getTenant();
        TenantStore tenantStore = this.mongoDBJsonStore.getTenantStore();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        Tenant tenant = new Tenant();

        tenant.setName(name);
        tenant.setEmail(email);

        JsonObject credentials = this.apiKeyManager.generareApiKey();
        String apiKey = credentials.get("apiKey").getAsString();
        String apiSecret = credentials.get("apiSecret").getAsString();

        tenant.setPrincipal(apiKey);
        tenant.setApiSecret(apiSecret);

        //store to the tenant store
        tenantStore.createTenant(adminTenant,
                mongoClient,
                tenant);

        return tenant;
    }

    public Tenant getTenant(String apiKey){
        Tenant adminTenant = this.securityTokenContainer.getTenant();
        TenantStore tenantStore = this.mongoDBJsonStore.getTenantStore();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        //get from tenant store
        Tenant tenant = tenantStore.getTenant(adminTenant,
                mongoClient,
                apiKey);

        return tenant;
    }
}
