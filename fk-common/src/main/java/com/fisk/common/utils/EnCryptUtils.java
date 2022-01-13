package com.fisk.common.utils;
import java.util.Base64;

/**
 * @author dick
 * @version v1.0
 * @description 加解密工具类
 * @date 2022/1/12 13:10
 */
public class EnCryptUtils {
    /**
     * base64加密
     * @param content 待加密内容
     * @return byte[]
     */
    public static byte[] base64Encrypt(final String content) {
        return Base64.getEncoder().encode(content.getBytes());
    }

    /**
     * base64解密
     * @param encoderContent 已加密内容
     * @return byte[]
     */
    public static byte[] base64Decrypt(final byte[] encoderContent) {
        return Base64.getDecoder().decode(encoderContent);
    }
}
