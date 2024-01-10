package com.fisk.dataaccess.utils.test;

import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.utils.sql.MongoDbUtils;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import java.util.ArrayList;
import java.util.List;

public class MongoDBJDBCExample {

    public static void main(String[] args) throws Exception {
        ServerAddress serverAddress = new ServerAddress("192.168.21.21", 27017);
        List<ServerAddress> serverAddresses = new ArrayList<>();
        serverAddresses.add(serverAddress);

        MongoCredential scramSha1Credential = MongoCredential.createScramSha1Credential("fisk", "admin", "password01!".toCharArray());
        List<MongoCredential> mongoCredentials = new ArrayList<>();
        mongoCredentials.add(scramSha1Credential);


        MongoClient mongoClient = new MongoClient(serverAddresses, mongoCredentials);


        MongoDbUtils mongoDbUtils = new MongoDbUtils();
        List<TablePyhNameDTO> fisk_test_mongodb1 = mongoDbUtils.getTrueTableNameList(mongoClient, "Fisk_Test_Mongodb");
        System.out.println(fisk_test_mongodb1);

    }

}