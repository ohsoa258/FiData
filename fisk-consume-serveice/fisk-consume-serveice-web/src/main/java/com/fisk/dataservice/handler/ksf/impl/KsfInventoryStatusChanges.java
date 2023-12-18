package com.fisk.dataservice.handler.ksf.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.entity.KsfPlantPO;
import com.fisk.dataservice.entity.TableApiParameterPO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.handler.ksf.KsfWebServiceHandler;
import com.fisk.dataservice.service.ITableApiService;
import com.fisk.dataservice.service.KsfPlantService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: wangjian
 * @Date: 2023-09-14
 * @Description:
 */
@Slf4j
@Component
public class KsfInventoryStatusChanges extends KsfWebServiceHandler {

    private static ITableApiService tableApiService;
    private static UserClient userClient;
    private static RedisUtil redisUtil;
    private static KsfPlantService ksfPlantService;

    @Autowired
    public void setTableApiService(ITableApiService tableApiService) {
        KsfInventoryStatusChanges.tableApiService = tableApiService;
    }

    @Autowired
    public void setUserClient(UserClient userClient) {
        KsfInventoryStatusChanges.userClient = userClient;
    }

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        KsfInventoryStatusChanges.redisUtil = redisUtil;
    }

    @Autowired
    public void setKsfPlantService(KsfPlantService ksfPlantService) {
        KsfInventoryStatusChanges.ksfPlantService = ksfPlantService;
    }


    @Override
    public ApiResultDTO sendApi(TableAppPO tableAppPO, long apiId, String fidata_batch_code, String sourcesys) {


        redisUtil.expire(RedisKeyEnum.TABLE_KSF_WEB_SERVER_SYNC.getName() + apiId, 100);
        ApiResultDTO apiResultDTO = new ApiResultDTO();
        TableApiServicePO tableApiServicePO = tableApiService.getById(apiId);
        KsfPlantPO plantPO = ksfPlantService.getById(tableApiServicePO.getPlantId());
        int number = 0;
        if (tableApiServicePO == null) {
            apiResultDTO.setFlag(false);
            apiResultDTO.setMsg("{\"error\":\"数据分发Api不存在apiId:" + apiId + "\"}");
            apiResultDTO.setNumber(number);
            apiResultDTO.setSyncTime(tableApiServicePO.getSyncTime());
            return apiResultDTO;
        }
        //获取查询时间区间
        String startTime = tableApiServicePO.getSyncTime();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        String endTime = now.format(formatter);

        LambdaQueryWrapper<TableApiParameterPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiParameterPO::getApiId, apiId);
        Map<String, Object> result = new HashMap<>();

        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(tableApiServicePO.getSourceDbId());
        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            DataSourceDTO dataSource = fiDataDataSource.data;
            Connection conn = null;
            Statement st = null;
            Connection conn1 = null;
            Statement st1 = null;
            try {
                Class.forName(dataSource.conType.getDriverName());
                conn = DriverManager.getConnection(dataSource.conStr, dataSource.conAccount, dataSource.conPassword);
                st = conn.createStatement();
                conn1 = DriverManager.getConnection(dataSource.conStr, dataSource.conAccount, dataSource.conPassword);
                st1 = conn.createStatement();
                //无需判断ddl语句执行结果,因为如果执行失败会进catch
                String[] split = tableApiServicePO.getSqlScript().split(";");
                String systemDataSql = null;
                String statusChangesSql = null;
                if (plantPO == null) {
                    systemDataSql = split[0] + " WHERE TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP AND sourcesys = '" + sourcesys + "' ORDER BY fi_createtime;";
                    statusChangesSql = split[1] + " WHERE fidata_batch_code in (select fidata_batch_code from public.ods_sap_ksf_inventory_sys  where TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP  AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP AND sourcesys = '" + sourcesys + "');";
                } else {
                    systemDataSql = split[0] + " where TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP AND sourcesys = '" + sourcesys + "' ORDER BY fi_createtime";
                    systemDataSql = "select q.* FROM (" + systemDataSql + ") as q LEFT JOIN public.ods_sap_ksf_inventory b on q.fidata_batch_code = b.fidata_batch_code where b.lgpla = '" + plantPO.getLgpla() + "'";
                    String selectSql = "select q.fidata_batch_code FROM (" + systemDataSql + ") as q LEFT JOIN public.ods_sap_ksf_inventory b on q.fidata_batch_code = b.fidata_batch_code where b.lgpla = '" + plantPO.getLgpla() + "'";

                    statusChangesSql = split[1] + " WHERE lgpla = '" + plantPO.getLgpla() + "' AND fidata_batch_code in (" + selectSql + ");";
                }
                log.info("开始执行脚本systemData:{}", systemDataSql);
                ResultSet systemData = st.executeQuery(systemDataSql);
                log.info("开始执行脚本statusChanges:{}", statusChangesSql);
                ResultSet statusChanges = st1.executeQuery(statusChangesSql);
                List<Map<String, Object>> resultJsonData = assembleInventoryStatusChanges(systemData, statusChanges, plantPO.getLgpla());
                List<Map<String, Object>> inventoryData = new ArrayList<>();
                for (Map<String, Object> resultJsonDatum : resultJsonData) {
                    result = resultJsonDatum;
                    Map<String, Object> data = (Map<String, Object>) resultJsonDatum.get("Data");
                    if (data != null) {
                        List<Map<String, Object>> inventoryStatusChanges = (List<Map<String, Object>>) data.get("MATDOCTAB");
                        inventoryData.addAll(inventoryStatusChanges);
                    }
                }
                Map<String, Object> data = new HashMap<>();
                data.put("MATDOCTAB", inventoryData);
                data.put("DocCount", inventoryData.size());
                result.put("Data", data);

                number = inventoryData.size();
                apiResultDTO.setNumber(number);
                apiResultDTO.setSyncTime(tableApiServicePO.getSyncTime());
            } catch (Exception e) {
                apiResultDTO.setFlag(false);
                apiResultDTO.setMsg("{\"error\":\"" + e.getMessage() + "\"}");
                apiResultDTO.setNumber(number);
                apiResultDTO.setSyncTime(tableApiServicePO.getSyncTime());
            } finally {
                try {
                    assert st != null;
                    st.close();
                    conn.close();
                    assert st1 != null;
                    st1.close();
                    conn1.close();
                } catch (SQLException e) {
                    apiResultDTO.setFlag(false);
                    apiResultDTO.setMsg("{\"error\":\"" + e.getMessage() + "\"}");
                    apiResultDTO.setNumber(number);
                    apiResultDTO.setSyncTime(tableApiServicePO.getSyncTime());
                }
            }
        } else {
            apiResultDTO.setFlag(false);
            apiResultDTO.setMsg("{\"error\":\"userclient无法查询到目标库的连接信息\"}");
            apiResultDTO.setNumber(number);
            apiResultDTO.setSyncTime(tableApiServicePO.getSyncTime());
            return apiResultDTO;
        }
        String data = JSON.toJSONString(result);
        log.info("apiId" + tableApiServicePO.getId() + "通知单推送数据:" + data);
        Map<String, Object> dataMap = (Map<String, Object>) result.get("Data");
        int docCount = (int) dataMap.get("DocCount");
        if (docCount != 0) {
            apiResultDTO = sendHttpPost(tableApiServicePO, data);
        } else {
            apiResultDTO.setFlag(true);
            apiResultDTO.setNumber(0);
            apiResultDTO.setMsg("本次同步数量为0，无需同步。");
        }
        if (apiResultDTO.getFlag()) {
            tableApiServicePO.setSyncTime(endTime);
            tableApiService.updateById(tableApiServicePO);
        }
        apiResultDTO.setSyncTime(tableApiServicePO.getSyncTime());
        apiResultDTO.setNumber(number);
        return apiResultDTO;
    }


    public List<Map<String, Object>> assembleInventoryStatusChanges(ResultSet resultSet1, ResultSet resultSet2, String wmsId) throws SQLException {
        Map<String, Map<String, Object>> inventoryData = new HashMap<>();
        // 遍历第一个结果集，将父表数据组装成 Map 对象，并保存到 inventoryData 中
        int column1Count = resultSet1.getMetaData().getColumnCount();
        while (resultSet1.next()) {
            String batchCode = resultSet1.getString("fidata_batch_code");

            Map<String, Object> map = inventoryData.get(batchCode);
            if (map == null) {
                map = new HashMap<>();
                for (int i = 1; i <= column1Count; i++) {
                    Object value = resultSet1.getObject(i);
                    String columnName = resultSet1.getMetaData().getColumnName(i);
                    if (!columnName.equals("fidata_batch_code")) {
                        if ("targetsys".equals(columnName)){
                            map.put("TargetSys", value);
                        }else if ("sourcesys".equals(columnName)){
                            map.put("SourceSys", value);
                        }
                    }
                }
                map.put("PushSeqNo", (int) System.currentTimeMillis());
                map.put("WMSID", wmsId);
                Map<String, Object> Data = new HashMap<>();
                map.put("Data", Data);
                inventoryData.put(batchCode, map);
            }
        }

        int column2Count = resultSet2.getMetaData().getColumnCount();
        // 遍历第二个结果集，将子表数据组装到对应的父表对象中
        while (resultSet2.next()) {
            String batchCode = resultSet2.getString("fidata_batch_code");

            Map<String, Object> map = inventoryData.get(batchCode);
            if (map != null) {
                Map<String, Object> data = (Map<String, Object>) map.get("Data");
                List<Map<String, Object>> inventoryStatusChanges = (List<Map<String, Object>>) data.get("MATDOCTAB");
                if (inventoryStatusChanges == null) {
                    data.put("MATDOCTAB", new ArrayList<Map<String, Object>>());
                }

                Map<String, Object> inventoryStatusChange = new HashMap<>();
                for (int i = 1; i <= column2Count; i++) {
                    Object value = resultSet2.getObject(i);
                    String columnName = resultSet2.getMetaData().getColumnName(i);
                    if (!columnName.equals("fidata_batch_code")) {
                        inventoryStatusChange.put(columnName.toUpperCase(), value);
                    }
                }
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("MATDOCTAB");
                // 设置其他字段的值
                list.add(inventoryStatusChange);
                data.put("MATDOCTAB", list);
            }
        }

        for (Map<String, Object> map : inventoryData.values()) {
            Object data = map.get("Data");
            if (data != null) {
                Map<String, Object> data1 = (Map<String, Object>) map.get("Data");
                List<Map<String, Object>> list = (List<Map<String, Object>>) data1.get("MATDOCTAB");
                if (!CollectionUtils.isEmpty(list)) {
                    data1.put("DocCount", list.size());
                    map.put("Data", data1);
                } else {
                    data1.put("DocCount", 0);
                    List<Map<String, Object>> inventoryStatusChanges = new ArrayList<>();
                    data1.put("MATDOCTAB", inventoryStatusChanges);
                    map.put("Data", data1);
                }
            }
        }
        resultSet1.close();
        resultSet2.close();
        return new ArrayList<>(inventoryData.values());
    }
}
