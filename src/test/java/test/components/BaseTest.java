package test.components;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public abstract class BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(BaseTest.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenMockComponent securityTokenMockComponent;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @BeforeEach
    public void setUp() throws Exception
    {
        this.tearDown();
    }

    @AfterEach
    public void tearDown() throws Exception
    {
        this.securityTokenMockComponent.start();
        this.cleanup();
    }

    private void cleanup(){
        try {
            if (this.mongoDBJsonStore == null) {
                this.mongoDBJsonStore = new MongoDBJsonStore();
            }
            if(this.securityTokenContainer != null && this.securityTokenContainer.getSecurityToken()!= null) {
                String principal = this.securityTokenContainer.getTenant().getPrincipal();
                String databaseName = principal + "_" + "aiplatform";
                this.mongoDBJsonStore.getMongoClient().getDatabase(databaseName).drop();
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
        }
    }
}
