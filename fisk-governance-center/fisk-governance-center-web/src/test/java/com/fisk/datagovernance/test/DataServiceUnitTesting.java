package com.fisk.datagovernance.test;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DataServiceUnitTesting {

    @Test
    public void getData()
    {
        /*Step 1：getToken*/
        String url = "{api_prd_address}/dataservice/apiService/getToken";
        // set request parameters
        GetTokenRequest getTokenRequest = new GetTokenRequest();
        getTokenRequest.appAccount = "test0233";
        getTokenRequest.appPassword = "test0233";
        String getTokenParams = JSONObject.toJSONString(getTokenRequest);
        // send request
        GetTokenResponse getTokenResponse = sendPostWebRequest(GetTokenResponse.class,
                url, getTokenParams, null);

        /*Step 2：getData*/
        url = "{api_prd_address}/dataservice/apiService/getData";
        // set request parameters
        GetDataRequest getDataRequest = new GetDataRequest();
        getDataRequest.apiCode = "c5bbe530bc0b47e6bfbd256364270365";
        HashMap<String, Object> parmList = new HashMap<>();
        parmList.put("product_colour", "blue");
        parmList.put("product_price", "7000");
        getDataRequest.parmList = parmList;
        getDataRequest.current = 1;
        getDataRequest.size = 500;
        String getDataParams = JSONObject.toJSONString(getDataRequest);
        // send request
        GetDataResponse getDataResponse = sendPostWebRequest(GetDataResponse.class,
                url, getDataParams, getTokenResponse.data);
    }

    public static <T> T sendPostWebRequest(Class<T> c, String url,
                                           String parameters, String token)
    {
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
                if (response!=null){
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

    public class GetTokenRequest
    {
        public String appAccount;

        public String appPassword;
    }

    public static class GetTokenResponse
    {
        public int code;

        public String msg;

        public String data;
    }

    public class GetDataRequest
    {
        public String apiCode;

        public HashMap<String, Object> parmList;

        public Integer current;

        public Integer size;
    }

    public static class GetDataResponse
    {
        public int code;

        public String msg;

        public Data data;
    }

    public static class Data
    {
        public Integer current;

        public Integer size;

        public Integer total;

        public Integer page;

        public List<DataArrayItem> dataArray;
    }

    public static class DataArrayItem
    {
        public String product_name;

        public String product_price;
    }
}
