package com.appgallabs.dataplatform.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.*;
import org.bson.Document;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MongoDBUtil {

    public static Set<String> readCollectionHashes(String connectionString, String database, String collection){
        try {
            Set<String> hashes = new HashSet<>();

            //setup driver components
            MongoClient mongoClient = MongoClients.create(connectionString);
            MongoDatabase db = mongoClient.getDatabase(database);
            MongoCollection<Document> dbCollection = db.getCollection(collection);

            FindIterable<Document> documents = dbCollection.find();
            MongoCursor<Document> cursor = documents.cursor();
            while (cursor.hasNext()) {
                Document document = cursor.next();
                String documentJson = document.toJson();
                JsonObject cour = JsonParser.parseString(documentJson).getAsJsonObject();
                cour.remove("_id");
                String hash = JsonUtil.getJsonHash(cour);
                hashes.add(hash);
            }

            return hashes;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

}
