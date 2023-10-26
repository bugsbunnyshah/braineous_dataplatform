package prototype.receiver.framework;

import com.google.gson.JsonObject;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;


public class StoreOrchestratorTests {

    @Test
    public void testConfigurationFramework() throws Exception{

        String connectionString = "mongodb+srv://dbuser:dbpassword@cluster0.example.mongodb.net/?retryWrites=true&w=majority";

        connectionString = "mongodb://localhost:27017"; //config
        String db = "sampledb"; //config
        String collectionName = "books"; //config
        MongoClient mongoClient = MongoClients.create(connectionString);
        MongoDatabase database = mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection(collectionName);


        //bulk insert
        try {
            List<WriteModel<Document>> bulkOperations = new ArrayList<>();
            for(int i=0; i<3; i++) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("title",""+i);

                Document document = Document.parse(jsonObject.toString());

                InsertOneModel<Document> doc1 = new InsertOneModel<>(document);

                bulkOperations.add(doc1);
            }
            collection.bulkWrite(bulkOperations);

        } catch (MongoBulkWriteException e){
            System.out.println("A MongoBulkWriteException occured with the following message: " + e.getMessage());
        }

        System.out.println(collection);
    }
}
