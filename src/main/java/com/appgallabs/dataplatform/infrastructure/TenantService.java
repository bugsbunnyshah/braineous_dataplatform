package com.appgallabs.dataplatform.infrastructure;

import com.appgallabs.dataplatform.common.ValidationException;
import com.appgallabs.dataplatform.infrastructure.security.ApiKeyManager;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.ValidationUtil;
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

    public Tenant createTenant(String name, String email) throws ValidationException {
        Tenant adminTenant = this.securityTokenContainer.getTenant();
        TenantStore tenantStore = this.mongoDBJsonStore.getTenantStore();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        //Perform Validation
        JsonObject errorJson = new JsonObject();
        boolean validationIssuesFound = false;

        //name is required
        if(name == null || name.trim().length()==0){
            validationIssuesFound = true;
            errorJson.addProperty("tenant_name_required", "Tenant Name is required");
        }

        //email is required
        if(email == null || email.trim().length()==0){
            validationIssuesFound = true;
            errorJson.addProperty("tenant_email_required", "Tenant Email is required");
        }else if(!ValidationUtil.isEmailValid(email)){
            validationIssuesFound = true;
            errorJson.addProperty("tenant_email_invalid", "Tenant Email is invalid");
        }

        //name "and" email should be unique
        Tenant unique = tenantStore.getTenant(adminTenant,
                mongoClient,
                name,
                email);
        if(unique != null){
            validationIssuesFound = true;
            errorJson.addProperty("tenant_exists", "A tenant with this name and email already exists");
        }

        if(validationIssuesFound){
            ValidationException validationException = new ValidationException(errorJson.toString());
            throw validationException;
        }

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
