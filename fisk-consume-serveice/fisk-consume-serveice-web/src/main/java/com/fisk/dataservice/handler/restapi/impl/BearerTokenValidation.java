package com.fisk.dataservice.handler.restapi.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.entity.TableApiAuthRequestPO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.enums.JsonTypeEnum;
import com.fisk.dataservice.enums.RequestTypeEnum;
import com.fisk.dataservice.handler.restapi.RestApiHandler;
import com.fisk.dataservice.service.ITableApiAuthRequestService;
import com.fisk.dataservice.util.HttpGetWithEntity;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-09-14
 * @Description:
 */
public class BearerTokenValidation extends RestApiHandler {
    private static ITableApiAuthRequestService tableApiAuthRequestService;

    @Autowired
    public void setTableApiAuthRequestService(ITableApiAuthRequestService tableApiAuthRequestService) {
        BearerTokenValidation.tableApiAuthRequestService = tableApiAuthRequestService;
    }

    @Override
    public ApiResultDTO sendHttpPost(TableAppPO tableAppPO, TableApiServicePO tableApiServicePO, String body) {
        ApiResultDTO apiResultDTO = new ApiResultDTO();
        LambdaQueryWrapper<TableApiAuthRequestPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiAuthRequestPO::getAppId, tableAppPO.getId());
        List<TableApiAuthRequestPO> list = tableApiAuthRequestService.list(queryWrapper);
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
                httpGetWithEntity.setHeader("Authorization", list.get(0).getParameterValue());
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
                httpPost.setHeader("Authorization", list.get(0).getParameterValue());
                httpPost.setConfig(config);
                if (tableApiServicePO.getJsonType() == JsonTypeEnum.ARRAY.getValue()) {
                    body = "[" + body + "]";
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
            response.setStatusCode(200);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                apiResultDTO.setFlag(false);
                apiResultDTO.setMsg(e.toString());
                e.printStackTrace();
                response.setStatusCode(200);
            }
        }
        return apiResultDTO;
    }
}
