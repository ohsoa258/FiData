package com.fisk.dataaccess.utils.httprequest.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fisk.dataaccess.dto.api.httprequest.ApiHttpRequestDTO;
import com.fisk.dataaccess.utils.httprequest.IBuildHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/4/28 11:30
 */
@Slf4j
public class BuildHttpRequestImpl implements IBuildHttpRequest {

    @Override
    public JSONObject httpRequest(ApiHttpRequestDTO dto) {
        try {
            // Body: raw-json参数
            String json = JSON.toJSONString(dto.jsonObject);
            String result = null;

            if (dto.httpRequestEnum.getValue() == 2) { // post
                result = sendPostRequest(dto, json);
            } else { // get
                result = sendGetRequest(dto, json);
            }
            return JSONObject.parseObject(result);
        } catch (Exception e) {
            log.error("执行httpRequest方法失败,【失败原因为：】", e);
        }
        return null;
    }

    @Override
    public String getRequestToken(ApiHttpRequestDTO dto) {
        try {
            String json = JSON.toJSONString(dto.jwtRequestDTO);

            String result = sendPostRequest(dto, json);

            JSONObject jsonObject = JSONObject.parseObject(result);
            String bearer = "Bearer ";
            String token = (String) jsonObject.get("token");

            return bearer + token;
        } catch (Exception e) {
            log.error("执行httpRequest方法失败,【失败原因为：】", e);
        }
        return null;
    }

    private String sendPostRequest(ApiHttpRequestDTO dto, String json) throws IOException {
        HttpClient client = new DefaultHttpClient();
        // post请求
        HttpPost httpPost = new HttpPost(dto.uri);

        httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
        if (StringUtils.isNotBlank(dto.requestHeader)) {
            httpPost.setHeader("Authorization", dto.requestHeader);
        }

        // 页面自定义的请求头信息
        if (!dto.headersParams.isEmpty()) {
            dto.headersParams.forEach(httpPost::setHeader);
        }

        if (StringUtils.isNotBlank(json)) {
            httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        }

        // form-data数据
        if (!dto.formDataParams.isEmpty()) {
            List<BasicNameValuePair> formDataList = new ArrayList<>();
            for (Map.Entry<String, String> entry : dto.formDataParams.entrySet()) {
                formDataList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            // form-data请求方式
            httpPost.setEntity(new UrlEncodedFormEntity(formDataList, StandardCharsets.UTF_8));
        }


        HttpResponse response = client.execute(httpPost);
        HttpEntity entity = response.getEntity();
        //解析返回数据
        String result = EntityUtils.toString(entity, "UTF-8");
        log.info("执行httpRequest方法成功,【返回信息为：】,{}", result);
        return result;
    }

    private String sendGetRequest(ApiHttpRequestDTO dto, String json) throws IOException {
        HttpClient client = new DefaultHttpClient();
        // post请求
//        HttpPost request = new HttpPost(dto.uri);
        HttpGet request = new HttpGet(dto.uri);
        request.setHeader("Content-Type", "application/json; charset=utf-8");

        // 页面自定义的请求头信息
        if (!dto.headersParams.isEmpty()) {
            dto.headersParams.forEach(request::setHeader);
        }

        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        //解析返回数据
        String result = EntityUtils.toString(entity, "UTF-8");
        log.info("执行httpRequest方法成功,【返回信息为：】,{}", result);
        return result;
    }
}
