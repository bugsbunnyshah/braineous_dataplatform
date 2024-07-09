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
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        String valid = Util.loadResource("parsers/xml/valid.xml");
        String invalid = Util.loadResource("parsers/xml/invalid.xml");

        // parse valid XML file
        builder.parse(new InputSource(new StringReader(valid)));
        System.out.println("**************************************");
        System.out.println("*****PARSE_SUCCESS**********");
        System.out.println("**************************************");

        //parse invalid XML file
        builder.parse(new InputSource(new StringReader(invalid)));
    }

    @Test
    public void testDetectCsv() throws Exception{
        String valid = Util.loadResource("parsers/csv/valid.csv");
        String invalid = Util.loadResource("parsers/csv/invalid.csv");

        JsonArray validArray = CSVDataUtil.convert(valid);
        JsonUtil.printStdOut(validArray);

        JsonArray invalidArray = CSVDataUtil.convert(invalid);
        JsonUtil.printStdOut(invalidArray);

        CSVReader reader = new CSVReaderBuilder(new StringReader(invalid)).build();
        String [] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            // nextLine[] is an array of values from the line
            System.out.println(nextLine[0]);
        }
    }

    @Test
    public void testDetectJson() throws Exception{
        String valid = Util.loadResource("parsers/json/valid.json");
        String invalid = Util.loadResource("parsers/json/invalid.json");

        JsonUtil.printStdOut(JsonUtil.validateJson(valid));

        System.out.println("**************************************");
        System.out.println("*****PARSE_SUCCESS**********");
        System.out.println("**************************************");

        JsonUtil.printStdOut(JsonUtil.validateJson(invalid));
    }
}
