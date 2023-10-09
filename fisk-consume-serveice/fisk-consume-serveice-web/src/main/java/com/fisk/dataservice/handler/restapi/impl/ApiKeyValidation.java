package com.fisk.dataservice.handler.restapi.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.entity.TableApiAuthRequestPO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.enums.AuthTypeEnum;
import com.fisk.dataservice.enums.JsonTypeEnum;
import com.fisk.dataservice.enums.RequestTypeEnum;
import com.fisk.dataservice.handler.restapi.RestApiHandler;
import com.fisk.dataservice.service.ITableApiAuthRequestService;
import com.fisk.dataservice.util.HttpGetWithEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-09-13
 * @Description:
 */
@Slf4j
@Component
public class ApiKeyValidation extends RestApiHandler {
    private static ITableApiAuthRequestService tableApiAuthRequestService;
    @Autowired
    public void setTableApiAuthRequestService(ITableApiAuthRequestService tableApiAuthRequestService) {
        ApiKeyValidation.tableApiAuthRequestService = tableApiAuthRequestService;
    }

    @Override
    public ApiResultDTO sendHttpPost(TableAppPO tableAppPO,TableApiServicePO tableApiServicePO,String body){
        ApiResultDTO apiResultDTO = new ApiResultDTO();
        LambdaQueryWrapper<TableApiAuthRequestPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiAuthRequestPO::getAppId,tableAppPO.getId());
        List<TableApiAuthRequestPO> list = tableApiAuthRequestService.list(queryWrapper);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        RequestConfig config = RequestConfig.custom().
                setConnectTimeout(10000).setConnectionRequestTimeout(10000).
                setSocketTimeout(20000).build();
        try {
            if (tableApiServicePO.getMethodType() == RequestTypeEnum.GET.getValue()) {
                HttpGetWithEntity httpGetWithEntity = new HttpGetWithEntity(tableApiServicePO.getApiAddress());
                httpGetWithEntity.setHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON_UTF8));
                // 设置请求头信息，鉴权
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
                httpGetWithEntity.setConfig(config);
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
            if (response != null) {
                try {
                    response.close();

                } catch (IOException e) {
                    apiResultDTO.setFlag(false);
                    apiResultDTO.setMsg(e.toString());
                    e.printStackTrace();
                }
            }
        }
        return apiResultDTO;
    }
}
