package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.azure.ai.openai.implementation.NonAzureOpenAIClientImpl;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.azure.QueryData;
import com.fisk.datamanagement.enums.AzureTypeEnum;
import com.fisk.datamanagement.service.AzureService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.datamanagement.dto.gpt.Completions;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: wangjian
 * @Date: 2023-08-07
 * @Description:
 */
@Slf4j
@Service
public class AzureServiceImpl implements AzureService {
    @Value("${azure.openai.key}")
    private String AZURE_OPENAI_KEY;
    @Value("${azure.openai.endpoint}")
    private String END_POINT;
    @Value("${azure.openai.deploymodel}")
    private String DEPLOYMODEL;
    @Resource
    UserClient userClient;

    NonAzureOpenAIClientImpl nonAzureOpenAIClient;
    @Override
    public List<Map<String, Object>> getData(QueryData queryData) {
        List<Map<String, Object>> data = new ArrayList<>();
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(queryData.dbId);
        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            if (queryData.type == AzureTypeEnum.CHAT.getValue()) {
                data = getListToGpt(queryData, fiDataDataSource.data);
            } else if (queryData.type == AzureTypeEnum.SQL.getValue()) {
                data = getListToSelectSql(queryData, fiDataDataSource.data);
            }
        } else {
            log.error("userclient无法查询到目标库的连接信息");
            throw new FkException(ResultEnum.ERROR);
        }
        return data;
    }
    public List<Map<String, Object>> getListToGpt1(QueryData queryData, DataSourceDTO dataSource) {
        List<Map<String, Object>> listToSelectSql = new ArrayList<>();
        String azureOpenaiKey = AZURE_OPENAI_KEY;
        String endpoint = END_POINT;
        String deploymentOrModelId = DEPLOYMODEL;
        log.info("开始创建请求");
        List<String> prompt = new ArrayList<>();
        prompt.add("### " + dataSource.conType.getName()+" 数据库,"+queryData.getText());
        CompletionsOptions completionsOptions = new CompletionsOptions(prompt);
        completionsOptions.setTemperature((double) 0);
        completionsOptions.setMaxTokens(5000);
        completionsOptions.setTopP((double) 1);
        completionsOptions.setFrequencyPenalty((double) 0);
        completionsOptions.setPresencePenalty((double) 0);
        List<String> stop = new ArrayList<>();
        stop.add("#");
        stop.add(";");
        completionsOptions.setStop(stop);
        log.info("开始调用chatGpt请求:{}", JSONObject.toJSONString(completionsOptions));
        // 设置连接超时时间为10秒
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setSocketTimeout(10000)
                .setConnectionRequestTimeout(10000)
                .build();
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(10000).build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultSocketConfig(socketConfig)
                .build();
        CloseableHttpResponse response = null;
        try {
            // 创建Http Post请求
            String url = endpoint +"/openai/deployments/"+deploymentOrModelId+"/completions?api-version=2023-07-01-preview";
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            httpPost.setHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON_VALUE));
            httpPost.setHeader("api-key", azureOpenaiKey);
            // 模拟表单
            httpPost.setEntity(new StringEntity(BinaryData.fromObject(completionsOptions).toString(),"UTF-8"));
            // 执行http请求
            response = httpClient.execute(httpPost);
            ObjectMapper mapper = new ObjectMapper();
            String resultString = EntityUtils.toString(response.getEntity());
            Completions completions = mapper.readValue(resultString,Completions.class);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SELECT");
            for (Completions.Choice choice : completions.getChoices()) {
                String text = choice.getText();
                log.info("sql: {}",text);
                stringBuilder.append(text);
            }
