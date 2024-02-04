package prototype.ingestion;

import com.google.gson.JsonArray;

import java.util.List;

public interface Storage {
    public void storeData(List<Record> dataset);
}
