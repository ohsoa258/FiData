package com.fisk.dataservice.handler.ksf;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.entity.TableApiParameterPO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.service.ITableApiParameterService;
import com.fisk.dataservice.service.ITableApiService;
import com.fisk.dataservice.service.ITableAppManageService;
import com.fisk.dataservice.util.TreeBuilder;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: wangjian
 * @Date: 2023-09-13
 * @Description:
 */
@Slf4j
@Component
public abstract class KsfWebServiceHandler {

    private static ITableAppManageService tableAppService;
    private static ITableApiService tableApiService;
    private static ITableApiParameterService tableApiParameterService;
    private static UserClient userClient;

    @Autowired
    public void setTableAppService(ITableAppManageService tableAppService) {
        KsfWebServiceHandler.tableAppService = tableAppService;
    }
    @Autowired
    public void setTableApiService(ITableApiService tableApiService) {
        KsfWebServiceHandler.tableApiService = tableApiService;
    }
    @Autowired
    public void setTableApiParameterService(ITableApiParameterService tableApiParameterService) {
        KsfWebServiceHandler.tableApiParameterService = tableApiParameterService;
    }
    @Autowired
    public void setUserClient(UserClient userClient) {
        KsfWebServiceHandler.userClient = userClient;
    }

    public abstract ApiResultDTO sendApi(TableAppPO tableAppPO,long apiId);

    public abstract ApiResultDTO sendHttpPost(TableAppPO tableAppPO, TableApiServicePO tableApiServicePO, String body);
    public JSONArray resultSetToJsonArray(ResultSet rs) throws SQLException {
        JSONArray array = new JSONArray();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (rs.next()) {
            JSONObject jsonObj = new JSONObject();
            // 遍历每一列
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                //获取sql查询数据集合
                String value = rs.getString(columnName);
                jsonObj.put(columnName, value);
            }
            array.add(jsonObj);
        }
        return array;
    }
}
