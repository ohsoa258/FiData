package com.fisk.system.web;

import com.alibaba.fastjson.JSONObject;
import com.fisk.system.config.SwaggerConfig;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.Tset})
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("测试登录");
    }

    @GetMapping("/getDataServiceToken")
    public ResponseEntity<String> getDataServiceToken() {
        long startTime = System.currentTimeMillis();
        String result = "获取数据服务token，开始时间（毫秒）：" + startTime + "\n";

        /*Step 1：getToken*/
        String url = "http://172.17.1.10:7002/dataservice/apiService/getToken";
        // set request parameters
        GetTokenRequest getTokenRequest = new GetTokenRequest();
        getTokenRequest.appAccount = "Netdisk";
        getTokenRequest.appPassword = "Netdisk@2023";
        String getTokenParams = JSONObject.toJSONString(getTokenRequest);
        // send request
        GetTokenResponse getTokenResponse = sendPostWebRequest(GetTokenResponse.class,
                url, getTokenParams, null);

        long endTime = System.currentTimeMillis();
        result += "获取数据服务token，结束时间（毫秒）：" + endTime + "\n";
        result += "获取数据服务token，运行总时长：" + (endTime - startTime) + "ms\n";
        result += "获取数据服务token，token值：" + getTokenResponse.data;
        return ResponseEntity.ok(result);
    }

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

    public class GetTokenRequest {
        public String appAccount;

        public String appPassword;
    }

    public static class GetTokenResponse {
        public int code;

        public String msg;

        public String data;
    }
}
