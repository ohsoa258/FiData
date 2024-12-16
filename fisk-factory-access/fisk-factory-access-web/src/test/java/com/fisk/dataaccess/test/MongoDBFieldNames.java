package com.fisk.dataaccess.test;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class MongoDBFieldNames {
    public static void main(String[] args) {
        MongoClient mongoClient = null;
        try {
            // 连接到 MongoDB 数据库
            mongoClient = MongoClients.create(MongoClientSettings.builder()
                    .applyToClusterSettings(builder ->
                            builder.hosts(java.util.Collections.singletonList(new ServerAddress("192.168.21.21", 27017))))
                    .credential(MongoCredential.createCredential("fisk", "admin", "password01!".toCharArray()))
                    .build());

            MongoDatabase database = mongoClient.getDatabase("Fisk_Test_Mongodb");
            MongoCollection<Document> collection = database.getCollection("orders");
            FindIterable<Document> documents = collection.find();

            HashSet<String> fieldNames = new HashSet<>();

            for (Document document : documents) {
                Set<String> keys = document.keySet();
                fieldNames.addAll(keys);
                for (String key : keys) {
                    System.out.println("数据：" + document.get(key));
                }
            }
            // 打印字段名称
            System.out.println("Fields in the collection: " + fieldNames);
        } catch (Exception e) {
            log.error("mongodb获取schema信息失败，原因：" + e.getMessage());
        } finally {
            // 关闭 MongoClient
            if (mongoClient != null) {
                mongoClient.close();
            }
        }
    }
}
