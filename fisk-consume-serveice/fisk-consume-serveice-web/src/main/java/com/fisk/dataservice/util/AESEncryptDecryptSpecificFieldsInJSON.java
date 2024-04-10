package com.fisk.dataservice.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AESEncryptDecryptSpecificFieldsInJSON {

    //创建线程池，核心线程数：2；最大线程数：4；时间：5；时间单位：秒；阻塞队列：ArrayBlockingQueue，最大容量为10；线程工厂：默认；拒绝策略：默认
    static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(4, 10, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
//    public static void main(String[] args) throws Exception {
////        String jsonString = "[\n" +
////                "    {\n" +
////                "        \"illegally_num\": \"260\",\n" +
////                "        \"date_key\": \"20240123\",\n" +
////                "        \"accident_num\": \"9\"\n" +
////                "    },\n" +
////                "    {\n" +
////                "        \"illegally_num\": \"260\",\n" +
////                "        \"date_key\": \"20240123\",\n" +
////                "        \"accident_num\": \"9\"\n" +
////                "    }\n" +
////                "]";
////
////        // 加密字段值
////        String key = "mysecretpassword"; // 密钥需要保密
////
////        // 加密字段
////        String encryptedJsonString = encryptFieldsInJson(jsonString, key);
////        System.out.println("Encrypted JSON: " + encryptedJsonString);
////
////        // 解密字段
////        String decryptedJsonString = decryptFieldsInJson(encryptedJsonString.toString(), key);
////        System.out.println("Decrypted JSON: " + decryptedJsonString);
//
//
//        String updateTime = "20240322183234396";
//        String substring = updateTime.substring(0, 16);
//
//        long startTime, endTime;
//        // 测试加密算法加密数据的效率
//        startTime = System.currentTimeMillis();
//
//        int messageCount = 100000; // 要加密的消息数量
//        List<String> messages = new ArrayList<>(messageCount);
//        for (int i = 0; i < messageCount; i++) {
//            messages.add("This is a public message");
//        }
//        List<List<String>> partition = Lists.partition(messages, messageCount / 4 + (messageCount % 4 != 0 ? 1 : 0));
//
//        List<String>[] encryptedLists = new List[partition.size()];
//        CountDownLatch count = new CountDownLatch(partition.size());
//        // 测试加密算法加密数据的效率
//        startTime = System.currentTimeMillis();
//        for (int i = 0; i < partition.size(); i++) {
//            final int index = i;
//            List<String> subList = partition.get(i);
//            poolExecutor.execute(() -> {
//                List<String> newMessage = new ArrayList<>();
//                for (String message : subList) {
//                    try {
//                        message = encryptField(message,"mysecretpassword1234123412341234");
//                        newMessage.add(message);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                encryptedLists[index] = newMessage;
//                }
//                count.countDown();
//            });
//        }
//        count.await();
//
//        // 组装加密后的子列表
//        List<String> combinedList = new ArrayList<>();
//        for (List<String> subList : encryptedLists) {
//            if (subList != null) {
//                combinedList.addAll(subList);
//            }
//        }
//        endTime = System.currentTimeMillis();
//        System.out.println("加密算法加密 " + messageCount + " 个单元格耗时: " + (endTime - startTime) + " 毫秒");
//
//
//
//        // 测试加密算法加密数据的效率
//        startTime = System.currentTimeMillis();
//        for (int i = 0; i < messageCount; i++) {
//            // 这里放入加密算法加密数据的逻辑
//            String message = "This is a public message";
//            message = encryptField(message,"mysecretpassword");
//        }
//        endTime = System.currentTimeMillis();
//        System.out.println("加密算法加密 " + messageCount + " 个单元格耗时: " + (endTime - startTime) + " 毫秒");
//
//        // 测试不加密数据的效率
//        startTime = System.currentTimeMillis();
//        for (int i = 0; i < messageCount; i++) {
//            // 这里放入不加密数据的逻辑
//            String message = "This is a public message";
//        }
//        endTime = System.currentTimeMillis();
//        System.out.println("不加密 " + messageCount + " 个单元格耗时: " + (endTime - startTime) + " 毫秒");
//
//    }

    private static String encryptFieldsInJson(String jsonString, String key) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonArray = objectMapper.readTree(jsonString);

        for (JsonNode jsonNode : jsonArray) {
            if (jsonNode.has("illegally_num")) {
                String illegallyNum = jsonNode.get("illegally_num").asText();
                String encryptedIllegallyNum = encryptField(illegallyNum, key);
                ((ObjectNode) jsonNode).put("illegally_num", encryptedIllegallyNum);
            }

            if (jsonNode.has("accident_num")) {
                String accidentNum = jsonNode.get("accident_num").asText();
                String encryptedAccidentNum = encryptField(accidentNum, key);
                ((ObjectNode) jsonNode).put("accident_num", encryptedAccidentNum);
            }
        }
        return jsonArray.toString();
    }

    private static String encryptField(String fieldValue, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedBytes = cipher.doFinal(fieldValue.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private static String decryptFieldsInJson(String jsonString, String key) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonArray = objectMapper.readTree(jsonString);

        for (JsonNode jsonNode : jsonArray) {
            if (jsonNode.has("illegally_num")) {
                String encryptedIllegallyNum = jsonNode.get("illegally_num").asText();
                String decryptedIllegallyNum = decryptField(encryptedIllegallyNum, key);
                ((ObjectNode) jsonNode).put("illegally_num", decryptedIllegallyNum);
            }

            if (jsonNode.has("accident_num")) {
                String encryptedAccidentNum = jsonNode.get("accident_num").asText();
                String decryptedAccidentNum = decryptField(encryptedAccidentNum, key);
                ((ObjectNode) jsonNode).put("accident_num", decryptedAccidentNum);
            }
        }

        return objectMapper.writeValueAsString(jsonArray);
    }

    private static String decryptField(String encryptedValue, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
        return new String(decryptedBytes);
    }



    public void test(){
        String value = "{\n" +
                "    \"code\": 200,\n" +
                "    \"msg\": \"请求成功\",\n" +
                "    \"data\": {\n" +
                "        \"current\": 1,\n" +
                "        \"size\": 10,\n" +
                "        \"total\": 1,\n" +
                "        \"page\": 1,\n" +
                "        \"encryptKey\": \"mysecretpassword\",\n" +
                "        \"dataArray\": [\n" +
                "            {\n" +
                "                \"table_name_alias\": \"t1tBAQclh3U9eqVQZOuh1J4DcCBwj9CAEB4xVqTZoOU=\",\n" +
                "                \"table_name\": \"DwjmhRMNZBvqdqRy37eGqw==\",\n" +
                "                \"table_type\": \"e+XpsZuiA3pkHGeje7+BEw==\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"table_name_alias\": \"t1tBAQclh3U9eqVQZOuh1J4DcCBwj9CAEB4xVqTZoOU=\",\n" +
                "                \"table_name\": \"DwjmhRMNZBvqdqRy37eGqw==\",\n" +
                "                \"table_type\": \"e+XpsZuiA3pkHGeje7+BEw==\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";

    }

    public static void main(String[] args) {
        String encryptedTableData = "{\n" +
                "    \"code\": 200,\n" +
                "    \"msg\": \"请求成功\",\n" +
                "    \"data\": {\n" +
                "        \"current\": 1,\n" +
                "        \"size\": 10,\n" +
                "        \"total\": 1,\n" +
                "        \"page\": 1,\n" +
                "        \"encryptKey\": \"mysecretpassword\",\n" +
                "        \"dataArray\": [\n" +
                "            {\n" +
                "                \"table_name_alias\": \"srS1j7V53QsaUka6HjLUJnLZCq9kQV+mcAMmImMzX1V3rj2tmnVi2uMbdI9bNZSb\",\n" +
                "                \"table_name\": \"srS1j7V53QsaUka6HjLUJnLZCq9kQV+mcAMmImMzX1V3rj2tmnVi2uMbdI9bNZSb\",\n" +
                "                \"table_type\": \"lMNgph4i2FMlIt8zlZu+Ig==\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"table_name_alias\": \"srS1j7V53QsaUka6HjLUJjDR/6/k1CNjrIqcFbFrugQ=\",\n" +
                "                \"table_name\": \"srS1j7V53QsaUka6HjLUJjDR/6/k1CNjrIqcFbFrugQ=\",\n" +
                "                \"table_type\": \"lMNgph4i2FMlIt8zlZu+Ig==\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        String[] columnNames = {"table_name_alias","table_name","table_type"};


        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonArray = objectMapper.readTree(encryptedTableData);
            String encryptKey = jsonArray.get("data").get("encryptKey").textValue();
            JsonNode jsonNodes = jsonArray.get("data").get("dataArray");
            for (JsonNode jsonNode : jsonNodes) {
                for (String columnName : columnNames) {
                    if (jsonNode.has(columnName)) {
                        String illegallyNum = jsonNode.get(columnName).asText();
                        String encryptedIllegallyNum = decryptField(illegallyNum, encryptKey);
                        ((ObjectNode) jsonNode).put(columnName, encryptedIllegallyNum);
                    }
                }
            }
            String json = objectMapper.writeValueAsString(jsonArray);
            System.out.println(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}