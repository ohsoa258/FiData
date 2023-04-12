package com.fisk.common.core.utils;



import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class LicenseEnCryptUtils {

    private static final String SECRET_KEY = "@w#DJt.8o8H&YjJS";

    private static final String VECTOR_KEY = "SC#35*dGQozSMT5a";

    /**
     * AES/CBC/PKCS5Padding 加密
     *
     * @param content    :待加密的内容.
     *  secret_key :用于生成密钥的 key，自定义即可，加密与解密必须使用同一个，如果不一致，则抛出异常
     *  vector_key 用于生成算法参数规范的 key，自定义即可，加密与解密必须使用同一个，如果不一致，解密的内容可能会造成与源内容不一致.
     *                   <p>
     *                   1、secret_key、vector_key: AES 时必须是 16 个字节，DES 时必须是 8 字节.
     *                   2、secret_key、vector_key 值不建议使用中文，如果是中文，注意一个汉字是3个字节。
     *                   </p>
     * @return 返回 Cipher 加密后的数据，对加密后的字节数组用 Base64 进行编码转成了可视字符串，如 7giH2bqIMH3kDMIg8gq0nY
     * @throws Exception
     */
    public static String encrypt(String content) throws Exception {
        //实例化 Cipher 对象。使用：AES-高级加密标准算法、CBC-有向量模式、PKCS5Padding-填充方案:（加密内容不足8位时用余位数补足8位）
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //使用 SecretKeySpec(byte[] key, String algorithm) 创建密钥. 算法要与 Cipher.getInstance 保持一致.
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        /**
         * init(int opMode,Key key,AlgorithmParameterSpec params)：初始化 Cipher，
         * 1、Cipher.ENCRYPT_MODE 表示加密模式
         * 2、key 表示加密密钥
         * 3、params 表示算法参数规范，使用 CBC 有向量模式时，必须传入,如果是 ECB-无向量模式,那么可以不传
         * 4、所有参数规范都必须实现 {@link AlgorithmParameterSpec} 接口.
         */
        IvParameterSpec parameterSpec = new IvParameterSpec(VECTOR_KEY.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        /**
         * byte[] doFinal(byte[] content)：对 content 完成加密操作，如果 cipher.init 初始化时使用的解密模式，则此时是解密操作.
         * 返回的是加密后的字节数组，如果直接 new String(byte[] bytes) 是会乱码的，可以借助 BASE64 转为可视字符串，或者转成 16 进制字符
         */
        byte[] encrypted = cipher.doFinal(content.getBytes());
        //BASE64Encoder.encode：BASE64 对字节数组内容进行编码，转为可视字符串，这样方便存储和转换.
        String base64Encode = new BASE64Encoder().encode(encrypted);
        return base64Encode;
    }

    /**
     * AES/CBC/PKCS5Padding 解密
     *
     * @param base64Encode :待解密的内容，因为加密时使用了 Base64 进行了编码，所以这里传入的也是 Base64 编码后的可视化字符串
     *  secret_key   :用于生成密钥的 key，自定义即可，加密与解密必须使用同一个，如果不一致，则抛出异常
     *  vector_key   用于生成算法参数规范的 key，自定义即可，加密与解密必须使用同一个，如果不一致，解密的内容可能会造成与源内容不一致.
     *                     <p>
     *                     1、secret_key、vector_key：AES 时必须是 16 个字节，DES 时必须是 8 字节.
     *                     2、secret_key、vector_key 值不建议使用中文，如果是中文，注意一个汉字是3个字节。
     *                     </p>
     * @return
     * @throws Exception
     */
    public static String decrypt(String base64Encode) throws Exception {
        //实例化 Cipher 对象。加密算法/反馈模式/填充方案，解密与加密需要保持一致.
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //创建密钥。算法也要与实例化 Cipher 一致.
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        //有向量模式(CBC)需要传入 AlgorithmParameterSpec 算法参数规范参数.
        IvParameterSpec parameterSpec = new IvParameterSpec(VECTOR_KEY.getBytes());
        //初始化 cipher。使用解密模式.
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        //将 Base64 编码的内容解码成字节数组(因为加密的时候，对密文使用了 Base64编码，所以这里需要先解码)
        byte[] content =  new BASE64Decoder().decodeBuffer(base64Encode);
        //执行解密操作。返回解密后的字节数组，此时可以使用 String(byte bytes[]) 转成源字符串.
        byte[] decrypted = cipher.doFinal(content);
        return new String(decrypted);
    }
}
