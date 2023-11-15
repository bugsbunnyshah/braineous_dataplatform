package tutorial.usecase;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static void setupSourceStore(JsonObject configJson, JsonArray dataSet){
        //get the driver configuration
        String connectionString = configJson.get("connectionString").getAsString();
        String database = configJson.get("database").getAsString();
        String collection = configJson.get("collection").getAsString();

        //setup driver components
        MongoClient mongoClient = MongoClients.create(connectionString);
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> dbCollection = db.getCollection(collection);

        //bulk insert
        List<WriteModel<Document>> bulkOperations = new ArrayList<>();
        int size = dataSet.size();
        for (int i = 0; i < size; i++) {
            JsonObject dataToBeStored = dataSet.get(i).getAsJsonObject();

            Document document = Document.parse(dataToBeStored.toString());

            InsertOneModel<Document> doc1 = new InsertOneModel<>(document);

            bulkOperations.add(doc1);
        }
        dbCollection.bulkWrite(bulkOperations);
    }
}
