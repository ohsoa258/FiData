package com.fisk.system.test;

import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoNamespace;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

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
            MongoCollection<Document> collection = fisk_test_mongodb.getCollection("_schema");

            Map<String, List<String>> stringListHashMap = new HashMap<>();
            //查找collection中的所有数据
            for (Document document : collection.find()) {
                List<String> tb_columns1 = new ArrayList<>();
                String tblName = (String)document.get("table");
                Object fields = document.get("fields");
                JSONArray jsonArray = new JSONArray(fields.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String fieldName = jsonObject.getString("name");
                    String fieldType = jsonObject.getString("type");
                    tb_columns1.add(fieldName);
                }
                stringListHashMap.put(tblName,tb_columns1);
            }
            System.out.println("================================");
            System.out.println(stringListHashMap);
            System.out.println("================================");


            MongoDbUtils mongoDbUtils = new MongoDbUtils();
            List<TablePyhNameDTO> fisk_test_mongodb1 = mongoDbUtils.getTrueTableNameList(mongoClient, "Fisk_Test_Mongodb");
            System.out.println(fisk_test_mongodb1);


            Set<String> keys1 = new HashSet<>();
            List<String> tb_columns2 = new ArrayList<>();
            //查找collection中第一行的所以列名
            Document first = collection.find().first();
            for (String k : first.keySet()) {
                if (!keys1.contains(k)) {
                    tb_columns2.add(k);
                    keys1.add(k);
                }
            }
            System.out.println("================================");
            System.out.println(tb_columns2);
            System.out.println("================================");

        }


    }
}