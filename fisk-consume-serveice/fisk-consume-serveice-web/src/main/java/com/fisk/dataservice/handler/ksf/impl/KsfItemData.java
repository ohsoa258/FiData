package com.fisk.dataservice.handler.ksf.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataservice.dto.ksfwebservice.item.Data;
import com.fisk.dataservice.dto.ksfwebservice.item.KsfGoods;
import com.fisk.dataservice.dto.ksfwebservice.item.ItemData;
import com.fisk.dataservice.dto.ksfwebservice.notice.NoticeData;
import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.entity.TableApiParameterPO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.enums.JsonTypeEnum;
import com.fisk.dataservice.handler.ksf.KsfWebServiceHandler;
import com.fisk.dataservice.service.ITableApiService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import java.net.URL;
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
public class KsfItemData extends KsfWebServiceHandler {


    private static ITableApiService tableApiService;
    private static UserClient userClient;
    private static RedisUtil redisUtil;

    @Autowired
    public void setTableApiService(ITableApiService tableApiService) {
        KsfItemData.tableApiService = tableApiService;
    }

    @Autowired
    public void setUserClient(UserClient userClient) {
        KsfItemData.userClient = userClient;
    }
    @Autowired
    public void setUserClient(RedisUtil redisUtil) {
        KsfItemData.redisUtil = redisUtil;
    }


    @Override
    public ApiResultDTO sendApi(TableAppPO tableAppPO, long apiId) {
        redisUtil.expire(RedisKeyEnum.TABLE_KSF_WEB_SERVER_SYNC.getName()+apiId, 100);
        ApiResultDTO apiResultDTO = new ApiResultDTO();
        TableApiServicePO tableApiServicePO = tableApiService.getById(apiId);
        int number = 0;
        if (tableApiServicePO == null) {
            apiResultDTO.setFlag(false);
            apiResultDTO.setMsg("{\"error\":\"数据分发Api不存在apiId:" + apiId + "\"}");
            apiResultDTO.setNumber(number);
            return apiResultDTO;
        }
        //获取查询时间区间
        String startTime = tableApiServicePO.getSyncTime();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        String endTime = now.format(formatter);

        LambdaQueryWrapper<TableApiParameterPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiParameterPO::getApiId, apiId);
        ItemData result = new ItemData();
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
                String systemDataSql = split[0]+" where TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP ORDER BY fi_createtime;";
                String statusChangesSql = split[1]+" WHERE fidata_batch_code in (select fidata_batch_code from ods_sap_ksf_item_sys where TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP);";
                log.info("开始执行脚本systemData:{}", systemDataSql);
                ResultSet systemData = st.executeQuery(systemDataSql);
                log.info("开始执行脚本items:{}", statusChangesSql);
                ResultSet items = st1.executeQuery(statusChangesSql);
                List<ItemData> resultJsonData = assembleInventoryStatusChangesDTO(systemData, items);

                List<KsfGoods> itemData = new ArrayList<>();
                for (ItemData resultJsonDatum : resultJsonData) {
                    result = resultJsonDatum;
                    Data data = resultJsonDatum.getData();
                    if (data != null){
                        itemData.addAll(data.getKsfGoods());
                    }
                }
                Data data = new Data();
                data.setKsfGoods(itemData);
                data.setDocCount(itemData.size());
                result.setData(data);

                number = result.getData().getDocCount();
                apiResultDTO.setNumber(number);
            } catch (Exception e) {
                apiResultDTO.setFlag(false);
                apiResultDTO.setMsg("{\"error\":\"" + e.getMessage() + "\"}");
                apiResultDTO.setNumber(number);
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
                }
            }
        } else {
            apiResultDTO.setFlag(false);
            apiResultDTO.setMsg("{\"error\":\"userclient无法查询到目标库的连接信息\"}");
            apiResultDTO.setNumber(number);
            return apiResultDTO;
        }
        String data = JSON.toJSONString(result);
        log.info("apiId"+tableApiServicePO.getId()+"通知单推送数据:"+ data);
        apiResultDTO = sendHttpPost(tableAppPO, tableApiServicePO, data);
        if (apiResultDTO.getFlag()){
            tableApiServicePO.setSyncTime(endTime);
            tableApiService.updateById(tableApiServicePO);
        }
        apiResultDTO.setNumber(number);
        return apiResultDTO;
    }

