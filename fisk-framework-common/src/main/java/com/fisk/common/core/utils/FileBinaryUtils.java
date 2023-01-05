package com.fisk.common.core.utils;

import org.springframework.util.FileCopyUtils;

import java.io.*;

/**
 * 文件互转二进制帮助类
 * @author JianWenYang
 */
public class FileBinaryUtils {

    /**
     * 文件转为二进制字符串
     *
     * @param filePath
     * @return
     */
    public static String fileToBinStr(String filePath) {
        try {
            File file = new File(filePath);
            InputStream fis = new FileInputStream(file);
            byte[] bytes = FileCopyUtils.copyToByteArray(fis);
            return new String(bytes, "ISO-8859-1");
        } catch (Exception ex) {
            throw new RuntimeException("transform file into bin String 出错", ex);
        }
    }

    /**
     * 二进制字符串转File
     *
     * @param bin
     * @param filePath
     * @param fileName
     * @return
     */
    public static File binToFile(String bin, String filePath, String fileName) {
        try {
            File file = new File(filePath, fileName);
            file.createNewFile();
            byte[] bytes1 = bin.getBytes("ISO-8859-1");
            FileCopyUtils.copy(bytes1, file);
            return file;
        } catch (Exception ex) {
            throw new RuntimeException("transform bin into File 出错", ex);
        }
    }

    public static InputStream getInputStream(String fileBinary) throws UnsupportedEncodingException {
        byte[] bytes = fileBinary.getBytes("ISO-8859-1");
        return new ByteArrayInputStream(bytes);
    }


}
