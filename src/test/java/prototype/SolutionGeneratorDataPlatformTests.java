package prototype;

import com.appgallabs.dataplatform.ingestion.service.IngestionService;
import com.appgallabs.dataplatform.ingestion.service.MapperService;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.BackgroundProcessListener;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.IngesterTest;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class SolutionGeneratorDataPlatformTests extends IngesterTest {
    private static Logger logger = LoggerFactory.getLogger(SolutionGeneratorDataPlatformTests.class);

    @Inject
    private MapperService mapperService;

    @Inject
    private IngestionService ingestionService;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    //@Test
    public void calculateSolutionForArrivalDelaySmallDataSet() throws Exception {
        String data = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                getResourceAsStream("crew/optimizeCrew.json"), StandardCharsets.UTF_8);

        JsonObject json = JsonParser.parseString(data).getAsJsonObject();
        JsonArray dataArray = json.get("data").getAsJsonArray();
        JsonUtil.print(dataArray);

        BackgroundProcessListener.getInstance().setThreshold(dataArray.size());

        JsonObject result = this.mapperService.map("flight",dataArray);
        System.out.println(result);

        System.out.println("*****WAITING********");
        synchronized (BackgroundProcessListener.getInstance().getReceiver()) {
            BackgroundProcessListener.getInstance().getReceiver().wait();
        }

        System.out.println("*****TERMINATING********");
        System.out.println(BackgroundProcessListener.getInstance().getReceiver().getData());


        //Read ingested data
        String dataLakeId = result.get("dataLakeId").getAsString();
        JsonArray flightIngestion = this.ingestionService.readDataLakeData(dataLakeId);
        for(int i=0; i<flightIngestion.size();i++)
        {
            System.out.println(flightIngestion.get(i));
        }
    }
}
