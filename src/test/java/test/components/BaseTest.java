package test.components;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.infrastructure.TenantStore;
import com.appgallabs.dataplatform.infrastructure.security.ApiKeyManager;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.UUID;

public abstract class BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(BaseTest.class);

    @Inject
    private ApiKeyManager apiKeyManager;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;


    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @BeforeEach
    public void setUp() throws Exception
    {
        this.securityTokenContainer = new SecurityTokenContainer();
        this.generateTenant();
    }

    @AfterEach
    public void tearDown() throws Exception
    {
        //this.cleanup();
    }

    private void cleanup(){
        String principal = this.securityTokenContainer.getTenant().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        this.mongoDBJsonStore.getMongoClient().getDatabase(databaseName).drop();
    }

    private void generateTenant(){
        try
        {
            TenantStore tenantStore = this.mongoDBJsonStore.getTenantStore();
            MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

            //generate a tenant
            String tenantName = UUID.randomUUID().toString();
            String tenantEmail = tenantName+"@email.com";
            Tenant tenant = new Tenant();
            tenant.setName(tenantName);
            tenant.setEmail(tenantEmail);

            JsonObject credentials = this.apiKeyManager.generareApiKey();
            String apiKey = credentials.get("apiKey").getAsString();
            String apiKeySecret = credentials.get("apiSecret").getAsString();

            tenant.setPrincipal(apiKey);
            tenant.setApiSecret(apiKeySecret);

            Tenant adminTenant = tenant;
            tenantStore.createTenant(adminTenant,
                    mongoClient,
                    tenant);


            //create a security token
            SecurityToken securityToken = new SecurityToken();
            securityToken.setPrincipal(apiKey);
            securityToken.setToken(apiKeySecret);
            this.securityTokenContainer.setSecurityToken(securityToken);

            logger.info("**************************TEST_COMPONENT***************************************");
            logger.info("(SecurityTokenContainer): " + this.securityTokenContainer);
            logger.info("(Principal): " + this.securityTokenContainer.getSecurityToken().getPrincipal());
            logger.info("(Token): " + this.securityTokenContainer.getSecurityToken().getToken());
            logger.info("*****************************************************************");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
