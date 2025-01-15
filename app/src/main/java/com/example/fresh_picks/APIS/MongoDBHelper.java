package com.example.fresh_picks.APIS;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBHelper {
    private static final String CONNECTION_STRING = "mongodb://hananhij11:KWlNeSrsrMLwo5KA@cluster0-shard-00-00.s3p3r.mongodb.net:27017,Fresh_Picks-shard-01-01:27017/?retryWrites=true&w=majority";
    private static final String DATABASE_NAME = "freshpicks";

    private static MongoClient mongoClient;
    private static MongoDatabase database;

    // Initialize the MongoDB connection
    public static void init() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
        }
    }

    // Get a collection
    public static MongoCollection<Document> getCollection(String collectionName) {
        if (database == null) {
            init();
        }
        return database.getCollection(collectionName);
    }

    // Get the database
    public static MongoDatabase getDatabase() {
        if (database == null) {
            init();
        }
        return database;
    }

    // Close the MongoDB connection
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
        }
    }
}
