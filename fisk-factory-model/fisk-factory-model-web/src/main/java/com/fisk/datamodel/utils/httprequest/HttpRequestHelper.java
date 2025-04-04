package com.fisk.datamodel.utils.httprequest;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author JianWenYang
 */
@Component
@Slf4j
public class HttpRequestHelper {

    public static String get(String url) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        String result = null;
        try {
            String newUrl = url;
            // 通过址默认配置创建一个httpClient实例
            httpClient = HttpClients.createDefault();
            // 创建httpGet远程连接实例
            HttpGet httpGet = new HttpGet(newUrl);
            RequestConfig requestConfig = getRequestConfig();
            // 为httpGet实例设置配置
            httpGet.setConfig(requestConfig);
            // 执行get请求得到返回对象
            httpResponse = httpClient.execute(httpGet);
            //获取返回状态码
            if (httpResponse.getStatusLine().getStatusCode() != ResultEnum.REQUEST_SUCCESS.getCode()
                    && httpResponse.getStatusLine().getStatusCode() != ResultEnum.ACCEPTED.getCode()) {
                throw new FkException(ResultEnum.BAD_REQUEST);
            }
            // 通过返回对象获取返回数据
            HttpEntity entity = httpResponse.getEntity();
            // 通过EntityUtils中的toString方法将结果转换为字符串
            result = EntityUtils.toString(entity);
        } catch (ClientProtocolException e) {
            log.error("atlas get clientProtocolException ex:", e);
        } catch (IOException e) {
            log.error("atlas get iOException ex:", e);
        } finally {
            // 关闭资源
            if (null != httpResponse) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    log.error("atlas get close httpResponse iOException ex:", e);
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    log.error("atlas get close httpClient iOException ex:", e);
                }
            }
        }
        return result;
    }

    public static String post(String url, String parameter) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        String result = null;

        // 创建httpClient实例
        httpClient = HttpClients.createDefault();
        // 创建httpPost远程连接实例
        HttpPost httpPost = new HttpPost(url);
        // 配置请求参数实例
        RequestConfig requestConfig = getRequestConfig();
        // 为httpPost实例设置配置
        httpPost.setConfig(requestConfig);
        // 设置请求头
        httpPost.addHeader("Content-Type", "application/json");
        //请求参数
        if (parameter != null && parameter.length() > 0) {
            try {
                httpPost.setEntity(new StringEntity(parameter, "UTF-8"));
            } catch (Exception e) {
                log.error("post exception ex:", e);
            }
        }
        try {
            // httpClient对象执行post请求,并返回响应参数对象
            httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() != ResultEnum.REQUEST_SUCCESS.getCode()
                    && httpResponse.getStatusLine().getStatusCode() != ResultEnum.ACCEPTED.getCode()) {
                throw new FkException(ResultEnum.BAD_REQUEST);
            }
            // 从响应对象中获取响应内容
            HttpEntity entity = httpResponse.getEntity();
            result = EntityUtils.toString(entity);
        } catch (ClientProtocolException e) {
            log.error("post clientProtocolException ex:", e);
        } catch (IOException e) {
            log.error("post iOException ex:", e);
        } finally {
            // 关闭资源
            if (null != httpResponse) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    log.error("post close httpResponse iOException ex:", e);
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    log.error("post close httpClient iOException ex:", e);
                }
            }
        }
        return result;
    }

    /**
     * 请求超时配置
     *
     * @return
     */
    private static RequestConfig getRequestConfig() {
        // 设置配置请求参数
        RequestConfig requestConfig = RequestConfig.custom()
                // 连接主机服务超时时间
                .setConnectTimeout(35000)
                // 请求超时时间
                .setConnectionRequestTimeout(35000)
                // 数据读取超时时间
                .setSocketTimeout(60000)
                .build();
        return requestConfig;
    }

}
