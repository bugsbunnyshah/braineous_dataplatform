package prototype.ingestion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class RecordGenerator {
    private static Logger logger = LoggerFactory.getLogger(RecordGenerator.class);

    public List<Record> parsePayload(String payload){
        JsonElement jsonElement = JsonParser.parseString(payload);
        List<Record> input = new ArrayList<>();
        if(jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject inputJson = jsonArray.get(i).getAsJsonObject();

                Record record = new Record();
                record.setData(inputJson);
                input.add(record);
            }
        }else if(jsonElement.isJsonObject()){
            Record record = new Record();
            record.setData(jsonElement.getAsJsonObject());
            input.add(record);
        }
        return input;
    }
}
