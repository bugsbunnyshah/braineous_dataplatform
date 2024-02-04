package prototype.ingestion;

import com.google.gson.JsonObject;

public class Record {
    private RecordMetaData recordMetaData;
    private JsonObject data;

    public RecordMetaData getRecordMetaData() {
        return recordMetaData;
    }

    public void setRecordMetaData(RecordMetaData recordMetaData) {
        this.recordMetaData = recordMetaData;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Record{" +
                "recordMetaData=" + recordMetaData +
                ", data=" + data +
                '}';
    }
}
