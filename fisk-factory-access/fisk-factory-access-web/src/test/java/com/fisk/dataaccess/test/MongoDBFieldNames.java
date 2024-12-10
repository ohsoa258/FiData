package com.fisk.dataaccess.test;

import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;

public class MongoDBFieldNames {
    public static void main(String[] args) {
        // 连接到 MongoDB 数据库
        try (MongoClient mongoClient = MongoClients.create("mongodb://192.168.21.21:27017")) {
            MongoDatabase database = mongoClient.getDatabase("Fisk_Test_Mongodb");
            MongoCollection<Document> collection = database.getCollection("orders");
            FindIterable<Document> documents = collection.find();

            HashSet<String> fieldNames = new HashSet<>();

            for (Document document : documents) {
                Set<String> keys = document.keySet();
                fieldNames.addAll(keys);
                for (String key : keys) {
                    System.out.println("数据："+document.get(key));
                }


            }


            // 打印字段名称
            System.out.println("Fields in the collection: " + fieldNames);
        }
    }
}
