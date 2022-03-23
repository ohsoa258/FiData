package com.fisk.common.utils;

import com.alibaba.fastjson.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dick
 * @author Lock
 * @version v1.0
 * @description 加解密工具类
 * @date 2022/1/12 13:10
 */
public class EnCryptUtils {
    /**
     * base64加密
     *
     * @param content 待加密内容
     * @return byte[]
     */
    public static byte[] base64Encrypt(final String content) {
        return Base64.getEncoder().encode(content.getBytes());
    }

    /**
     * base64解密
     *
     * @param encoderContent 已加密内容
     * @return byte[]
     */
    public static byte[] base64Decrypt(final byte[] encoderContent) {
        return Base64.getDecoder().decode(encoderContent);
    }

    //可配置到Constant中，并读取配置文件注入,16位,自己定义
    private static final String KEY = "jiuhnbty16538907";

    //参数分别代表 算法名称/加密模式/数据填充方式
    private static final String ALGORITHMSTR = "AES/ECB/PKCS5Padding";

    /**
     * AES对称加密
     *
     * @param content    加密的字符串
     * @param encryptKey key值
     * @throws Exception ex
     * @return 加密后的值
     */
    public static String encrypt(String content, String encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(), "AES"));
        byte[] b = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        // 采用base64算法进行转码,避免出现中文乱码
        return Base64.getEncoder().encodeToString(b);

    }

    /**
     * AES对称解密
     *
     * @param encryptStr 解密的字符串
     * @param decryptKey 解密的key值
     * @return 解密后的值
     * @throws Exception ex
     */
    public static String decrypt(String encryptStr, String decryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptKey.getBytes(), "AES"));
        // 采用base64算法进行转码,避免出现中文乱码
        byte[] encryptBytes = Base64.getDecoder().decode(encryptStr);
        byte[] decryptBytes = cipher.doFinal(encryptBytes);
        return new String(decryptBytes);
    }

    public static String encrypt(String content) throws Exception {
        return encrypt(content, KEY);
    }

    public static String decrypt(String encryptStr) throws Exception {
        return decrypt(encryptStr, KEY);
    }


    public static void main(String[] args) throws Exception {
        Map map = new HashMap<String, String>();
        map.put("key", "value");
        map.put("中文", "汉字");
        String content = JSONObject.toJSONString(map);
        System.out.println("加密前：" + content);

        String encrypt = encrypt(content, KEY);
        System.out.println("加密后：" + encrypt);

        String decrypt = decrypt(encrypt, KEY);
        System.out.println("解密后：" + decrypt);
    }

}
