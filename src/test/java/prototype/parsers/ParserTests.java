package prototype.parsers;

import com.appgallabs.dataplatform.ingestion.util.CSVDataUtil;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;

import com.google.gson.JsonArray;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.json.JSONObject;
import org.json.XML;

import org.junit.jupiter.api.Test;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

public class ParserTests {

    @Test
    public void testDetectXml() throws Exception{
        String valid = Util.loadResource("parsers/xml/valid.xml");
        String invalid = Util.loadResource("parsers/xml/invalid.xml");

        System.out.println(JsonUtil.isXmlValid(valid));
        System.out.println(JsonUtil.isXmlValid(invalid));
    }

    @Test
    public void testDetectCsv() throws Exception{
        String valid = Util.loadResource("parsers/csv/valid.csv");
        String invalid = Util.loadResource("parsers/csv/invalid.csv");
        String random = Util.loadResource("parsers/csv/random.csv");


        System.out.println(JsonUtil.isCsvValid(valid));
        System.out.println(JsonUtil.isCsvValid(invalid));
    }

    @Test
    public void testDetectJson() throws Exception{
        String valid = Util.loadResource("parsers/json/valid.json");
        String invalid = Util.loadResource("parsers/json/invalid.json");

        System.out.println(JsonUtil.isJsonValid(valid));
        System.out.println(JsonUtil.isJsonValid(invalid));
    }
}
