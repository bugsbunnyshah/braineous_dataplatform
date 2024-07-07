package prototype.stores;

import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;

public class IcebergSparkTests {

    @Test
    public void testDrive() throws Exception{
        //get a spark session
        SparkSession spark = SparkSession
                .builder()
                .appName("Java Spark SQL data sources example")
                .config("spark.master", "local")
                .getOrCreate();


    }

    @Test
    public void testBulkWrite() throws Exception{

    }

    @Test
    public void testRead() throws Exception{

    }
}
