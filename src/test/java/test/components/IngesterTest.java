package test.components;

import com.appgallabs.dataplatform.deprecated.StreamIngesterContext;
import com.appgallabs.dataplatform.util.BGNotificationReceiver;
import com.appgallabs.dataplatform.util.BackgroundProcessListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class IngesterTest extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(IngesterTest.class);

    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        BGNotificationReceiver receiver = new BGNotificationReceiver();
        BackgroundProcessListener.getInstance().setReceiver(receiver);

        StreamIngesterContext.getStreamIngester().start();
    }

    @AfterEach
    void tearDown() {
        try {
            StreamIngesterContext.getStreamIngester().stop();
        }catch (Exception e)
        {
            logger.error(e.getMessage(),e);
        }
        finally {
            BackgroundProcessListener.getInstance().clear();
        }
        super.tearDown();
    }
}
