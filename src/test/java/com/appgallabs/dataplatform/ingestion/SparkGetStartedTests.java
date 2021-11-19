package com.appgallabs.dataplatform.ingestion;

/*import io.bugsbunny.dataIngestion.service.Skills;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;*/

public class SparkGetStartedTests{
   /* private static Logger logger = LoggerFactory.getLogger(SparkGetStartedTests.class);

    //@Test
    public void testFirstStart() throws Exception
    {
        String logFile = "README.md"; // Should be some file on your system
        SparkSession spark = SparkSession
                .builder()
                .appName("Java Spark SQL basic example")
                .config("spark.master", "local")
                .getOrCreate();
        Dataset<String> logData = spark.read().textFile(logFile).cache();

        long numAs = logData.filter((FilterFunction<String>) s -> s.contains("a")).count();
        long numBs = logData.filter((FilterFunction<String>) s -> s.contains("b")).count();

        logger.info("**********");
        logger.info("Lines with a: " + numAs + ", lines with b: " + numBs);
        logger.info("**********");

        spark.stop();
    }

    //@Test
    public void testFirstRDD() throws Exception
    {
        String master = "local";
        SparkConf conf = new SparkConf().setAppName("testFirstRDD").setMaster(master);
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
        JavaRDD<Integer> distData = sc.parallelize(data);

        final Integer sum = distData.reduce((a, b) -> a + b);

        logger.info("*******");
        logger.info("Sum is: "+sum);
        logger.info("*******");

        sc.stop();
    }

    //@Test
    public void testMapReduce() throws Exception
    {
        Skills skills = new Skills();

        int lineCount = skills.countLines("skills.json");

        logger.info("*******");
        logger.info("Total Length: "+lineCount);
        logger.info("*******");
    }

    //@Test
    public void testFileReadingWriting() throws Exception
    {
        SparkSession spark = SparkSession
                .builder()
                .appName("FileReadingWriting")
                .config("spark.master", "local")
                .getOrCreate();

        DataFrameReader reader = spark.read();
        Dataset<Row> skillsDataSet = reader.csv("airlines.dat");
        Dataset<String> json = skillsDataSet.toJSON();

        Iterator<String> itr = json.toLocalIterator();
        logger.info("*******");
        while(itr.hasNext())
        {
            logger.info(itr.next());
        }
        logger.info("*******");



        //logger.info("*******");
        //logger.info("Dataset Row Count: "+skillsDataSet.count());
        //skillsDataSet.show();
        //logger.info("*******");

        //skillsDataSet.write().parquet("sampleParquet.parquet");

        String[] columns = {"name","iata","icao","alternativeName","country","callSign"};

        spark.close();
    }*/
}
