package com.fisk.dataservice.handler.restapi.impl;

import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.enums.JsonTypeEnum;
import com.fisk.dataservice.handler.restapi.RestApiHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @Author: wangjian
 * @Date: 2023-09-14
 * @Description:
 */
@Slf4j
public class NoneValidation extends RestApiHandler {
    @Override
    public ApiResultDTO sendHttpPost(TableAppPO tableAppPO, TableApiServicePO tableApiServicePO, String body, Boolean flag) {
        ApiResultDTO apiResultDTO = new ApiResultDTO();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            HttpPost httpPost = new HttpPost(tableApiServicePO.getApiAddress());
            httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
            httpPost.setHeader("Accept", "application/json;charset=utf-8");
            RequestConfig config = RequestConfig.custom().
                    setConnectTimeout(10000).setConnectionRequestTimeout(10000).
                    setSocketTimeout(20000).build();
            httpPost.setConfig(config);
            if (tableApiServicePO.getJsonType() == JsonTypeEnum.ARRAY.getValue()){
                if (flag){
                    body = "[" + body + "]";
                }
            }
            StringEntity entity = new StringEntity(body, "UTF-8");
            httpPost.setEntity(entity);
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            apiResultDTO.setFlag(false);
            apiResultDTO.setMsg(e.getMessage());
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
        apiResultDTO.setFlag(true);
        apiResultDTO.setMsg(resultString);
        return apiResultDTO;
    }
}
