package com.fisk.dataaccess.utils.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fisk.dataaccess.dto.api.httprequest.ApiHttpRequestDTO;
import com.fisk.dataaccess.dto.api.httprequest.JwtRequestDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/4/28 12:36
 */
public class JsonObjectParseDemo {

    public static void main(String[] args) {


        ApiHttpRequestDTO dto1 = new ApiHttpRequestDTO();
        dto1.uri = "http://192.168.11.140:8080/api/getToken";
        JwtRequestDTO jwtRequestDTO = new JwtRequestDTO();
        jwtRequestDTO.username = "zhaofeihong";
        jwtRequestDTO.password = "Password01!";
        dto1.jwtRequestDTO = jwtRequestDTO;
        // 获取token
        String requestToken = getRequestToken(dto1);
        System.out.println(requestToken);

        ApiHttpRequestDTO dto2 = new ApiHttpRequestDTO();
        dto2.uri = "http://192.168.11.140:8080/api/Members/167ad2e0-31fa-4b5b-bbee-1f15de5a9ee1";
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("pageNum", "1");
        jsonObject1.put("pageSize", "10");
        dto2.jsonObject = jsonObject1;
        dto2.requestHeader = requestToken;
        JSONObject result = httpRequest(dto2);
        Object data = result.get("data");

        System.out.println(data);
    }

    public static JSONObject httpRequest(ApiHttpRequestDTO dto) {
        try {
            String json = JSON.toJSONString(dto.jsonObject);

            String result = sendRequest(dto, json);
            return JSONObject.parseObject(result);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getRequestToken(ApiHttpRequestDTO dto) {
        try {
            String json = JSON.toJSONString(dto.jwtRequestDTO);

            String result = sendRequest(dto, json);

            JSONObject jsonObject = JSONObject.parseObject(result);
            String bearer = "Bearer ";
            String token = (String) jsonObject.get("token");

            return bearer + token;
        } catch (Exception e) {

        }
        return null;
    }

    private static String sendRequest(ApiHttpRequestDTO dto, String json) throws IOException {
        HttpClient client = new DefaultHttpClient();
        // post请求
        HttpPost request = new HttpPost(dto.uri);

        request.setHeader("Content-Type", "application/json; charset=utf-8");
        if (StringUtils.isNotBlank(dto.requestHeader)) {
            request.setHeader("Authorization", dto.requestHeader);
        }

        request.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        //解析返回数据
        return EntityUtils.toString(entity, "UTF-8");
    }

}
