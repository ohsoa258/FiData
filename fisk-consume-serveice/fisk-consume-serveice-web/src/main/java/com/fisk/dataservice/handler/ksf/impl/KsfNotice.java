package com.fisk.dataservice.handler.ksf.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.ksfwebservice.notice.Data;
import com.fisk.dataservice.dto.ksfwebservice.notice.NoticeData;
import com.fisk.dataservice.dto.ksfwebservice.notice.NoticeDetail;
import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.entity.TableApiParameterPO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.handler.ksf.KsfWebServiceHandler;
import com.fisk.dataservice.service.ITableApiService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.xml.namespace.QName;
import java.net.URL;
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
        List<NoticeData> resultJsonData = null;
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
                String systemDataSql = split[0]+"where TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP;";
                String noticeDataSql = split[1]+"WHERE fidata_batch_code in  (select fidata_batch_code from ods_sap_ksf_notice  where TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP);";
                String noticeDetailSql = split[2]+"WHERE fidata_batch_code in (select fidata_batch_code from ods_sap_ksf_notice where TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP);";
                ResultSet systemData = st1.executeQuery(systemDataSql);
                ResultSet noticeData = st2.executeQuery(noticeDataSql);
                ResultSet noticeDetail = st3.executeQuery(noticeDetailSql);
                resultJsonData = assembleInventoryStatusChangesDTO(systemData, noticeData,noticeDetail);
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
    public List<NoticeData> assembleInventoryStatusChangesDTO(ResultSet resultSet1, ResultSet resultSet2, ResultSet resultSet3) throws SQLException {
        Map<String, NoticeData> dtoMap = new HashMap<>();
        // 遍历第一个结果集，将父表数据组装成 InventoryStatusChangesDTO 对象，并保存到 dtoMap 中
        while (resultSet1.next()) {
            String batchCode = resultSet1.getString("fidata_batch_code");

            NoticeData dto = dtoMap.get(batchCode);
            if (dto == null) {
                dto = new NoticeData();
                dto.setSourceSys(resultSet1.getString("sourcesys"));
                dto.setTargetSys(resultSet1.getString("targetsys"));
                dto.setPushSeqNo((int) System.currentTimeMillis());
                dto.setWMSID("ZTJ1");
                dto.setData(new Data());
                dtoMap.put(batchCode, dto);
            }
        }
        Map<String,Map<String, List<NoticeDetail>>> detailMap = new HashMap<>();
        // 遍历第三个结果集，将子表数据组装到对应的父表对象中

        Map<String,List<NoticeDetail>> noticeMap = new HashMap<>();
        while (resultSet3.next()) {
            String batchCode = resultSet1.getString("fidata_batch_code");
            List<NoticeDetail> details = noticeMap.get(batchCode);

            NoticeDetail noticeDetail = new NoticeDetail();
            noticeDetail.setEBELN(resultSet3.getString("ebeln"));
            noticeDetail.setEINDT(resultSet3.getString("eindt"));
            noticeDetail.setPOSNR(resultSet3.getString("posnr"));
            noticeDetail.setLTEXT(resultSet3.getString("ltext"));
            noticeDetail.setPSTYV(resultSet3.getString("pstyv"));
            noticeDetail.setMATNR(resultSet3.getString("matnr"));
            noticeDetail.setMENGE(resultSet3.getString("menge"));
            noticeDetail.setMEINS(resultSet3.getString("meins"));
            noticeDetail.setNETPR(resultSet3.getString("netpr"));
            noticeDetail.setWERKS(resultSet3.getString("werks"));
            noticeDetail.setNAME1(resultSet3.getString("name1"));
            noticeDetail.setLGORT(resultSet3.getString("lgort"));
            noticeDetail.setLGOBE(resultSet3.getString("lgobe"));
            noticeDetail.setINSMK(resultSet3.getString("insmk"));
            noticeDetail.setCHARG(resultSet3.getString("charg"));
            if (details == null) {
                details = new ArrayList<>();
                details.add(noticeDetail);
                noticeMap.put(batchCode, details);
            }else {
                details.add(noticeDetail);
                noticeMap.put(batchCode, details);
            }
        }

        for (Map.Entry<String, List<NoticeDetail>> stringListEntry : noticeMap.entrySet()) {
            Map<String, List<NoticeDetail>> ebelnToDetails = stringListEntry.getValue().stream().collect(groupingBy(NoticeDetail::getEBELN));
            detailMap.put(stringListEntry.getKey(),ebelnToDetails);
        }

        // 遍历第二个结果集，将子表数据组装到对应的父表对象中
        while (resultSet2.next()) {
            String batchCode = resultSet2.getString("fidata_batch_code");
            NoticeData noticeData = dtoMap.get(batchCode);;
            if (noticeData != null) {
                Data data = noticeData.getData();
                if (data.getKsfNotices() == null) {
                    data.setKsfNotices(new ArrayList<>());
                }

                com.fisk.dataservice.dto.ksfwebservice.notice.KsfNotice ksfNotice = new com.fisk.dataservice.dto.ksfwebservice.notice.KsfNotice();
                // 设置其他字段的值
                String ebeln = resultSet2.getString("ebeln");
                ksfNotice.setBSART(resultSet2.getString("bsart"));
                ksfNotice.setEBELN(ebeln);
                ksfNotice.setLGPLA(resultSet2.getString("lgpla"));
                ksfNotice.setVTXTK(resultSet2.getString("vtxtk"));
                ksfNotice.setLIFNR(resultSet2.getString("lifnr"));
                ksfNotice.setNAME1(resultSet2.getString("name1"));
                ksfNotice.setKUNAG(resultSet2.getString("kunag"));
                ksfNotice.setKUNNR(resultSet2.getString("kunnr"));
                ksfNotice.setNAME2(resultSet2.getString("name2"));
                ksfNotice.setHTEXT(resultSet2.getString("htext"));
                ksfNotice.setBUDAT(resultSet2.getString("budat"));
                Map<String, List<NoticeDetail>> stringListMap = detailMap.get(batchCode);
                if (!CollectionUtils.isEmpty(stringListMap)){
                    List<NoticeDetail> noticeDetails = stringListMap.get(ebeln);
                    ksfNotice.setDETAIL(noticeDetails);
                }

                data.getKsfNotices().add(ksfNotice);
            }
        }

        // 设置 DocCount 属性为 MATDOCTAB 的 size
        for (NoticeData dto : dtoMap.values()) {
            Data data = dto.getData();
            if (data != null && data.getKsfNotices() != null) {
                data.setDocCount(data.getKsfNotices().size());
            }
        }
        resultSet1.close();
        resultSet2.close();
        return new ArrayList<>(dtoMap.values());
    }
}
