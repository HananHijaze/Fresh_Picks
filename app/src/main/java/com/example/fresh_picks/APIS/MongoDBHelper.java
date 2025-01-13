package com.example.fresh_picks.APIS;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBHelper {
    private static final String CONNECTION_STRING = "mongodb://username:password@cluster0.mongodb.net:27017/freshpicks?retryWrites=true&w=majority";
    private static final String DATABASE_NAME = "freshpicks"; // Ensure consistency

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
        return database.getCollection(collectionName);
    }

    // Get the database
    public static MongoDatabase getDatabase() {
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
