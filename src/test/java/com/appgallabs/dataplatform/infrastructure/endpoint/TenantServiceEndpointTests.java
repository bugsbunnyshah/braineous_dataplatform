package com.appgallabs.dataplatform.infrastructure.endpoint;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.infrastructure.TenantService;
import com.appgallabs.dataplatform.infrastructure.security.ApiKeyManager;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.ApiUtil;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.BaseTest;

import javax.inject.Inject;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TenantServiceEndpointTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(TenantServiceEndpointTests.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;
    @Inject
    private TenantService tenantService;

    @Inject
    private ApiKeyManager apiKeyManager;

    @Test
    public void testValidationNameRequired() throws Exception{
        String tenantName = UUID.randomUUID().toString();
        String tenantEmail = tenantName+"@email.com";

        //create tenant
        String createEndpoint = "/tenant_manager/create_tenant";
        JsonObject payload = new JsonObject();
        //payload.addProperty("name", tenantName);
        payload.addProperty("email", tenantEmail);

        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonObject createResponseJson = ApiUtil.apiPostRequest(createEndpoint,payload.toString(),securityToken)
                .getAsJsonObject();
        assertTrue(createResponseJson.has("tenant_name_required"));
    }

    @Test
    public void testValidationEmailRequired() throws Exception{
        String tenantName = UUID.randomUUID().toString();
        String tenantEmail = tenantName+"@email.com";

        //create tenant
        String createEndpoint = "/tenant_manager/create_tenant";
        JsonObject payload = new JsonObject();
        payload.addProperty("name", tenantName);
        //payload.addProperty("email", tenantEmail);

        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonObject createResponseJson = ApiUtil.apiPostRequest(createEndpoint,payload.toString(),securityToken)
                .getAsJsonObject();
        assertTrue(createResponseJson.has("tenant_email_required"));
    }

    @Test
    public void testValidationEmailInvalid() throws Exception{
        String tenantName = UUID.randomUUID().toString();
        String tenantEmail = tenantName+"email.com";

        //create tenant
        String createEndpoint = "/tenant_manager/create_tenant";
        JsonObject payload = new JsonObject();
        payload.addProperty("name", tenantName);
        payload.addProperty("email", tenantEmail);

        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonObject createResponseJson = ApiUtil.apiPostRequest(createEndpoint,payload.toString(),securityToken)
                .getAsJsonObject();
        assertTrue(createResponseJson.has("tenant_email_invalid"));
    }

    @Test
    public void testValidationUniqueness() throws Exception{
        String tenantName = UUID.randomUUID().toString();
        String tenantEmail = tenantName+"@email.com";

        //create tenant
        String createEndpoint = "/tenant_manager/create_tenant";
        JsonObject payload = new JsonObject();
        payload.addProperty("name", tenantName);
        payload.addProperty("email", tenantEmail);

        for(int i=0; i<2; i++) {
            SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
            ApiUtil.apiPostRequest(createEndpoint, payload.toString(),securityToken)
                    .getAsJsonObject();
        }
    }

    @Test
    public void endToEnd() throws Exception{
        String tenantName = UUID.randomUUID().toString();
        String tenantEmail = tenantName+"@email.com";

        //create tenant
        String createEndpoint = "/tenant_manager/create_tenant";
        JsonObject payload = new JsonObject();
        payload.addProperty("name", tenantName);
        payload.addProperty("email", tenantEmail);

        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonObject createResponseJson = ApiUtil.apiPostRequest(createEndpoint,payload.toString(),securityToken)
                .getAsJsonObject();
        String apiKey = createResponseJson.get("apiKey").getAsString();

        //read tenant
        String readEndpoint = "/tenant_manager/get_tenant/"+apiKey+"/";
        JsonObject readResponseJson = ApiUtil.apiGetRequest(readEndpoint,securityToken).getAsJsonObject();
        Tenant storedTenant = Tenant.parse(readResponseJson.toString());
        assertEquals(tenantName,storedTenant.getName());
        assertEquals(tenantEmail,storedTenant.getEmail());
        assertEquals(apiKey,storedTenant.getPrincipal());

        //authenticate tenant
        Tenant tenant = this.tenantService.getTenant(apiKey);
        String apiSecret = tenant.getApiSecret();
        boolean success = this.apiKeyManager.authenticate(apiKey,apiSecret);
        boolean failure = this.apiKeyManager.authenticate(apiKey, "blah");
        assertTrue(success);
        assertFalse(failure);
    }
}
