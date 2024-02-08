package prototype.ingestion;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.components.BaseTest;

import javax.inject.Inject;

@QuarkusTest
public class StagingFrameworkTests extends BaseTest {

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private StagingArea stagingArea;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Tenant tenant = this.securityTokenContainer.getTenant();

        String jsonString = test.components.Util.loadResource("prototype/pipeline/pipeline_config_1.json");

        Registry registry = Registry.getInstance();
        registry.registerPipe(tenant, JsonUtil.validateJson(jsonString).getAsJsonObject());
    }

    @Test
    public void receiveData() throws Exception{
        String datasetLocation = "ingestion/algorithm/input_array.json";
        String json = Util.loadResource(datasetLocation);

        String pipeId = "book_pipe";
        String entity = "books";
        String data = json;

        this.stagingArea.receiveDataForStorage(
                pipeId,
                entity,
                data);

        this.stagingArea.runIntegrationAgent(pipeId, entity);
    }

    @Test
    public void testConfigurationService() throws Exception{
        String confLocation = "localhost-services/braineous/conf/braineous.config";
        this.configurationService.configure(confLocation);

        String property = "colors.background";
        System.out.println(this.configurationService.getProperty(property));
    }
}
