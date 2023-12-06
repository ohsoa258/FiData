package com.fisk.dataservice.handler.ksf.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.client.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
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
    public void setUserClient(RedisUtil redisUtil) {
        KsfAcknowledgement.redisUtil = redisUtil;
    }

    @Override
    public ApiResultDTO sendApi(TableAppPO tableAppPO, long apiId) {
        redisUtil.expire(RedisKeyEnum.TABLE_KSF_WEB_SERVER_SYNC.getName() + apiId, 100);
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
        ZALLSAPUPLOADGOODSMOV resultJsonData = null;

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
                String systemDataSql = split[0] + " where TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP ORDER BY fi_createtime;";
                String headSql = split[1] + " WHERE fidata_batch_code in  (select fidata_batch_code from wms_acknowledgement_sys  where TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP  AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP);";
                String detailSql = split[2] + " WHERE fidata_batch_code in  (select fidata_batch_code from wms_acknowledgement_sys  where TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') > '" + startTime + "'::TIMESTAMP  AND TO_TIMESTAMP(fi_createtime, 'YYYY-MM-DD HH24:MI:SS.US') <= '" + endTime + "'::TIMESTAMP);";
                log.info("开始执行脚本systemData:{}", systemDataSql);
                ResultSet systemData = st.executeQuery(systemDataSql);
                log.info("开始执行脚本head:{}", headSql);
                ResultSet heads = st1.executeQuery(headSql);
                log.info("开始执行脚本detail:{}", detailSql);
                ResultSet details = st1.executeQuery(detailSql);
                resultJsonData = assembleConfirmationSlipDTO(systemData, heads, details);
                apiResultDTO.setNumber(resultJsonData.getITMATDOCHEAD().getZALLSAPUPLOADGOODSMOV1().size());
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
        log.info("apiId" + tableApiServicePO.getId() + "通知单推送数据:" + resultJsonData);
        apiResultDTO = send(resultJsonData);
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
            ZALLSAPUPLOADGOODSMOVOrchestration1PortDingTSoap port = service.getZALLSAPUPLOADGOODSMOVOrchestration1PortDingTSoap();
            OperationZALLSAPUPLOADGOODSMOV operationZALLSAPUPLOADGOODSMOV = new OperationZALLSAPUPLOADGOODSMOV();
            operationZALLSAPUPLOADGOODSMOV.setZALLSAPUPLOADGOODSMOV(resultJsonData);
            OperationZALLSAPUPLOADGOODSMOVResponse operationZALLSAPUPLOADGOODSMOVResponse = port.operationZALLSAPUPLOADGOODSMOV(operationZALLSAPUPLOADGOODSMOV);
            ZALLSAPUPLOADGOODSMOVResponse zallsapuploadgoodsmovResponse = operationZALLSAPUPLOADGOODSMOVResponse.getZALLSAPUPLOADGOODSMOVResponse();
            ArrayOfZALLSAPUPLOADGOODSMOV3 otmatdoc = zallsapuploadgoodsmovResponse.getOTMATDOC();
            List<ZALLSAPUPLOADGOODSMOV3> zallsapuploadgoodsmov3 = otmatdoc.getZALLSAPUPLOADGOODSMOV3();
            ZALLSAPUPLOADGOODSMOV3 zallsapuploadgoodsmov31 = zallsapuploadgoodsmov3.get(0);

            zallsapuploadgoodsmov31.getMBLNR();
            if (Integer.parseInt(zallsapuploadgoodsmov31.getSTATUS()) == 1){
                apiResultDTO.setFlag(true);
                apiResultDTO.setMsg(zallsapuploadgoodsmov31.getINFOTEXT());
            }else if (Integer.parseInt(zallsapuploadgoodsmov31.getSTATUS()) == -1){
                apiResultDTO.setFlag(false);
                apiResultDTO.setMsg(zallsapuploadgoodsmov31.getINFOTEXT());
            }else {
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

    public ZALLSAPUPLOADGOODSMOV assembleConfirmationSlipDTO(ResultSet resultSet1, ResultSet resultSet2, ResultSet resultSet3) throws SQLException, JAXBException {
        ZALLSAPUPLOADGOODSMOV zallsapuploadgoodsmov = new ZALLSAPUPLOADGOODSMOV();
        while (resultSet1.next()) {
            zallsapuploadgoodsmov.setSOURCESYS(resultSet1.getString("sourcesys"));
            zallsapuploadgoodsmov.setTARGETSYS(resultSet1.getString("targetsys"));
            zallsapuploadgoodsmov.setUPDATETIME(resultSet1.getString("updatetime"));
        }
        while (resultSet2.next()) {
            ZALLSAPUPLOADGOODSMOV1 header = new ZALLSAPUPLOADGOODSMOV1();
            header.setBSART(resultSet1.getString("bsart"));
            header.setEBELN(resultSet1.getString("ebeln"));
            header.setIDATE(resultSet1.getString("i_date"));
            header.setITIME(resultSet1.getString("i_time"));
            header.setLGPLA(resultSet1.getString("lgpla"));
            header.setVTXTK(resultSet1.getString("vtxtk"));
            header.setHTEXT(resultSet1.getString("htext"));
            header.setBUDAT(resultSet1.getString("budat"));
            zallsapuploadgoodsmov.getITMATDOCHEAD().getZALLSAPUPLOADGOODSMOV1().add(header);
        }

        while (resultSet3.next()) {
            ZALLSAPUPLOADGOODSMOV2 item = new ZALLSAPUPLOADGOODSMOV2();
            item.setEBELN(resultSet1.getString("ebeln"));
            item.setPOSNR(resultSet1.getString("posnr"));
            item.setSGTXT(resultSet1.getString("sgtxt"));
            item.setMATNR(resultSet1.getString("matnr"));
            // 创建一个 BigDecimal 对象
            BigDecimal value = new BigDecimal(resultSet1.getString("menge"));

            // 创建一个 JAXBElement 对象
            JAXBContext jaxbContext = JAXBContext.newInstance(BigDecimal.class);
            QName qname = new QName("http://example.com/schema", "value");
            JAXBElement<BigDecimal> jaxbElement = new JAXBElement<>(qname, BigDecimal.class, value);

            // 调用 setMENGE 方法并传递 JAXBElement 对象
            item.setMENGE(jaxbElement);
            item.setMEINS(resultSet1.getString("meins"));
            item.setWERKS(resultSet1.getString("werks"));
            item.setLGORT(resultSet1.getString("lgort"));
            item.setINSMK(resultSet1.getString("insmk"));
            item.setHSDAT(resultSet1.getString("hsdat"));
            item.setLICHA(resultSet1.getString("licha"));
            item.setCHARG(resultSet1.getString("charg"));
            item.setELIKZ(resultSet1.getString("elikz"));
            zallsapuploadgoodsmov.getITMATDOCDETAILS().getZALLSAPUPLOADGOODSMOV2().add(item);
        }
        return zallsapuploadgoodsmov;
    }
}
