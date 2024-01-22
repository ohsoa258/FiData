package com.fisk.dataaccess.webservice.service;

import com.alibaba.fastjson.JSON;
import com.fisk.auth.client.AuthClient;
import com.fisk.auth.dto.UserAuthDTO;
import com.fisk.common.core.constants.RedisTokenKey;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.api.ReceiveDataDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.service.impl.ApiConfigImpl;
import com.fisk.dataaccess.service.impl.AppDataSourceImpl;
import com.fisk.dataaccess.webservice.IWebServiceServer;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.WsAccessDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import java.util.List;

/**
 * @author lsj
 * @date 2023-10-8 14:05:02
 * 该实现类用于提供webService客户端方式可调用的方法
 */
@Service
@WebService(targetNamespace = "http://tempuri.org/")
@Slf4j
public class WebServiceImpl implements IWebServiceServer {

    @Resource
    private ApiConfigImpl apiConfig;
    @Resource
    private AuthClient authClient;
    @Resource
    private AppDataSourceImpl appDataSourceImpl;
    @Resource
    private PublishTaskClient taskClient;

    /**
     * webService推送数据
     *
     * @param dataDTO
     * @return 执行结果
     */
    @Override
    @WebMethod()
    @WebResult(name = "result")
    public String webServicePushData(@WebParam(name = "dataDTO") WebServiceReceiveDataDTO dataDTO) {
        String token = dataDTO.getToken();
        if (token == null) {
            log.error("token为空，请先获取token");
            return "token为空，请先获取token";
        }
        ReceiveDataDTO receiveDataDTO = new ReceiveDataDTO();
        receiveDataDTO.setApiCode(dataDTO.getWebServiceCode());
        receiveDataDTO.setPushData(dataDTO.getData());
        receiveDataDTO.setIfWebService(true);
        receiveDataDTO.setWebServiceToken(token);
        return apiConfig.webServicePushData(receiveDataDTO);
    }

    /**
     * 获取webService的临时token
     *
     * @param userDTO
     * @return 获取token结果
     */
    @Override
    @WebMethod
    @WebResult(name = "token")
    public String webServiceGetToken(@WebParam(name = "userDTO") WebServiceUserDTO userDTO) {

        // 根据账号名称查询对应的app_id下
        List<AppDataSourcePO> dataSourcePos =
                appDataSourceImpl.query().eq("realtime_account", userDTO.getUseraccount()).list();
        if (CollectionUtils.isEmpty(dataSourcePos)) {
            log.error("webServiceGetToken方法的账号或密码不正确或数据库中指定账号的realtime_account和realtime_pwd为空,请联系管理人员...");
            return "webServiceGetToken方法的账号或密码不正确或数据库中指定账号的realtime_account和realtime_pwd为空,请联系管理人员...";
        }
        AppDataSourcePO dataSourcePo = dataSourcePos.get(0);
        if (!dataSourcePo.realtimeAccount.equals(userDTO.getUseraccount()) || !dataSourcePo.realtimePwd.equals(userDTO.getPassword())) {
            log.error("请输入正确的账号或密码");
            return "请输入正确的账号或密码";
        }
        UserAuthDTO userAuthDTO = new UserAuthDTO();
        userAuthDTO.setUserAccount(userDTO.getUseraccount());
        userAuthDTO.setPassword(userDTO.getPassword());
        userAuthDTO.setTemporaryId(RedisTokenKey.DATA_ACCESS_TOKEN + dataSourcePo.id);

        ResultEntity<String> result = authClient.getToken(userAuthDTO);
        if (result.code == ResultEnum.SUCCESS.getCode()) {
            return result.data;
        } else {
            log.error("远程调用失败,方法名: 【auth-service:getToken】");
            return "远程调用失败,方法名: 【auth-service:getToken】";
        }
    }

