package com.appgallabs.dataplatform.ingestion.service;

/*import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;*/

public class Skills{
    /*private static Logger logger = LoggerFactory.getLogger(Skills.class);

    private JavaRDD<String> lines;

    public Skills()
    {

    }

    public int countLines(String fileName)
    {
        String master = "local";
        SparkConf conf = new SparkConf().setAppName("fileNameOperations").setMaster(master);
        JavaSparkContext sc = new JavaSparkContext(conf);

        this.lines = sc.textFile("skills.json");

        JavaRDD<Integer> lineLengths = lines.map(new Function<String, Integer>() {
            public Integer call(String s) { return s.length(); }
        });
        int totalLength = lineLengths.reduce(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer a, Integer b) { return a + b; }
        });

        sc.stop();

        return totalLength;
    }*/
}