//    @Override
//    public ApiResultDTO sendHttpPost(TableAppPO tableAppPO, TableApiServicePO tableApiServicePO, String body) {
//        ApiResultDTO apiResultDTO = new ApiResultDTO();
//        //创建动态客户端
//        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
//        //webService的这个动态客户端的地址需要从数据库中查出来
//        Client client = dcf.createClient(tableApiServicePO.getApiAddress());
//        //设置超时时间
//        HTTPConduit conduit = (HTTPConduit) client.getConduit();
//        HTTPClientPolicy policy = new HTTPClientPolicy();
//        policy.setAllowChunking(false);
//        // 连接服务器超时时间 30秒
//        policy.setConnectionTimeout(30000);
//        // 等待服务器响应超时时间 30秒
//        policy.setReceiveTimeout(30000);
//        conduit.setClient(policy);
//        JSONObject result = null;
//        try {
//            // invoke("方法名",参数1,参数2,参数3....);
//            Object[] objects = client.invoke(tableApiServicePO.getMethodName(), body);
//            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(objects[0]));
//            if ((int)jsonObject.get("code") == 1){
//                apiResultDTO.setFlag(true);
//                apiResultDTO.setMsg(jsonObject.get("msg").toString());
//            }else if ((int)jsonObject.get("code") == -1){
//                apiResultDTO.setFlag(false);
//                apiResultDTO.setMsg(jsonObject.get("msg").toString());
//            }else {
//                apiResultDTO.setFlag(false);
//                apiResultDTO.setMsg("远程调用异常");
//            }
//        } catch (Exception e) {
//            apiResultDTO.setFlag(false);
//            apiResultDTO.setMsg(e.toString());
//            e.printStackTrace();
//        } finally {
//            if (client != null) {
//                try {
//                    client.close();
//                } catch (Exception e) {
//                    apiResultDTO.setFlag(false);
//                    apiResultDTO.setMsg(e.toString());
//                    e.printStackTrace();
//                }
//            }
//        }
//        return apiResultDTO;
//
//
////        String result = null;
////        try {
////            Service service = new Service();
////            Call call = (Call) service.createCall();
////
////            // 设置wsdl地址
////            call.setTargetEndpointAddress(new URL(tableApiServicePO.getApiAddress()));
////
////            // 设置命名空间和方法名
////            call.setOperationName(new QName("http://tempuri.org/", tableApiServicePO.getMethodName()));
////
////            // 设置参数类型和参数名称
////            call.addParameter("SAPPushData", org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
////
////            // 设置返回值类型
////            call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);
////
////            // 设置参数
//////            String requestData = "{ \"cdSign\":\"1\",\"beginDate\":\"2021-11-21\",\"endDate\":\"2021-11-22\"}";
////            result = (String) call.invoke(new Object[]{body});
////            log.info("库存状态变更返回值:" + result);
////            JSONObject jsonObject = JSON.parseObject(result);
////            if ((int) jsonObject.get("code") == 1) {
////                apiResultDTO.setFlag(true);
////                apiResultDTO.setMsg(jsonObject.get("msg").toString());
////            } else if ((int) jsonObject.get("code") == -1) {
////                apiResultDTO.setFlag(false);
////                apiResultDTO.setMsg(jsonObject.get("msg").toString());
////            } else {
////                apiResultDTO.setFlag(false);
////                apiResultDTO.setMsg("远程调用异常");
////            }
////        } catch (Exception e) {
////            apiResultDTO.setFlag(false);
////            apiResultDTO.setMsg(e.toString());
////            e.printStackTrace();
////        }
//
////        return apiResultDTO;
//    }


    public List<ItemData> assembleInventoryStatusChangesDTO(ResultSet resultSet1, ResultSet resultSet2) throws SQLException {
        Map<String, ItemData> dtoMap = new HashMap<>();
        // 遍历第一个结果集，将父表数据组装成 InventoryStatusChangesDTO 对象，并保存到 dtoMap 中
        while (resultSet1.next()) {
            String batchCode = resultSet1.getString("fidata_batch_code");

            ItemData dto = dtoMap.get(batchCode);
            if (dto == null) {
                dto = new ItemData();
                dto.setSourceSys(resultSet1.getString("sourcesys"));
                dto.setTargetSys(resultSet1.getString("targetsys"));
                dto.setPushSeqNo((int) System.currentTimeMillis());
                dto.setWMSID("ZTJ1");
                dto.setData(new Data());
                dtoMap.put(batchCode, dto);
            }
        }

        // 遍历第二个结果集，将子表数据组装到对应的父表对象中
        while (resultSet2.next()) {
            String batchCode = resultSet2.getString("fidata_batch_code");

            ItemData dto = dtoMap.get(batchCode);
            if (dto != null) {
                Data data = dto.getData();
                if (data.getKsfGoods() == null) {
                    data.setKsfGoods(new ArrayList<>());
                }

                KsfGoods ksfGoods = new KsfGoods();
                // 设置其他字段的值
                ksfGoods.setMTART(resultSet2.getString("mtart"));
                ksfGoods.setMTBEZ(resultSet2.getString("mtbez"));
                ksfGoods.setMATNR(resultSet2.getString("matnr"));
                ksfGoods.setMAKTX(resultSet2.getString("maktx"));
                ksfGoods.setNORMT(resultSet2.getString("normt"));
                ksfGoods.setMEINS(resultSet2.getString("meins"));
                ksfGoods.setVBAMG(resultSet2.getString("vbamg"));
                data.getKsfGoods().add(ksfGoods);
            }
        }

        // 设置 DocCount 属性为 MATDOCTAB 的 size
        for (ItemData dto : dtoMap.values()) {
            Data data = dto.getData();
            if (data != null && data.getKsfGoods() != null) {
                data.setDocCount(data.getKsfGoods().size());
            }else {
                data.setDocCount(0);
                List<KsfGoods> ksfGoods = new ArrayList<>();
                data.setKsfGoods(ksfGoods);
            }
        }
        resultSet1.close();
        resultSet2.close();
        return new ArrayList<>(dtoMap.values());
    }
}
