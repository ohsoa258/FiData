package com.fisk.datamanagement.service.impl;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.core.credential.AzureKeyCredential;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.azure.QueryData;
import com.fisk.datamanagement.enums.AzureTypeEnum;
import com.fisk.datamanagement.service.AzureService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    @Override
    public List<Map<String, Object>> getData(QueryData queryData) {

        if (!StringUtils.isEmpty(queryData.getQualifiedName())) {
            if (queryData.getQualifiedName().contains("_")) {
                String[] s = queryData.getQualifiedName().split("_");
                queryData.setQualifiedName(s[0]);
            }
        }
        List<Map<String, Object>> data = new ArrayList<>();
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getByIpAndDbName(queryData.qualifiedName, queryData.getDbName());
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

    public List<Map<String, Object>> getListToGpt(QueryData queryData, DataSourceDTO dataSource) {
        String azureOpenaiKey = AZURE_OPENAI_KEY;
        String endpoint = END_POINT;
        String deploymentOrModelId = DEPLOYMODEL;

        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildClient();

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
        Completions completions = client.getCompletions(deploymentOrModelId, completionsOptions);

        log.info("Model ID={} is created at {}.{}", completions.getId(),completions.getId(), completions.getCreatedAt());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        for (Choice choice : completions.getChoices()) {
            String text = choice.getText();
            log.info("索引:{}, 文字: {}.{}", choice.getIndex(),choice.getIndex(), text);
            stringBuilder.append(text);
        }
//        CompletionsUsage usage = completions.getUsage();
        queryData.setText(stringBuilder.toString());
        List<Map<String, Object>> listToSelectSql = getListToSelectSql(queryData, dataSource);
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
            throw new FkException(ResultEnum.ERROR);
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
