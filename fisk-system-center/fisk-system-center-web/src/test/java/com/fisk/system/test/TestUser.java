package com.fisk.system.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

import static org.apache.commons.io.Charsets.UTF_8;

/**
 * @author: Lock
 * @data: 2021/5/31 12:13
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestUser {
    public static void main(String[] args) {


        String garbledString = "鏉ㄤ繚鍗?";

        Charset gbkCharset = Charset.forName("GBK");
        CharsetDecoder gbkDecoder = gbkCharset.newDecoder();

        Charset utf8Charset = StandardCharsets.UTF_8;
        CharsetEncoder utf8Encoder = utf8Charset.newEncoder();

        try {
            byte[] utf8Bytes = garbledString.getBytes(utf8Charset);
            String decodedString = gbkDecoder.decode(ByteBuffer.wrap(utf8Bytes)).toString();
            System.out.println(decodedString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
