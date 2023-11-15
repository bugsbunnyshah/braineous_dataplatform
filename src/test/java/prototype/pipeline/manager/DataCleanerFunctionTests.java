package prototype.pipeline.manager;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusTest
public class DataCleanerFunctionTests {

    private static Logger logger = LoggerFactory.getLogger(DataCleanerFunctionTests.class);

    @Test
    public void testJython() throws Exception{
        logger.info("***********");
        logger.info("TEST_JYTHON");
        logger.info("***********");
    }
}
