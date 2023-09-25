package com.fisk.dataservice.handler.restapi.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.entity.TableApiAuthRequestPO;
import com.fisk.dataservice.entity.TableApiResultPO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.enums.AuthTypeEnum;
import com.fisk.dataservice.enums.JsonTypeEnum;
import com.fisk.dataservice.enums.RequestTypeEnum;
import com.fisk.dataservice.handler.restapi.RestApiHandler;
import com.fisk.dataservice.service.ITableApiAuthRequestService;
import com.fisk.dataservice.service.ITableApiResultService;
import com.fisk.dataservice.util.HttpGetWithEntity;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: wangjian
 * @Date: 2023-09-14
 * @Description:
 */
public class BasicValidation extends RestApiHandler {

    private static ITableApiAuthRequestService tableApiAuthRequestService;

    private static ITableApiResultService tableApiResultService;
    @Autowired
    public void setTableApiAuthRequestService(ITableApiAuthRequestService tableApiAuthRequestService) {
        BasicValidation.tableApiAuthRequestService = tableApiAuthRequestService;
    }

    @Autowired
    public void setTableApiResultService(ITableApiResultService tableApiResultService) {
        BasicValidation.tableApiResultService = tableApiResultService;
    }
    @Override
    public ApiResultDTO sendHttpPost(TableAppPO tableAppPO, TableApiServicePO tableApiServicePO, String body) {
        ApiResultDTO apiResultDTO = new ApiResultDTO();
        LambdaQueryWrapper<TableApiAuthRequestPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiAuthRequestPO::getAppId, tableAppPO.getId());
        List<TableApiAuthRequestPO> list = tableApiAuthRequestService.list(queryWrapper);
        ApiResultDTO authToken = getAuthToken(tableAppPO, tableApiServicePO, list);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        RequestConfig config = RequestConfig.custom().
                setConnectTimeout(35000).setConnectionRequestTimeout(35000).
                setSocketTimeout(60000).build();
        // 创建httpGet远程连接实例
        try {
            if (tableApiServicePO.getMethodType() == RequestTypeEnum.GET.getValue()) {
                HttpGetWithEntity httpGetWithEntity = new HttpGetWithEntity(tableAppPO.getAuthenticationUrl());
                httpGetWithEntity.setHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON_UTF8));
                // 设置请求头信息，鉴权
                httpGetWithEntity.setHeader("Authorization", authToken.getMsg());
                httpGetWithEntity.setConfig(config);
                if (tableApiServicePO.getJsonType() == JsonTypeEnum.ARRAY.getValue()) {
                    body = "[" + body + "]";
                }
                HttpEntity httpEntity = new StringEntity(body, ContentType.APPLICATION_JSON);
                httpGetWithEntity.setEntity(httpEntity);
                response = httpClient.execute(httpGetWithEntity);
                resultString = EntityUtils.toString(response.getEntity(), "utf-8");
            } else if (tableApiServicePO.getMethodType() == RequestTypeEnum.POST.getValue()) {
                HttpPost httpPost = new HttpPost(tableApiServicePO.getApiAddress());
                httpPost.setHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON_UTF8));
                httpPost.setHeader("Authorization", authToken.getMsg());
                httpPost.setConfig(config);
                if (tableApiServicePO.getJsonType() == JsonTypeEnum.ARRAY.getValue()){
                    body = "["+body+"]";
                }
                StringEntity entity = new StringEntity(body, "UTF-8");
                httpPost.setEntity(entity);
                response = httpClient.execute(httpPost);
                resultString = EntityUtils.toString(response.getEntity(), "utf-8");
            }
            apiResultDTO.setFlag(true);
            apiResultDTO.setMsg(resultString);
        } catch (Exception e) {
            apiResultDTO.setFlag(false);
            apiResultDTO.setMsg(e.toString());
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                apiResultDTO.setFlag(false);
                apiResultDTO.setMsg(e.toString());
                e.printStackTrace();
            }
        }
        return apiResultDTO;
    }

    private ApiResultDTO getAuthToken(TableAppPO tableAppPO, TableApiServicePO tableApiServicePO,List<TableApiAuthRequestPO> list){
        ApiResultDTO apiResultDTO = new ApiResultDTO();
        LambdaQueryWrapper<TableApiResultPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiResultPO::getAppId,tableAppPO.getId());
        List<TableApiResultPO> apiResultPOS = tableApiResultService.list(queryWrapper);
        List<String> key = new ArrayList<>();
        List<TableApiResultPO> collect = apiResultPOS.stream().filter(i -> i.getSelected() == 1).collect(Collectors.toList());
        key.add(collect.get(0).getName());
        if (collect.get(0).getPid() !=0){
            key = getKeys(key,apiResultPOS,collect.get(0).getPid());
        }
        Collections.reverse(key);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        RequestConfig config = RequestConfig.custom().
                setConnectTimeout(35000).setConnectionRequestTimeout(35000).
                setSocketTimeout(60000).build();
        // 创建httpGet远程连接实例
        try {
            JSONObject result = null;
            if (tableApiServicePO.getMethodType() == RequestTypeEnum.GET.getValue()) {
                HttpGetWithEntity httpGetWithEntity = new HttpGetWithEntity(tableAppPO.getAuthenticationUrl());
                httpGetWithEntity.setHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON_UTF8));
                httpGetWithEntity.setConfig(config);
                if (tableAppPO.getAuthType() == AuthTypeEnum.HEADER.getValue()){
                    for (TableApiAuthRequestPO tableApiAuthRequestPO : list) {
                        httpGetWithEntity.setHeader(tableApiAuthRequestPO.getParameterKey(), tableApiAuthRequestPO.getParameterValue());
                    }
                }else if (tableAppPO.getAuthType() == AuthTypeEnum.PARAMS.getValue()){
                    URIBuilder uriBuilder = new URIBuilder(httpGetWithEntity.getURI());
                    for (TableApiAuthRequestPO tableApiAuthRequestPO : list) {
                        uriBuilder.setParameter(tableApiAuthRequestPO.getParameterKey(), tableApiAuthRequestPO.getParameterValue());
                    }
                    httpGetWithEntity.setURI(uriBuilder.build());
                }
                response = httpClient.execute(httpGetWithEntity);
                resultString = EntityUtils.toString(response.getEntity(), "utf-8");
                result = JSONObject.parseObject(resultString);
            } else if (tableApiServicePO.getMethodType() == RequestTypeEnum.POST.getValue()) {
                HttpPost httpPost = new HttpPost(tableApiServicePO.getApiAddress());
                httpPost.setHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON_UTF8));
                if (tableAppPO.getAuthType() == AuthTypeEnum.HEADER.getValue()){
                    for (TableApiAuthRequestPO tableApiAuthRequestPO : list) {
                        httpPost.setHeader(tableApiAuthRequestPO.getParameterKey(), tableApiAuthRequestPO.getParameterValue());
                    }
                }else if (tableAppPO.getAuthType() == AuthTypeEnum.PARAMS.getValue()){
                    URIBuilder uriBuilder = new URIBuilder(httpPost.getURI());
                    for (TableApiAuthRequestPO tableApiAuthRequestPO : list) {
                        uriBuilder.setParameter(tableApiAuthRequestPO.getParameterKey(), tableApiAuthRequestPO.getParameterValue());
                    }
                    httpPost.setURI(uriBuilder.build());
                }
                httpPost.setConfig(config);
                response = httpClient.execute(httpPost);
                resultString = EntityUtils.toString(response.getEntity(), "utf-8");
                result = JSONObject.parseObject(resultString);
            }
            for (String s : key) {
                result = (JSONObject) result.get(s);
            }
            apiResultDTO.setFlag(true);
            apiResultDTO.setMsg(result.toString());
        } catch (Exception e) {
            apiResultDTO.setFlag(false);
            apiResultDTO.setMsg(e.toString());
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                apiResultDTO.setFlag(false);
                apiResultDTO.setMsg(e.toString());
                e.printStackTrace();
            }
        }
        return apiResultDTO;
    }

    private List<String> getKeys(List<String> key,List<TableApiResultPO> apiResultPOS,Integer pid){
        List<TableApiResultPO> collect = apiResultPOS.stream().filter(i -> i.getId() == pid).collect(Collectors.toList());
        if (collect.get(0).getPid() !=0){
            key.add(collect.get(0).getName());
            getKeys(key,apiResultPOS,collect.get(0).getPid());
        }else {
            key.add(collect.get(0).getName());
        }
        return key;
    }
}