//        CompletionsUsage usage = completions.getUsage();
            queryData.setText(stringBuilder.toString());
            listToSelectSql = getListToSelectSql(queryData, dataSource);
        } catch (Exception e) {
            log.error("OpenAI请求报错" + e.getMessage());
        } finally {
            try {
                httpClient.close();
                response.close();
            } catch (IOException e) {
                log.error("OpenAI请求报错" + e.getMessage());
            }
        }
        return listToSelectSql;
    }
    public List<Map<String, Object>> getListToGpt(QueryData queryData, DataSourceDTO dataSource) {
        List<Map<String, Object>> listToSelectSql = new ArrayList<>();
        String azureOpenaiKey = AZURE_OPENAI_KEY;
        String endpoint = END_POINT;
        String deploymentOrModelId = DEPLOYMODEL;
        log.info("开始创建请求");
        List<String> prompt = new ArrayList<>();
        prompt.add("### " + dataSource.conType.getName()+" 数据库,"+queryData.getText());
        CompletionsOptions completionsOptions = new CompletionsOptions(prompt);
        completionsOptions.setModel(deploymentOrModelId);
        completionsOptions.setTemperature((double) 0);
        completionsOptions.setMaxTokens(800);
        completionsOptions.setTopP((double) 1);
        completionsOptions.setFrequencyPenalty((double) 0);
        completionsOptions.setPresencePenalty((double) 0);
        List<String> stop = new ArrayList<>();
        stop.add("#");
        stop.add(";");
        completionsOptions.setStop(stop);
        log.info("开始调用chatGpt请求:{}", JSONObject.toJSONString(completionsOptions));
        // 设置连接超时时间为10秒
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setSocketTimeout(10000)
                .setConnectionRequestTimeout(10000)
                .build();
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(10000).build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultSocketConfig(socketConfig)
                .build();
        CloseableHttpResponse response = null;
        try {
            // 创建Http Post请求
            String url = endpoint;
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            httpPost.setHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON_VALUE));
            httpPost.setHeader("api-key", azureOpenaiKey);
            // 模拟表单
            httpPost.setEntity(new StringEntity(BinaryData.fromObject(completionsOptions).toString(),"UTF-8"));
            // 执行http请求
            response = httpClient.execute(httpPost);
            ObjectMapper mapper = new ObjectMapper();
            String resultString = EntityUtils.toString(response.getEntity());
            Completions completions = mapper.readValue(resultString,Completions.class);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SELECT");
            for (Completions.Choice choice : completions.getChoices()) {
                String text = choice.getText();
                log.info("sql: {}",text);
                stringBuilder.append(text);
            }
//        CompletionsUsage usage = completions.getUsage();
            queryData.setText(stringBuilder.toString());
            listToSelectSql = getListToSelectSql(queryData, dataSource);
        } catch (Exception e) {
            log.error("OpenAI请求报错" + e.getMessage());
        } finally {
            try {
                httpClient.close();
                response.close();
            } catch (IOException e) {
                log.error("OpenAI请求报错" + e.getMessage());
            }
        }
        return listToSelectSql;
    }

    public List<Map<String, Object>> getListToSelectSql(QueryData queryData, DataSourceDTO dataSource) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();//声明Map
        data.put("selectSql",queryData.getText());

        Connection conn = null;
        Statement st = null;
        try {
            Class.forName(dataSource.conType.getDriverName());
            conn = DriverManager.getConnection(dataSource.conStr, dataSource.conAccount, dataSource.conPassword);
            st = conn.createStatement();
            //无需判断ddl语句执行结果,因为如果执行失败会进catch
            log.info("开始执行脚本:{}", queryData.getText());
            ResultSet resultSet = st.executeQuery(queryData.getText());
            List<Object> list = new ArrayList<>();
            ResultSetMetaData md = resultSet.getMetaData();//获取键名
            int columnCount = md.getColumnCount();//获取列的数量
            while (resultSet.next()) {
                Map<String, Object> rowData = new HashMap<>();//声明Map
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), resultSet.getObject(i));//获取键名及值
                }
                list.add(rowData);
            }
            data.put("selectData",list);
            dataList.add(data);
        } catch (Exception e) {
            log.error(e.getMessage());
            data.put("selectData",null);
            dataList.add(data);
            return dataList;
        } finally {
            try {
                st.close();
                conn.close();
            } catch (SQLException e) {
                log.error(e.getMessage());
                throw new FkException(ResultEnum.ERROR);
            }
        }
        return dataList;
    }
}
