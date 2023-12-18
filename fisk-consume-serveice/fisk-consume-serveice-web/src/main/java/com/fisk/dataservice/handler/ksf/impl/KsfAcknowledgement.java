package com.fisk.dataservice.handler.ksf.impl;

import cn.com.ksf.ws.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.entity.TableApiParameterPO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.handler.ksf.KsfWebServiceHandler;
import com.fisk.dataservice.service.ITableApiService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-12-04
 * @Description:
 */
@Slf4j
@Component
public class KsfAcknowledgement extends KsfWebServiceHandler {

    private static ITableApiService tableApiService;

    private static UserClient userClient;

    private static RedisUtil redisUtil;

    @Autowired
    public void setTableApiService(ITableApiService tableApiService) {
        KsfAcknowledgement.tableApiService = tableApiService;
    }

    @Autowired
    public void setUserClient(UserClient userClient) {
        KsfAcknowledgement.userClient = userClient;
    }

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        KsfAcknowledgement.redisUtil = redisUtil;
    }

    @Override
    public ApiResultDTO sendApi(TableAppPO tableAppPO, long apiId, String fidata_batch_code,String sourcesys) {
        redisUtil.expire(RedisKeyEnum.TABLE_KSF_WEB_SERVER_SYNC.getName() + apiId, 100);
        ApiResultDTO apiResultDTO = new ApiResultDTO();
        TableApiServicePO tableApiServicePO = tableApiService.getById(apiId);
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
        ZALLSAPUPLOADGOODSMOV resultJsonData = null;

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
                String systemDataSql;
                String headSql;
                String detailSql;
                if (StringUtils.isNotBlank(fidata_batch_code)){
                    systemDataSql = split[0] + " where fidata_batch_code = '" + fidata_batch_code + "'";
                    headSql = split[1] + " WHERE fidata_batch_code = '" + fidata_batch_code + "'";
                    detailSql = split[2] + " WHERE fidata_batch_code = '" + fidata_batch_code + "'";
                }else {
                    systemDataSql = split[0] + " where TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP ORDER BY fi_createtime;";
                    headSql = split[1] + " WHERE fidata_batch_code in  (select fidata_batch_code from wms.wms_acknowledgement_sys  where TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP  AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP);";
                    detailSql = split[2] + " WHERE fidata_batch_code in  (select fidata_batch_code from wms.wms_acknowledgement_sys  where TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP  AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP);";

                }
                log.info("开始执行脚本systemData:{}", systemDataSql);
                ResultSet systemData = st1.executeQuery(systemDataSql);
                log.info("开始执行脚本head:{}", headSql);
                ResultSet heads = st2.executeQuery(headSql);
                log.info("开始执行脚本detail:{}", detailSql);
                ResultSet details = st3.executeQuery(detailSql);
                resultJsonData = assembleConfirmationSlipDTO(systemData, heads, details);
                apiResultDTO.setNumber(resultJsonData.getITMATDOCHEAD().getZALLSAPUPLOADGOODSMOV1().size());
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
        log.info("apiId" + tableApiServicePO.getId() + "通知单推送数据:" + JSON.toJSON(resultJsonData));
        apiResultDTO = send(resultJsonData);
        apiResultDTO.setSyncTime(tableApiServicePO.getSyncTime());
        if (apiResultDTO.getFlag()) {
            tableApiServicePO.setSyncTime(endTime);
            tableApiService.updateById(tableApiServicePO);
        }
        apiResultDTO.setNumber(number);
        return apiResultDTO;
    }

    public ApiResultDTO send(ZALLSAPUPLOADGOODSMOV resultJsonData) {
        ApiResultDTO apiResultDTO = new ApiResultDTO();
        try {
            // 创建服务实例
            ZALLSAPUPLOADGOODSMOVOrchestration1PortDingT service = new ZALLSAPUPLOADGOODSMOVOrchestration1PortDingT();
// 获取端口
            ZALLSAPUPLOADGOODSMOVOrchestration1PortDingTSoap port = service.getZALLSAPUPLOADGOODSMOVOrchestration1PortDingTSoap12();
            OperationZALLSAPUPLOADGOODSMOV operationZALLSAPUPLOADGOODSMOV = new OperationZALLSAPUPLOADGOODSMOV();
            operationZALLSAPUPLOADGOODSMOV.setZALLSAPUPLOADGOODSMOV(resultJsonData);
            log.info("发送前");
            OperationZALLSAPUPLOADGOODSMOVResponse operationZALLSAPUPLOADGOODSMOVResponse = port.operationZALLSAPUPLOADGOODSMOV(operationZALLSAPUPLOADGOODSMOV);
            log.info("发送后接收:"+JSON.toJSON(operationZALLSAPUPLOADGOODSMOVResponse));
            apiResultDTO.setFlag(true);
            apiResultDTO.setMsg("发送sap确认单成功！");
        } catch (Exception e) {
            apiResultDTO.setFlag(false);
            apiResultDTO.setMsg(e.toString());
            e.printStackTrace();
        }
        return apiResultDTO;
    }

    public ZALLSAPUPLOADGOODSMOV assembleConfirmationSlipDTO(ResultSet resultSet1, ResultSet resultSet2, ResultSet resultSet3) throws SQLException, JAXBException {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String nowTime = now.format(formatter);
        ZALLSAPUPLOADGOODSMOV zallsapuploadgoodsmov = new ZALLSAPUPLOADGOODSMOV();
        while (resultSet1.next()) {
            zallsapuploadgoodsmov.setSOURCESYS(resultSet1.getString("sourcesys"));
            zallsapuploadgoodsmov.setTARGETSYS(resultSet1.getString("targetsys"));
            zallsapuploadgoodsmov.setUPDATETIME(resultSet1.getString("updatetime"));
        }
        while (resultSet2.next()) {
            ZALLSAPUPLOADGOODSMOV1 header = new ZALLSAPUPLOADGOODSMOV1();
            header.setBSART(resultSet2.getString("bsart"));
            header.setEBELN(resultSet2.getString("ebeln"));
            header.setIDATE(resultSet2.getString("i_date"));
            header.setITIME(resultSet2.getString("i_time"));
            header.setLGPLA(resultSet2.getString("lgpla"));
            header.setVTXTK(resultSet2.getString("vtxtk"));
            header.setHTEXT(resultSet2.getString("htext"));
            header.setBUDAT(nowTime);
            ArrayOfZALLSAPUPLOADGOODSMOV1 itmatdochead = zallsapuploadgoodsmov.getITMATDOCHEAD();
            if (itmatdochead == null){
                itmatdochead = new ArrayOfZALLSAPUPLOADGOODSMOV1();
                zallsapuploadgoodsmov.setITMATDOCHEAD(itmatdochead);
            }
            zallsapuploadgoodsmov.getITMATDOCHEAD().getZALLSAPUPLOADGOODSMOV1().add(header);
        }

        while (resultSet3.next()) {
            ZALLSAPUPLOADGOODSMOV2 item = new ZALLSAPUPLOADGOODSMOV2();
            item.setEBELN(resultSet3.getString("ebeln"));
            item.setPOSNR(resultSet3.getString("posnr"));
            item.setSGTXT(resultSet3.getString("sgtxt"));
            item.setMATNR(resultSet3.getString("matnr"));
            item.setDZUSCH("");
            // 创建一个 BigDecimal 对象
            BigDecimal value = new BigDecimal(resultSet3.getString("menge"));

            // 创建一个 JAXBElement 对象
            QName qname = new QName("http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", "MENGE");
            JAXBElement<BigDecimal> jaxbElement = new JAXBElement<>(qname, BigDecimal.class, value);

            // 调用 setMENGE 方法并传递 JAXBElement 对象
            item.setMENGE(jaxbElement);
            item.setMEINS(resultSet3.getString("meins"));
            item.setWERKS(resultSet3.getString("werks"));
            item.setLGORT(resultSet3.getString("lgort"));
            item.setINSMK(resultSet3.getString("insmk"));
            item.setHSDAT(resultSet3.getString("hsdat"));
            item.setLICHA(resultSet3.getString("licha"));
            item.setCHARG(resultSet3.getString("charg"));
            item.setELIKZ(resultSet3.getString("elikz"));
            ArrayOfZALLSAPUPLOADGOODSMOV2 itmatdocdetail = zallsapuploadgoodsmov.getITMATDOCDETAILS();
            if (itmatdocdetail == null){
                itmatdocdetail = new ArrayOfZALLSAPUPLOADGOODSMOV2();
                zallsapuploadgoodsmov.setITMATDOCDETAILS(itmatdocdetail);
            }
            zallsapuploadgoodsmov.getITMATDOCDETAILS().getZALLSAPUPLOADGOODSMOV2().add(item);
        }
        String s = convertToXml(zallsapuploadgoodsmov);
        log.info("请求体:"+s);
        return zallsapuploadgoodsmov;
    }

    public static String convertToXml(Object object) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(object, stringWriter);

        return stringWriter.toString();
    }
}
