package com.fisk.common.core.utils.aesutils;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES密钥加密工具类
 */
@Resource
public class AesEncryptionDecryptionUtils {

    //使用示例
    public static void main(String[] args) {
        try {
            // 生成 AES128 密钥
            SecretKey secretKey = generateAes128Key();
            String s = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            // JSON 数据
            String jsonData = "{\"data\": [{\"b\": \"数据b\", \"a\": \"数据a\"}]}";

            // 加密 JSON 数据
            String encryptedData = encryptJsonData(jsonData, secretKey);

            // 输出加密后的数据
            System.out.println("Encrypted Data: " + encryptedData);

            // 解密加密后的数据
            String decryptedData = decryptJsonData(encryptedData, secretKey);

            // 输出解密后的数据
            System.out.println("Decrypted Data: " + decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 生成 AES128 密钥
    public static SecretKey generateAes128Key() throws Exception {
        // 使用AES加密算法
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        // AES128
        keyGen.init(128); // 设置密钥长度为 128 位
        return keyGen.generateKey();
    }

    // 加密 JSON 数据
    public static String encryptJsonData(String jsonData, SecretKey secretKey) throws Exception {
        // 使用 ECB 模式和 PKCS5Padding 填充
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); 
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // 将 JSON 数据转换为字节数组
        byte[] dataBytes = jsonData.getBytes(StandardCharsets.UTF_8);

        // 加密数据
        byte[] encryptedBytes = cipher.doFinal(dataBytes);

        // 将加密后的数据转换为 Base64 编码
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // 解密加密后的数据
    public static String decryptJsonData(String encryptedData, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // 使用 ECB 模式和 PKCS5Padding 填充
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // 将 Base64 编码的数据转换回字节数组
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

        // 解密数据
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // 将解密后的字节数组转换为字符串
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}