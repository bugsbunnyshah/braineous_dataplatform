package prototype.graphql;

import com.appgallabs.dataplatform.TestConstants;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.ingestion.pipeline.PipelineService;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.IOUtils;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

import com.appgallabs.dataplatform.util.JsonUtil;
import test.components.BaseTest;
import test.components.Util;

import javax.inject.Inject;


//@QuarkusTest
public class GraphQLTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(GraphQLTests.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private PipelineService pipelineService;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Tenant tenant = this.securityTokenContainer.getTenant();

        String jsonString = Util.loadResource("pipeline/mongodb_config_1.json");

        Registry registry = Registry.getInstance();
        registry.registerPipe(tenant, JsonUtil.validateJson(jsonString).getAsJsonObject());
    }
}
