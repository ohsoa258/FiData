package com.fisk.task.utils;

import java.io.IOException;

import java.net.URI;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import java.util.List;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import org.apache.http.client.methods.CloseableHttpResponse;

import org.apache.http.client.methods.HttpGet;

import org.apache.http.client.methods.HttpPost;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;

import org.apache.http.entity.StringEntity;

import org.apache.http.impl.client.CloseableHttpClient;

import org.apache.http.impl.client.HttpClients;

import org.apache.http.message.BasicNameValuePair;

import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@Slf4j
public class HttpClientUtil {

    public static String doGet(String url, Map<String, String> param) {
        // 创建Httpclient对象
        CloseableHttpClient httpclient = (CloseableHttpClient) wrapClient();
        String resultString = "";
        CloseableHttpResponse response = null;
        try {
            // 创建uri
            URIBuilder builder = new URIBuilder(url);
            if (param != null) {
                for (String key : param.keySet()) {
                    builder.addParameter(key, param.get(key));
                }
            }
            URI uri = builder.build();
            // 创建http GET请求
            HttpGet httpGet = new HttpGet(uri);
            String nifiToken = NifiHelper.getNifiToken();
            httpGet.setHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON_UTF8));
            httpGet.setHeader("Authorization", nifiToken);
            // 执行请求
            response = httpclient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            log.error("nifi请求报错" + StackTraceHelper.getStackTraceInfo(e));
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpclient.close();
            } catch (IOException e) {
                log.error("nifi请求报错" + StackTraceHelper.getStackTraceInfo(e));
            }
        }
        return resultString;
    }

    public static String doGet(String url) {
        return doGet(url, null);
    }

    public static String doPost(String url, Map<String, String> param) {

        // 创建Httpclient对象
        CloseableHttpClient httpClient = (CloseableHttpClient) wrapClient();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            String nifiToken = NifiHelper.getNifiToken();
            httpPost.setHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON_UTF8));
            httpPost.setHeader("Authorization", nifiToken);
            // 创建参数列表
            if (param != null) {
                List<NameValuePair> paramList = new ArrayList<>();
                for (String key : param.keySet()) {
                    paramList.add(new BasicNameValuePair(key, param.get(key)));
                }
                // 模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            log.error("nifi请求报错" + StackTraceHelper.getStackTraceInfo(e));
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                log.error("nifi请求报错" + StackTraceHelper.getStackTraceInfo(e));
            }
        }
        return resultString;
    }

    /**
     * 原生字符串发送put请求
     *
     * @param url
     * @param jsonStr
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static String doPut(String url, String jsonStr) {
        //log.info("put请求参数与地址,{},{}", url, jsonStr);
        CloseableHttpClient httpClient = (CloseableHttpClient) wrapClient();
        HttpPut httpPut = new HttpPut(url);
        //RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000).setConnectionRequestTimeout(35000).setSocketTimeout(60000).build();
        //httpPut.setConfig(requestConfig);
        String nifiToken = NifiHelper.getNifiToken();
        httpPut.setHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON_UTF8));
        httpPut.setHeader("Authorization", nifiToken);

        CloseableHttpResponse httpResponse = null;
        try {
            httpPut.setEntity(new StringEntity(jsonStr));
            httpResponse = httpClient.execute(httpPut);
            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity);
            //log.info("put请求返回结果" + result);
            return result;
        } catch (ClientProtocolException e) {
            log.error("nifi请求报错" + StackTraceHelper.getStackTraceInfo(e));
        } catch (IOException e) {
            log.error("nifi请求报错" + StackTraceHelper.getStackTraceInfo(e));
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    log.error("nifi请求报错" + StackTraceHelper.getStackTraceInfo(e));
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    log.error("nifi请求报错" + StackTraceHelper.getStackTraceInfo(e));
                }
            }
        }
        return null;
    }


    public static String doPost(String url) {
        return doPost(url, null);
    }

    public static String doPostJson(String url, String json) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = (CloseableHttpClient) wrapClient();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            String nifiToken = NifiHelper.getNifiToken();
            httpPost.setHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON_UTF8));
            httpPost.setHeader("Authorization", nifiToken);
            // 创建请求内容
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            log.error("nifi请求报错" + StackTraceHelper.getStackTraceInfo(e));
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                log.error("nifi请求报错" + StackTraceHelper.getStackTraceInfo(e));
            }
        }
        return resultString;

    }

    public static HttpClient wrapClient() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] arg0,
                                               String arg1) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] arg0,
                                               String arg1) throws CertificateException {
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLConnectionSocketFactory ssf = new SSLConnectionSocketFactory(
                    ctx, NoopHostnameVerifier.INSTANCE);
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setSSLSocketFactory(ssf).build();
            return httpclient;
        } catch (Exception e) {
            return HttpClients.createDefault();
        }
    }

}
