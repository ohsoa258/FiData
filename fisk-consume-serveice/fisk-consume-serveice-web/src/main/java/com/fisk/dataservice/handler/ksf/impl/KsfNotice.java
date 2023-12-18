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

import static java.util.stream.Collectors.groupingBy;

/**
 * @Author: wangjian
 * @Date: 2023-09-14
 * @Description:
 */
@Slf4j
@Component
public class KsfNotice extends KsfWebServiceHandler {

    private static ITableApiService tableApiService;
    private static UserClient userClient;
    private static RedisUtil redisUtil;
    private static KsfPlantService ksfPlantService;

    @Autowired
    public void setTableApiService(ITableApiService tableApiService) {
        KsfNotice.tableApiService = tableApiService;
    }

    @Autowired
    public void setUserClient(UserClient userClient) {
        KsfNotice.userClient = userClient;
    }

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        KsfNotice.redisUtil = redisUtil;
    }

    @Autowired
    public void setKsfPlantService(KsfPlantService ksfPlantService) {
        KsfNotice.ksfPlantService = ksfPlantService;
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
            Connection conn1 = null;
            Statement st1 = null;
            Connection conn2 = null;
            Statement st2 = null;
            Connection conn3 = null;
            Statement st3 = null;
            try {
                Class.forName(dataSource.conType.getDriverName());
                conn1 = DriverManager.getConnection(dataSource.conStr, dataSource.conAccount, dataSource.conPassword);
                st1 = conn1.createStatement();
                conn2 = DriverManager.getConnection(dataSource.conStr, dataSource.conAccount, dataSource.conPassword);
                st2 = conn2.createStatement();
                conn3 = DriverManager.getConnection(dataSource.conStr, dataSource.conAccount, dataSource.conPassword);
                st3 = conn3.createStatement();
                //无需判断ddl语句执行结果,因为如果执行失败会进catch
                String[] split = tableApiServicePO.getSqlScript().split(";");
                String systemDataSql = null;
                String noticeDataSql = null;
                String noticeDetailSql = null;
                if (plantPO == null) {
                    systemDataSql = split[0] + " where TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP AND sourcesys = '" + sourcesys + "' ORDER BY fi_createtime;";
                    noticeDataSql = split[1] + " WHERE fidata_batch_code in  (select fidata_batch_code from public.ods_sap_ksf_notice  where TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP AND sourcesys = '" + sourcesys + "');";
                    noticeDetailSql = split[2] + " WHERE fidata_batch_code in (select fidata_batch_code from public.ods_sap_ksf_notice where TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP AND sourcesys = '" + sourcesys + "');";
                } else {
                    systemDataSql = split[0] + " where TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP AND sourcesys = '" + sourcesys + "' ORDER BY fi_createtime";
                    systemDataSql = "select q.* FROM (" + systemDataSql + ") as q LEFT JOIN public.ods_sap_headers b on q.fidata_batch_code = b.fidata_batch_code where b.lgpla = '" + plantPO.getLgpla() + "'";
                    String selectSql = "select q.fidata_batch_code FROM (" + systemDataSql + ") as q LEFT JOIN public.ods_sap_headers b on q.fidata_batch_code = b.fidata_batch_code where b.lgpla = '" + plantPO.getLgpla() + "'";

                    noticeDataSql = split[1] + " WHERE lgpla ='" + plantPO.getLgpla() + "' AND fidata_batch_code in (" + selectSql + ");";
                    noticeDetailSql = split[2] + " WHERE fidata_batch_code in (" + selectSql + ");";
                }
                log.info("开始执行脚本systemData:{}", systemDataSql);
                ResultSet systemData = st1.executeQuery(systemDataSql);
                log.info("开始执行脚本noticeData:{}", noticeDataSql);
                ResultSet noticeData = st2.executeQuery(noticeDataSql);
                log.info("开始执行脚本noticeDetail:{}", noticeDetailSql);
                ResultSet noticeDetail = st3.executeQuery(noticeDetailSql);
                List<Map<String, Object>> resultJsonData = assembleNotice(systemData, noticeData, noticeDetail, plantPO.getLgpla());

                List<Map<String, Object>> noticesData = new ArrayList<>();
                for (Map<String, Object> resultJsonDatum : resultJsonData) {
                    result = resultJsonDatum;
                    Map<String, Object> data = (Map<String, Object>) resultJsonDatum.get("Data");
                    if (data != null) {
                        List<Map<String, Object>> ksfNotices = (List<Map<String, Object>>) data.get("Ksf_Notice");
                        noticesData.addAll(ksfNotices);
                    }
                }
                Map<String, Object> data = new HashMap<>();
                data.put("Ksf_Notice", noticesData);
                data.put("DocCount", noticesData.size());
                result.put("Data", data);

                number = noticesData.size();
                apiResultDTO.setNumber(number);
                apiResultDTO.setSyncTime(tableApiServicePO.getSyncTime());
            } catch (Exception e) {
                apiResultDTO.setFlag(false);
                apiResultDTO.setMsg("{\"error\":\"" + e.getMessage() + "\"}");
                apiResultDTO.setNumber(number);
                apiResultDTO.setSyncTime(tableApiServicePO.getSyncTime());
            } finally {
                try {
                    assert st1 != null;
                    st1.close();
                    conn1.close();
                    assert st2 != null;
                    st2.close();
                    conn2.close();
                    assert st3 != null;
                    st3.close();
                    conn3.close();
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
        log.info("docCount:"+docCount);
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

    public List<Map<String, Object>> assembleNotice(ResultSet resultSet1, ResultSet resultSet2, ResultSet resultSet3, String wmsId) throws SQLException {
        Map<String, Map<String, Object>> noticeData = new HashMap<>();
        // 遍历第一个结果集，将父表数据组装成 Map 对象，并保存到 noticeData 中
        int column1Count = resultSet1.getMetaData().getColumnCount();
        while (resultSet1.next()) {
            String batchCode = resultSet1.getString("fidata_batch_code");

            Map<String, Object> map = noticeData.get(batchCode);
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
                noticeData.put(batchCode, map);
            }
        }

        Map<String, Map<String, List<Map<String, Object>>>> detailMap = new HashMap<>();
        // 遍历第三个结果集，将子表数据组装到对应的父表对象中

        int column3Count = resultSet3.getMetaData().getColumnCount();
        Map<String, List<Map<String, Object>>> noticeMap = new HashMap<>();
        while (resultSet3.next()) {
            String batchCode = resultSet3.getString("fidata_batch_code");
            List<Map<String, Object>> details = noticeMap.get(batchCode);

            Map<String, Object> noticeDetail = new HashMap<>();
            for (int i = 1; i < column3Count; i++) {
                Object value = resultSet3.getObject(i);
                String columnName = resultSet3.getMetaData().getColumnName(i);
                if (!columnName.equals("fidata_batch_code")) {
                    noticeDetail.put(columnName.toUpperCase(), value);
                }
            }
            if (details == null) {
                details = new ArrayList<>();
                details.add(noticeDetail);
                noticeMap.put(batchCode, details);
            } else {
                details.add(noticeDetail);
                noticeMap.put(batchCode, details);
            }
        }

        for (Map.Entry<String, List<Map<String, Object>>> stringListEntry : noticeMap.entrySet()) {
            Map<String, List<Map<String, Object>>> ebelnToDetails = stringListEntry.getValue().stream().collect(groupingBy(i -> (String) i.get("EBELN")));
            detailMap.put(stringListEntry.getKey(), ebelnToDetails);
        }

        int column2Count = resultSet2.getMetaData().getColumnCount();
        // 遍历第二个结果集，将子表数据组装到对应的父表对象中
        while (resultSet2.next()) {
            String batchCode = resultSet2.getString("fidata_batch_code");
            Map<String, Object> notice = noticeData.get(batchCode);
            ;
            if (notice != null) {
                Map<String, Object> data = (Map<String, Object>) notice.get("Data");
                List<Map<String, Object>> KsfNotices = (List<Map<String, Object>>) data.get("Ksf_Notice");
                if (KsfNotices == null) {
                    data.put("Ksf_Notice", new ArrayList<Map<String, Object>>());
                }
                String ebeln = null;
                Map<String, Object> ksfNotice = new HashMap<>();
                for (int i = 1; i <= column2Count; i++) {
                    Object value = resultSet2.getObject(i);
                    String columnName = resultSet2.getMetaData().getColumnName(i);
                    if (columnName.equals("ebeln")) {
                        ebeln = (String) value;
                    }
                    if (!columnName.equals("fidata_batch_code")) {
                        ksfNotice.put(columnName.toUpperCase(), value);
                    }
                }
                Map<String, List<Map<String, Object>>> stringListMap = detailMap.get(batchCode);
                if (stringListMap != null) {
                    List<Map<String, Object>> noticeDetails = stringListMap.get(ebeln);
                    ksfNotice.put("DETAIL", noticeDetails);
                }
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("Ksf_Notice");
                // 设置其他字段的值
                list.add(ksfNotice);
                data.put("Ksf_Notice", list);
            }
        }

        // 设置 DocCount 属性为 MATDOCTAB 的 size
        for (Map<String, Object> map : noticeData.values()) {
            Object data = map.get("Data");
            if (data != null) {
                Map<String, Object> data1 = (Map<String, Object>) map.get("Data");
                List<Map<String, Object>> list = (List<Map<String, Object>>) data1.get("Ksf_Notice");
                if (!CollectionUtils.isEmpty(list)) {
                    data1.put("DocCount", list.size());
                    map.put("Data", data1);
                } else {
                    data1.put("DocCount", 0);
                    List<Map<String, Object>> ksfNotices = new ArrayList<>();
                    data1.put("Ksf_Notice", ksfNotices);
                    map.put("Data", data1);
                }
            }
        }
        resultSet1.close();
        resultSet2.close();
        return new ArrayList<>(noticeData.values());
    }
}
