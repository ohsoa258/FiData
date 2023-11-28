package com.fisk.dataservice.handler.ksf.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.ksfwebservice.Inventory.Data;
import com.fisk.dataservice.dto.ksfwebservice.Inventory.InventoryStatusChangesDTO;
import com.fisk.dataservice.dto.ksfwebservice.Inventory.MATDOCTAB;
import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.handler.ksf.KsfWebServiceHandler;
import com.fisk.dataservice.service.*;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Author: wangjian
 * @Date: 2023-09-14
 * @Description:
 */
@Slf4j
@Component
public class KsfInventoryStatusChanges extends KsfWebServiceHandler {

    private static ITableApiAuthRequestService tableApiAuthRequestService;

    private static ITableApiResultService tableApiResultService;

    private static ITableAppManageService tableAppService;
    private static ITableApiService tableApiService;
    private static ITableApiParameterService tableApiParameterService;
    private static UserClient userClient;

    @Autowired
    public void setTableAppService(ITableAppManageService tableAppService) {
        KsfInventoryStatusChanges.tableAppService = tableAppService;
    }

    @Autowired
    public void setTableApiService(ITableApiService tableApiService) {
        KsfInventoryStatusChanges.tableApiService = tableApiService;
    }

    @Autowired
    public void setTableApiParameterService(ITableApiParameterService tableApiParameterService) {
        KsfInventoryStatusChanges.tableApiParameterService = tableApiParameterService;
    }

    @Autowired
    public void setUserClient(UserClient userClient) {
        KsfInventoryStatusChanges.userClient = userClient;
    }

    @Autowired
    public void setTableApiAuthRequestService(ITableApiAuthRequestService tableApiAuthRequestService) {
        KsfInventoryStatusChanges.tableApiAuthRequestService = tableApiAuthRequestService;
    }

