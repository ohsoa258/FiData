package com.fisk.apiservice.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpUtils {
    public static <T> T sendPostWebRequest(Class<T> c, String url,
                                           String parameters, String token) {
        T t = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
            httpPost.setHeader("Accept", "application/json;charset=utf-8");
            if (token != null && token != "") {
                httpPost.setHeader("Authorization", token);
            }
            RequestConfig config = RequestConfig.custom().
                    setConnectTimeout(35000).setConnectionRequestTimeout(35000).
                    setSocketTimeout(60000).build();
            httpPost.setConfig(config);

            StringEntity entity = new StringEntity(parameters, "UTF-8");
            httpPost.setEntity(entity);
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (resultString != null && resultString != "") {
            t = JSONObject.parseObject(resultString, c);
        }
        return t;
    }

}
