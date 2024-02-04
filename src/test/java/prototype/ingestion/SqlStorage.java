package prototype.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SqlStorage implements Storage{
    private static Logger logger = LoggerFactory.getLogger(SqlStorage.class);

    @Override
    public void storeData(List<Record> dataset) {
        logger.info(dataset.toString());
    }
}
