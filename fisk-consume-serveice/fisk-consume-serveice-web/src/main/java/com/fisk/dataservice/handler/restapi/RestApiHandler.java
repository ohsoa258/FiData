package com.fisk.dataservice.handler.restapi;

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
public abstract class RestApiHandler {
    private static ITableAppManageService tableAppService;
    private static ITableApiService tableApiService;
    private static ITableApiParameterService tableApiParameterService;
    private static UserClient userClient;

    @Autowired
    public void setTableAppService(ITableAppManageService tableAppService) {
        RestApiHandler.tableAppService = tableAppService;
    }
    @Autowired
    public void setTableApiService(ITableApiService tableApiService) {
        RestApiHandler.tableApiService = tableApiService;
    }
    @Autowired
    public void setTableApiParameterService(ITableApiParameterService tableApiParameterService) {
        RestApiHandler.tableApiParameterService = tableApiParameterService;
    }
    @Autowired
    public void setUserClient(UserClient userClient) {
        RestApiHandler.userClient = userClient;
    }

    public ApiResultDTO sendApi(long apiId) {
        ApiResultDTO apiResultDTO = new ApiResultDTO();
        TableApiServicePO tableApiServicePO = tableApiService.getById(apiId);
        if (tableApiServicePO == null){
            apiResultDTO.setFlag(false);
            apiResultDTO.setMsg("{\"error\":\"数据分发Api不存在apiId:"+apiId+"\"}");
            return apiResultDTO;
        }
        LambdaQueryWrapper<TableApiParameterPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiParameterPO::getApiId, apiId);
        List<TableApiParameterPO> apiParameterPOList = tableApiParameterService.list(queryWrapper);

        TableAppPO tableAppPO = tableAppService.getById(tableApiServicePO.getAppId());
        JSONArray resultJsonData = null;
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(tableApiServicePO.getSourceDbId());
        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            DataSourceDTO dataSource = fiDataDataSource.data;
            Connection conn = null;
            Statement st = null;
            try {
                Class.forName(dataSource.conType.getDriverName());
                conn = DriverManager.getConnection(dataSource.conStr, dataSource.conAccount, dataSource.conPassword);
                st = conn.createStatement();
                //无需判断ddl语句执行结果,因为如果执行失败会进catch
                log.info("开始执行脚本:{}", tableApiServicePO.getSqlScript());
                ResultSet resultSet = st.executeQuery(tableApiServicePO.getSqlScript());
                resultJsonData = resultSetToJsonArray(resultSet);
            } catch (Exception e) {
                apiResultDTO.setFlag(false);
                apiResultDTO.setMsg("{\"error\":\"" + e.getMessage()+"\"}");
            } finally {
                try {
                    assert st != null;
                    st.close();
                    conn.close();
                } catch (SQLException e) {
                    apiResultDTO.setFlag(false);
                    apiResultDTO.setMsg("{\"error\":\"" + e.getMessage()+"\"}");
                }
            }
        } else {
            apiResultDTO.setFlag(false);
            apiResultDTO.setMsg("{\"error\":\"userclient无法查询到目标库的连接信息\"}");
            return apiResultDTO;
        }
        JSONArray finalResultJsonData = resultJsonData;
        List<TreeBuilder.Node> nodes = apiParameterPOList.stream().map(i -> {
            TreeBuilder.Node node = new TreeBuilder.Node();
            node.setId(String.valueOf(i.getId()));
            node.setPid(String.valueOf(i.getPid()));
            JSONObject json = new JSONObject();
            if (i.getSelected() == 1){
                json.put(i.getParameterName(), finalResultJsonData);
            }else {
                json.put(i.getParameterName(), i.getParameterValue());
            }
            node.setParameter(json);
            return node;
        }).collect(Collectors.toList());
        JSONObject json = new TreeBuilder().buildTree(nodes);
        apiResultDTO = sendHttpPost(tableAppPO,tableApiServicePO,json.toJSONString());
        return apiResultDTO;
    }

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