    @Autowired
    public void setTableApiResultService(ITableApiResultService tableApiResultService) {
        KsfInventoryStatusChanges.tableApiResultService = tableApiResultService;
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
        List<InventoryStatusChangesDTO> resultJsonData = null;
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
                log.info("开始执行脚本:{}", tableApiServicePO.getSqlScript());
                //获取查询时间区间
                String startTime = tableApiServicePO.getSyncTime();
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
                String endTime = now.format(formatter);

                String[] split = tableApiServicePO.getSqlScript().split(";");
                String systemDataSql = split[0].replace("${startTime}", startTime).replace("${endTime}", endTime);
                String statusChangesSql = split[1].replace("${startTime}", startTime).replace("${endTime}", endTime);
                ResultSet systemData = st.executeQuery(systemDataSql);
                ResultSet statusChanges = st1.executeQuery(statusChangesSql);
                resultJsonData = assembleInventoryStatusChangesDTO(systemData, statusChanges);
                number = resultJsonData.size();
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
//            map.put("result",JSONObject.parseObject(body));
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
        String result = null;
        try {
            Service service = new Service();
            Call call = (Call) service.createCall();

            // 设置wsdl地址
            call.setTargetEndpointAddress(new URL("wsdl地址"));

            // 设置命名空间和方法名
            call.setOperationName(new QName("http://tempuri.org/", "SAPPushDataInfMATDOC"));

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


    public List<InventoryStatusChangesDTO> assembleInventoryStatusChangesDTO(ResultSet resultSet1, ResultSet resultSet2) throws SQLException {
        Map<String, InventoryStatusChangesDTO> dtoMap = new HashMap<>();
        // 遍历第一个结果集，将父表数据组装成 InventoryStatusChangesDTO 对象，并保存到 dtoMap 中
        while (resultSet1.next()) {
            String batchCode = resultSet1.getString("fidata_batch_code");

            InventoryStatusChangesDTO dto = dtoMap.get(batchCode);
            if (dto == null) {
                dto = new InventoryStatusChangesDTO();
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

            InventoryStatusChangesDTO dto = dtoMap.get(batchCode);
            if (dto != null) {
                Data data = dto.getData();
                if (data.getMATDOCTAB() == null) {
                    data.setMATDOCTAB(new ArrayList<>());
                }

                MATDOCTAB matdoctab = new MATDOCTAB();
                // 设置其他字段的值
                matdoctab.setMBLNR(resultSet2.getString("mblnr"));
                matdoctab.setMJAHR(resultSet2.getString("mjahr"));
                matdoctab.setBLART(resultSet2.getString("blart"));
                matdoctab.setBLDAT(resultSet2.getString("bldat"));
                matdoctab.setBUDAT(resultSet2.getString("budat"));
                matdoctab.setCPUDT(resultSet2.getString("cpudt"));
                matdoctab.setCPUTM(resultSet2.getString("cputm"));
                matdoctab.setUSNAM(resultSet2.getString("usnam"));
                matdoctab.setBKTXT(resultSet2.getString("bktxt"));
                matdoctab.setLGPLA(resultSet2.getString("lgpla"));
                matdoctab.setVTXTK(resultSet2.getString("vtxtk"));
                matdoctab.setMTART(resultSet2.getString("mtart"));
                matdoctab.setMATKL(resultSet2.getString("matkl"));
                matdoctab.setXAUTO(resultSet2.getString("xauto"));
                matdoctab.setKZBEW(resultSet2.getString("kzbew"));
                matdoctab.setKZZUG(resultSet2.getString("kzzug"));
                matdoctab.setZEILE(resultSet2.getString("zeile"));
                matdoctab.setBWART(resultSet2.getString("bwart"));
                matdoctab.setMATNR(resultSet2.getString("matnr"));
                matdoctab.setMAKTX(resultSet2.getString("maktx"));
                matdoctab.setWERKS(resultSet2.getString("werks"));
                matdoctab.setWNAME1(resultSet2.getString("wname1"));
                matdoctab.setLGORT(resultSet2.getString("lgort"));
                matdoctab.setLGOBE(resultSet2.getString("lgobe"));
                matdoctab.setCHARG(resultSet2.getString("charg"));
                matdoctab.setINSMK(resultSet2.getString("insmk"));
                matdoctab.setSOBKZ(resultSet2.getString("sobkz"));
                matdoctab.setMENGE(resultSet2.getString("menge"));
                matdoctab.setMEINS(resultSet2.getString("meins"));
                matdoctab.setERFMG(resultSet2.getString("erfmg"));
                matdoctab.setERFME(resultSet2.getString("erfme"));
                matdoctab.setDMBTR(resultSet2.getString("dmbtr"));
                matdoctab.setWAERS(resultSet2.getString("waers"));
                matdoctab.setLIFNR(resultSet2.getString("lifnr"));
                matdoctab.setLNAME1(resultSet2.getString("lname1"));
                matdoctab.setEBELP(resultSet2.getString("ebelp"));
                matdoctab.setKUNNR(resultSet2.getString("kunnr"));
                matdoctab.setKNAME1(resultSet2.getString("kname1"));
                matdoctab.setWEMPF(resultSet2.getString("wempf"));
                matdoctab.setKDAUF(resultSet2.getString("kdauf"));
                matdoctab.setKDPOS(resultSet2.getString("kdpos"));
                matdoctab.setXBLNR(resultSet2.getString("xblnr"));
                matdoctab.setBUKRS(resultSet2.getString("bukrs"));
                matdoctab.setBUTXT(resultSet2.getString("butxt"));
                matdoctab.setBELNR(resultSet2.getString("belnr"));
                matdoctab.setSAKTO(resultSet2.getString("sakto"));
                matdoctab.setKOSTL(resultSet2.getString("kostl"));
                matdoctab.setAUFNR(resultSet2.getString("aufnr"));
                matdoctab.setANLN1(resultSet2.getString("anln1"));
                matdoctab.setVFDAT(resultSet2.getString("vfdat"));
                matdoctab.setHSDAT(resultSet2.getString("hsdat"));
                matdoctab.setUMMAT(resultSet2.getString("ummat"));
                matdoctab.setUMWRK(resultSet2.getString("umwrk"));
                matdoctab.setUMLGO(resultSet2.getString("umlgo"));
                matdoctab.setUMCHA(resultSet2.getString("umcha"));
                matdoctab.setGRUND(resultSet2.getString("grund"));
                matdoctab.setSGTXT(resultSet2.getString("sgtxt"));

                data.getMATDOCTAB().add(matdoctab);
            }
        }

        // 设置 DocCount 属性为 MATDOCTAB 的 size
        for (InventoryStatusChangesDTO dto : dtoMap.values()) {
            Data data = dto.getData();
            if (data != null && data.getMATDOCTAB() != null) {
                data.setDocCount(data.getMATDOCTAB().size());
            }
        }
        resultSet1.close();
        resultSet2.close();
        return new ArrayList<>(dtoMap.values());
    }
}
