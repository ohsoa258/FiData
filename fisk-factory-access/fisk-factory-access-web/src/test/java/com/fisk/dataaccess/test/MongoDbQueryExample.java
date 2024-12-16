package com.fisk.dataaccess.test;

import com.fisk.dataaccess.utils.sql.DbConnectionHelper;
import com.fisk.dataaccess.utils.sql.MongoDbUtils;
import com.mongodb.client.MongoClient;

public class MongoDbQueryExample {
    public static void main(String[] args) {
        // 创建 MongoClient
        MongoClient mongoClient = DbConnectionHelper
                .myMongoClient("192.168.21.21", 27017, "admin", "fisk", "password01!");
        MongoDbUtils mongoDbUtils = new MongoDbUtils();
        mongoDbUtils.mongodbQuery(mongoClient, "Fisk_Test_Mongodb",
                "orders",
                "{\"username\": \"Tom\"}",
                "{\"_id\": 1, \"username\": 1, \"product\": 1, \"price\": 1, \"type\": 1}");

        mongoDbUtils.mongodbQuery(mongoClient, "Fisk_Test_Mongodb",
                "orders",
                null,
                null);

        mongoDbUtils.mongodbQuery(mongoClient, "Fisk_Test_Mongodb",
                "orders",
                "{\"username\": \"Tom\"}",
                null);

        mongoDbUtils.mongodbQuery(mongoClient, "Fisk_Test_Mongodb",
                "orders",
                null,
                "{\"_id\": 1, \"username\": 1, \"product\": 1, \"price\": 1, \"type\": 1}");
    }
}