    /**
     * 康师傅前置机定制接口--通知单
     *
     * @param api_message
     * @param elements
     * @return
     */
    @Override
    @WebMethod(action = "http://tempuri.org/KSF_Notice")
    @WebResult(name = "KSF_NoticeResult")
    public ResponseMessage KSF_Notice(@WebParam(name = "API_Message", targetNamespace = "http://tempuri.org/") BPI_Message api_message, @WebParam(name = "Elements", targetNamespace = "http://tempuri.org/") Elements elements) {
        log.debug("通知单推送的系统数据：" + JSON.toJSONString(api_message));
        log.debug("通知单推送的明细数据：" + JSON.toJSONString(elements));
        List<HEADER> header = elements.getElement().getHEADER();
        List<DETAIL> detail = elements.getElement().getDETAIL();
        //将webservice接收到的xml格式的数据转换为json格式的数据
        KsfNoticeDTO data = new KsfNoticeDTO();
        data.setSourceSys(api_message.getSourceSys());
        data.setTargetSys(api_message.getTargetSys());
        data.setUpdateTime(api_message.getUpdateTime());
        data.setGuid(api_message.getGuid());
        data.setSingleTargetSys(api_message.getSingleTargetSys());
        data.setAppKey(api_message.getAppKey());
        data.setIsTest(api_message.getIsTest());
        data.setIsManualSend(api_message.getIsManualSend());
        data.setHeaders(header);
        data.setDetails(detail);
        String pushData = JSON.toJSONString(data);
        //json解析的根节点 data
        String rebuild = "{\"data\": [" + pushData + "]}";

        ReceiveDataDTO receiveDataDTO = new ReceiveDataDTO();
        //todo：建完应用-api之后写回来
        receiveDataDTO.setApiCode(11L);
        receiveDataDTO.setPushData(rebuild);
        receiveDataDTO.setIfWebService(true);
        String result = apiConfig.KsfWebServicePushData(receiveDataDTO);

        //表头行数
        int headerSize = header.size();
        //业务数据行数
        int detailSize = detail.size();

        ResponseMessage responseMessage = new ResponseMessage();
        //统一报文返回类型
        if (result.contains("失败")) {
            responseMessage.setSTATUS("1");
            result = "前置机数据同步失败，请联系系统管理员";
        } else if (!result.contains("成功")) {
            responseMessage.setSTATUS("1");
            result = "前置机数据同步失败，请联系系统管理员";
        } else {
            responseMessage.setSTATUS("0");
            result = "前置机数据接收-同步成功，本次接收明细：表头条数：" + headerSize + " 业务数据条数：" + detailSize;
        }
        responseMessage.setINFOTEXT(result);
        log.info("待返回的通知单执行结果：" + responseMessage);

        WsAccessDTO wsAccessDTO = new WsAccessDTO();
        wsAccessDTO.setApiConfigId(11);
        wsAccessDTO.setBatchCode(null);
        wsAccessDTO.setSourceSys(api_message.getSourceSys());
        wsAccessDTO.setIsAcknowledgement(0);
        //发送消息给数据分发服务
        taskClient.wsAccessToConsume(wsAccessDTO);
        return responseMessage;
    }

//    /**
//     * 康师傅前置机定制接口--物料主数据
//     *
//     * @param item
//     * @return
//     */
//    @Override
//    @WebMethod
//    @WebResult(name = "KSF_ItemResult")
//    public KSF_NoticeResult ksf_item_data(@WebParam(name = "KSF_Item") KSF_Item item) {
//        log.debug("物料主数据推送的数据：" + JSON.toJSONString(item));
//        //将webservice接收到的xml格式的数据转换为json格式的数据
//        KsfItemDTO data = new KsfItemDTO();
//        data.setSourceSys(item.getAPI_Message().getSourceSys());
//        data.setTargetSys(item.getAPI_Message().getTargetSys());
//        data.setUpdateTime(item.getAPI_Message().getUpdateTime());
//        data.setGuid(item.getAPI_Message().getGuid());
//        data.setSingleTargetSys(item.getAPI_Message().getSingleTargetSys());
//        data.setAppKey(item.getAPI_Message().getAppKey());
//        data.setIsTest(item.getAPI_Message().getIsTest());
//        data.setIsManualSend(item.getAPI_Message().getIsManualSend());
//        data.setItemData(item.getItemData());
//        String pushData = JSON.toJSONString(data);
//        //json解析的根节点 data
//        String rebuild = "{\"data\": [" + pushData + "]}";
//
//        ReceiveDataDTO receiveDataDTO = new ReceiveDataDTO();
//        //todo：建完应用-api之后写回来
//        receiveDataDTO.setApiCode(11L);
//        receiveDataDTO.setPushData(rebuild);
//        receiveDataDTO.setIfWebService(true);
//        String result = apiConfig.KsfWebServicePushData(receiveDataDTO);
//
//        //统一报文返回类型
//        KSF_NoticeResult ksf_noticeResult = new KSF_NoticeResult();
//        ksf_noticeResult.setSTATUS(result);
//        ksf_noticeResult.setINFOTEXT(result);
//        return ksf_noticeResult;
//    }
//
//    /**
//     * 康师傅前置机定制接口--库存状态变更
//     *
//     * @param inventory
//     * @return
//     */
//    @Override
//    @WebMethod
//    @WebResult(name = "KSF_Inventory_StatusResult")
//    public KSF_NoticeResult ksf_inventory_data(@WebParam(name = "KSF_Inventory_Status") KSF_Inventory_Status inventory) {
//        log.debug("库存状态变更推送的数据：" + JSON.toJSONString(inventory));
//        //将webservice接收到的xml格式的数据转换为json格式的数据
//        KsfInventoryDTO data = new KsfInventoryDTO();
//        data.setSourceSys(inventory.getAPI_Message().getSourceSys());
//        data.setTargetSys(inventory.getAPI_Message().getTargetSys());
//        data.setUpdateTime(inventory.getAPI_Message().getUpdateTime());
//        data.setGuid(inventory.getAPI_Message().getGuid());
//        data.setSingleTargetSys(inventory.getAPI_Message().getSingleTargetSys());
//        data.setAppKey(inventory.getAPI_Message().getAppKey());
//        data.setIsTest(inventory.getAPI_Message().getIsTest());
//        data.setIsManualSend(inventory.getAPI_Message().getIsManualSend());
//        data.setKSF_Inventory(inventory.getKSF_Inventory());
//        String pushData = JSON.toJSONString(data);
//        //json解析的根节点 data
//        String rebuild = "{\"data\": [" + pushData + "]}";
//
//        ReceiveDataDTO receiveDataDTO = new ReceiveDataDTO();
//        //todo：建完应用-api之后写回来
//        receiveDataDTO.setApiCode(11L);
//        receiveDataDTO.setPushData(rebuild);
//        receiveDataDTO.setIfWebService(true);
//        String result = apiConfig.KsfWebServicePushData(receiveDataDTO);
//
//        //统一报文返回类型
//        KSF_NoticeResult ksf_noticeResult = new KSF_NoticeResult();
//        ksf_noticeResult.setSTATUS(result);
//        ksf_noticeResult.setINFOTEXT(result);
//        return ksf_noticeResult;
//    }
//
//    /**
//     * wms-推送确认单数据
//     *
//     * @param data
//     * @return 执行结果
//     */
//    @Override
//    @WebMethod()
//    @WebResult(name = "KSF_Acknowledgement_Result")
//    public String ksf_acknowledgement_data(@WebParam(name = "AcknowledgementData") String data) {
//        log.debug("库存状态变更推送的数据：" + JSON.toJSONString(data));
//        ReceiveDataDTO receiveDataDTO = new ReceiveDataDTO();
//        //todo：建完应用-api之后写回来
//        receiveDataDTO.setApiCode(1L);
//        receiveDataDTO.setPushData(data);
//        receiveDataDTO.setIfWebService(true);
//        return apiConfig.KsfWebServicePushData(receiveDataDTO);
//    }

}
