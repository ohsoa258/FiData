package com.fisk.dataservice.handler.restapi.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.entity.TableApiAuthRequestPO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.enums.JsonTypeEnum;
import com.fisk.dataservice.handler.restapi.RestApiHandler;
import com.fisk.dataservice.service.ITableApiAuthRequestService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
        try {
            HttpPost httpPost = new HttpPost(tableApiServicePO.getApiAddress());
            httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
            httpPost.setHeader("Accept", "application/json;charset=utf-8");
            for (TableApiAuthRequestPO tableApiAuthRequestPO : list) {
                httpPost.setHeader(tableApiAuthRequestPO.getParameterKey(), tableApiAuthRequestPO.getParameterValue());
            }
            RequestConfig config = RequestConfig.custom().
                    setConnectTimeout(35000).setConnectionRequestTimeout(35000).
                    setSocketTimeout(60000).build();
            httpPost.setConfig(config);
            if (tableApiServicePO.getJsonType() == JsonTypeEnum.ARRAY.getValue()){
                body = "["+body+"]";
            }
            StringEntity entity = new StringEntity(body, "UTF-8");
            httpPost.setEntity(entity);
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
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
        apiResultDTO.setFlag(true);
        apiResultDTO.setMsg(resultString);
        return apiResultDTO;
    }
}
