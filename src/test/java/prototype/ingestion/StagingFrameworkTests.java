package prototype.ingestion;

import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.Util;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import test.components.BaseTest;

import javax.inject.Inject;

@QuarkusTest
public class StagingFrameworkTests extends BaseTest {

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private StagingArea stagingArea;

    @Test
    public void testModelIntegration() throws Exception{
        String datasetLocation = "ingestion/algorithm/input_array.json";
        String json = Util.loadResource(datasetLocation);

        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        String pipeId = "staging_pipe";
        String data = json;

        this.stagingArea.receiveDataForStorage(securityToken,
                pipeId,
                data);

        this.stagingArea.runIntegrationAgent(securityToken, pipeId);
    }
}
