package prototype.infrastructure;

import com.google.gson.JsonObject;

import java.util.Objects;

public class DataLakeObject {
    private String oid;
    private JsonObject json;

    public DataLakeObject(JsonObject json) {
        this.json = json;
        this.oid = json.get("oid").getAsString();
    }

    public JsonObject getJson() {
        return json;
    }

    @Override
    public String toString() {
        return this.json.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataLakeObject that = (DataLakeObject) o;
        return oid.equals(that.oid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oid);
    }
}
