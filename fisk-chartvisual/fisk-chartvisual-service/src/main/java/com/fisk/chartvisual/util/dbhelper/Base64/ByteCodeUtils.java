package com.fisk.chartvisual.util.dbhelper.Base64;

import javax.xml.bind.DatatypeConverter;

/**
 * @author WangYan
 * @date 2021/11/17 14:47
 * Base64 工具类
 */
public class ByteCodeUtils {

    /**
     * 字节转base64
     * @param image
     * @return
     */
    public static String byteConvertStringFun(byte[] image){
        if (image==null){
            return  "";
        }
        return DatatypeConverter.printBase64Binary(image);
    }

    /**
     * base64转字节
     * @param image
     * @return
     */
    public static byte[] stringConvertByteFun(String image){
        if (image==null){
            return  null;
        }
        return DatatypeConverter.parseBase64Binary(image);
    }
}
