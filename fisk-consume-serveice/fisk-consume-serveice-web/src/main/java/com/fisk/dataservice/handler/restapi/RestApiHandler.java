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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.sql.*;
import java.util.Base64;
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
        int number = 0;
        if (tableApiServicePO == null){
            apiResultDTO.setFlag(false);
            apiResultDTO.setMsg("{\"error\":\"数据分发Api不存在apiId:"+apiId+"\"}");
            apiResultDTO.setNumber(number);
            return apiResultDTO;
        }
        LambdaQueryWrapper<TableApiParameterPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiParameterPO::getApiId, apiId);
        List<TableApiParameterPO> apiParameterPOList = tableApiParameterService.list(queryWrapper);
        Boolean encryptFlag = false;
        String encryptKey = null;
        List<TableApiParameterPO> parameterEncryptKey = apiParameterPOList.stream().filter(i -> i.getEncryptKey() == 1).collect(Collectors.toList());
        List<String> parameterName = apiParameterPOList.stream().filter(i -> i.getEncrypt() == 1).map(TableApiParameterPO::getParameterName).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(parameterName)){
            encryptFlag = true;
            encryptKey = parameterEncryptKey.get(0).getParameterValue();
        }

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
                resultJsonData = resultSetToJsonArray(resultSet,encryptFlag,parameterName,encryptKey);
                number = resultJsonData.size();
                apiResultDTO.setNumber(number);
            } catch (Exception e) {
                apiResultDTO.setFlag(false);
                apiResultDTO.setMsg("{\"error\":\"" + e.getMessage()+"\"}");
                apiResultDTO.setNumber(number);
            } finally {
                try {
                    assert st != null;
                    st.close();
                    conn.close();
                } catch (SQLException e) {
                    apiResultDTO.setFlag(false);
                    apiResultDTO.setMsg("{\"error\":\"" + e.getMessage()+"\"}");
                    apiResultDTO.setNumber(number);
                }
            }
        } else {
            apiResultDTO.setFlag(false);
            apiResultDTO.setMsg("{\"error\":\"userclient无法查询到目标库的连接信息\"}");
            apiResultDTO.setNumber(number);
            return apiResultDTO;
        }
        JSONArray finalResultJsonData = resultJsonData;
        List<TableApiParameterPO> collect = apiParameterPOList.stream().filter(i -> i.getSelected() == 1).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)){
            apiResultDTO = sendHttpPost(tableAppPO,tableApiServicePO,finalResultJsonData.toJSONString(),false);
            apiResultDTO.setNumber(number);
        }else {
            Integer id = null;
            for (TableApiParameterPO tableApiParameterPO : apiParameterPOList) {
                if (tableApiParameterPO.getSelected() == 1){
                    id = (int)tableApiParameterPO.getId();
                }
            }
            Integer finalId = id;
            apiParameterPOList = apiParameterPOList.stream().filter(i->i.getPid() != finalId).collect(Collectors.toList());

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
            log.info("发送参数2:"+json.toJSONString());
            apiResultDTO = sendHttpPost(tableAppPO,tableApiServicePO,json.toJSONString(),true);
            apiResultDTO.setNumber(number);
        }
        return apiResultDTO;
    }

    public abstract ApiResultDTO sendHttpPost(TableAppPO tableAppPO, TableApiServicePO tableApiServicePO, String body,Boolean flag);
    public JSONArray resultSetToJsonArray(ResultSet rs,Boolean flag,List<String> parameterName,String encryptKey) throws Exception {
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
                if (flag){
                    if (parameterName.contains(columnName)){
                        if (!StringUtils.isEmpty(value)){
                            value = encryptField(value,encryptKey);
                        }
                    }
                }
                jsonObj.put(columnName, value);
            }
            array.add(jsonObj);
        }
        return array;
    }

    private static String encryptField(String fieldValue, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedBytes = cipher.doFinal(fieldValue.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
}
