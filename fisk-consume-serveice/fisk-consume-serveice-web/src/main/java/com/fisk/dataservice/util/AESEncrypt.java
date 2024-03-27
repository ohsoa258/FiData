package com.fisk.dataservice.util;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
public class AESEncrypt {
    public static void main(String[] args) {
        String encryptedTableData = "{\n" +
                " \"code\": 200,\n" +
                " \"msg\": \"\",\n" +
                " \"data\": {\n" +
                " \"current\": 1,\n" +
                " \"size\": 10,\n" +
                " \"total\": 1,\n" +
                " \"page\": 1,\n" +
                " \"dataArray\": [\n" +
                " {\n" +
                " \"table_name_alias\":\"srS1j7V53QsaUka6HjLUJnLZCq9kQV+mcAMmImMzX1V3rj2tmnVi2uMbdI9bNZSb\",\n" +
                " \"table_name\":\"srS1j7V53QsaUka6HjLUJnLZCq9kQV+mcAMmImMzX1V3rj2tmnVi2uMbdI9bNZSb\",\n" +
                " \"table_type\": \"lMNgph4i2FMlIt8zlZu+Ig==\"\n" +
                " },\n" +
                " {\n" +
        " \"table_name_alias\":\"srS1j7V53QsaUka6HjLUJjDR/6/k1CNjrIqcFbFrugQ=\",\n" +
                " \"table_name\":\"srS1j7V53QsaUka6HjLUJjDR/6/k1CNjrIqcFbFrugQ=\",\n" +
                " \"table_type\": \"lMNgph4i2FMlIt8zlZu+Ig==\"\n" +
                " }\n" +
                " ]\n" +
                " }\n" +
                "}";
        String encryptKey = "mysecretpassword";
        String[] columnNames = {"table_name_alias","table_name","table_type"};
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonArray = objectMapper.readTree(encryptedTableData);
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
    private static String decryptField(String encryptedValue, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
        return new String(decryptedBytes);
    }
}