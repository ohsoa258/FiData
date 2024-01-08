package com.fisk.system.test;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoNamespace;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MongoDBJDBCExample {

    public static void main(String[] args) throws Exception {
        ServerAddress serverAddress = new ServerAddress("192.168.21.21", 27017);
        List<ServerAddress> serverAddresses = new ArrayList<>();
        serverAddresses.add(serverAddress);

        MongoCredential scramSha1Credential = MongoCredential.createScramSha1Credential("fisk", "admin", "password01!".toCharArray());
        List<MongoCredential> mongoCredentials = new ArrayList<>();
        mongoCredentials.add(scramSha1Credential);


        MongoClient mongoClient = new MongoClient(serverAddresses, mongoCredentials);
        //库名
        MongoDatabase fisk_test_mongodb = mongoClient.getDatabase("Fisk_Test_Mongodb");
        //集合名
        MongoCollection<Document> orders = fisk_test_mongodb.getCollection("orders");
        MongoNamespace namespace = orders.getNamespace();
        long l = orders.countDocuments();
        //获取集合的索引(类似主键)名称
        List<String> indexs = new ArrayList<>();
        ListIndexesIterable<Document> documents1 = orders.listIndexes();
        for (Document document : documents1) {
            Map<String, String> key = (Map<String, String>) document.get("key");
            Set<String> indexNames = key.keySet();
            for (String index : indexNames) {
                indexs.add(index);
            }
        }


        //
        FindIterable<Document> documents = orders.find();
        MongoCursor<Document> iterator = documents.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }


        MongoIterable mongoIterable = fisk_test_mongodb.listCollectionNames();
        MongoCursor table = mongoIterable.iterator();
        while (table.hasNext()) {
            //获取表名
            String tableName = table.next().toString();
            //根据collection名获取collection
            MongoCollection<Document> collection = fisk_test_mongodb.getCollection(tableName);
            //查找collection中的所有数据
            FindIterable findIterable = collection.find();
            MongoCursor cursor = findIterable.iterator();
            List<String> tb_columns = new ArrayList<>();
            while (cursor.hasNext()) {
                String str = cursor.next().toString();
                str = str.substring(9, str.length() - 1);
                str = str.replaceAll("[{]", "{\"");
                str = str.replaceAll("[}]", "\"}");
                str = str.replaceAll("=", "\":\"");
                str = str.replaceAll(",", "\",\"");
                str = str.replaceAll(" ", "");
                JSONObject jsonObject = JSONObject.parseObject(str);
                for (String k : jsonObject.keySet()) {
                    int mark = 0;
                    for (String tc : tb_columns) {
                        //避免字段重复
                        if (tc.equals(k))
                            mark = 1;
                    }
                    if (mark == 0) {
                        tb_columns.add(k);
                    }
                }

            }
            System.out.println(tb_columns);
        }


    }
}