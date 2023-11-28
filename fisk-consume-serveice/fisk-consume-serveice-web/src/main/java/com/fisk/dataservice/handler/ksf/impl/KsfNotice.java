package com.fisk.dataservice.handler.ksf.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.ksfwebservice.item.Data;
import com.fisk.dataservice.dto.ksfwebservice.item.ItemDataDTO;
import com.fisk.dataservice.dto.ksfwebservice.item.KsfGoods;
import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.JsonTypeEnum;
import com.fisk.dataservice.handler.ksf.KsfWebServiceHandler;
import com.fisk.dataservice.service.ITableApiAuthRequestService;
import com.fisk.dataservice.service.ITableApiResultService;
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
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    public void setTableApiService(ITableApiService tableApiService) {
        KsfNotice.tableApiService = tableApiService;
    }

    @Autowired
    public void setUserClient(UserClient userClient) {
        KsfNotice.userClient = userClient;
    }


    @Override
    public ApiResultDTO sendApi(TableAppPO tableAppPO, long apiId) {
        ApiResultDTO apiResultDTO = new ApiResultDTO();
        TableApiServicePO tableApiServicePO = tableApiService.getById(apiId);
        int number = 0;
        if (tableApiServicePO == null) {
            apiResultDTO.setFlag(false);
            apiResultDTO.setMsg("{\"error\":\"数据分发Api不存在apiId:" + apiId + "\"}");
            apiResultDTO.setNumber(number);
            return apiResultDTO;
        }
        LambdaQueryWrapper<TableApiParameterPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiParameterPO::getApiId, apiId);
        List<ItemDataDTO> resultJsonData = null;
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
                log.info("开始执行脚本:{}", tableApiServicePO.getSqlScript());
                //获取查询时间区间
                String startTime = tableApiServicePO.getSyncTime();
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
                String endTime = now.format(formatter);

                String[] split = tableApiServicePO.getSqlScript().split(";");
                String systemDataSql = split[0].replace("${startTime}", startTime).replace("${endTime}", endTime);
                String statusChangesSql = split[1].replace("${startTime}", startTime).replace("${endTime}", endTime);
                ResultSet systemData = st1.executeQuery(systemDataSql);
                ResultSet items = st2.executeQuery(statusChangesSql);
                resultJsonData = assembleInventoryStatusChangesDTO(systemData, items);
                number = resultJsonData.size();
                apiResultDTO.setNumber(number);
            } catch (Exception e) {
                apiResultDTO.setFlag(false);
                apiResultDTO.setMsg("{\"error\":\"" + e.getMessage() + "\"}");
                apiResultDTO.setNumber(number);
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
                }
            }
        } else {
            apiResultDTO.setFlag(false);
            apiResultDTO.setMsg("{\"error\":\"userclient无法查询到目标库的连接信息\"}");
            apiResultDTO.setNumber(number);
            return apiResultDTO;
        }

        apiResultDTO = sendHttpPost(tableAppPO, tableApiServicePO, JSON.toJSONString(resultJsonData));
        apiResultDTO.setNumber(number);
        return apiResultDTO;
    }

    @Override
    public ApiResultDTO sendHttpPost(TableAppPO tableAppPO, TableApiServicePO tableApiServicePO, String body) {
        ApiResultDTO apiResultDTO = new ApiResultDTO();
//        //创建动态客户端
//        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
//        //webService的这个动态客户端的地址需要从数据库中查出来
//        Client client = dcf.createClient(tableAppPO.getAuthenticationUrl());
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
//            if (tableApiServicePO.getJsonType() == JsonTypeEnum.ARRAY.getValue()){
//                body = "["+body+"]";
//            }
//            Map<String,Object> map = new HashMap<>();
//            map.put("SAPPushData",JSONObject.parseObject(body));
//            // invoke("方法名",参数1,参数2,参数3....);
//            Object[] objects = client.invoke(tableApiServicePO.getMethodName(), JSONObject.toJSONString(map));
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
        String result = null;
        try {
            Service service = new Service();
            Call call = (Call) service.createCall();

            // 设置wsdl地址
            call.setTargetEndpointAddress(new URL(tableApiServicePO.getApiAddress()));

            // 设置命名空间和方法名
            call.setOperationName(new QName("http://tempuri.org/", tableApiServicePO.getMethodName()));

            // 设置参数类型和参数名称
            call.addParameter("SAPPushData", org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);

            // 设置返回值类型
            call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);

            // 设置参数
//            String requestData = "{ \"cdSign\":\"1\",\"beginDate\":\"2021-11-21\",\"endDate\":\"2021-11-22\"}";
            result = (String) call.invoke(new Object[]{body});
            log.info("库存状态变更返回值:" + result);
            JSONObject jsonObject = JSON.parseObject(result);
            if ((int) jsonObject.get("code") == 1) {
                apiResultDTO.setFlag(true);
                apiResultDTO.setMsg(jsonObject.get("msg").toString());
            } else if ((int) jsonObject.get("code") == -1) {
                apiResultDTO.setFlag(false);
                apiResultDTO.setMsg(jsonObject.get("msg").toString());
            } else {
                apiResultDTO.setFlag(false);
                apiResultDTO.setMsg("远程调用异常");
            }
        } catch (Exception e) {
            apiResultDTO.setFlag(false);
            apiResultDTO.setMsg(e.toString());
            e.printStackTrace();
        }

        return apiResultDTO;
    }
    public List<ItemDataDTO> assembleInventoryStatusChangesDTO(ResultSet resultSet1, ResultSet resultSet2) throws SQLException {
        Map<String, ItemDataDTO> dtoMap = new HashMap<>();
        // 遍历第一个结果集，将父表数据组装成 InventoryStatusChangesDTO 对象，并保存到 dtoMap 中
        while (resultSet1.next()) {
            String batchCode = resultSet1.getString("fidata_batch_code");

            ItemDataDTO dto = dtoMap.get(batchCode);
            if (dto == null) {
                dto = new ItemDataDTO();
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

            ItemDataDTO dto = dtoMap.get(batchCode);
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
        for (ItemDataDTO dto : dtoMap.values()) {
            Data data = dto.getData();
            if (data != null && data.getKsfGoods() != null) {
                data.setDocCount(data.getKsfGoods().size());
            }
        }
        resultSet1.close();
        resultSet2.close();
        return new ArrayList<>(dtoMap.values());
    }
}
