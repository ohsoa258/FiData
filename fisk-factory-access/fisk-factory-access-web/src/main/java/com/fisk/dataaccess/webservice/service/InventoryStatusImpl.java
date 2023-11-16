package com.fisk.dataaccess.webservice.service;

import com.alibaba.fastjson.JSON;
import com.fisk.auth.client.AuthClient;
import com.fisk.dataaccess.dto.api.ReceiveDataDTO;
import com.fisk.dataaccess.service.impl.ApiConfigImpl;
import com.fisk.dataaccess.service.impl.AppDataSourceImpl;
import com.fisk.dataaccess.webservice.IServerInventoryStatus;
import com.fisk.dataaccess.webservice.IServerItemData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * @author lsj
 * @date 2023-10-8 14:05:02
 * 该实现类用于提供webService客户端方式可调用的方法
 */
@Service
@WebService
@Slf4j
public class InventoryStatusImpl implements IServerInventoryStatus {

    @Resource
    private ApiConfigImpl apiConfig;

    /**
     * 康师傅前置机定制接口--库存状态变更
     *
     * @param inventory
     * @return
     */
    @Override
    @WebMethod
    @WebResult(name = "KSF_Inventory_StatusResult")
    public KSF_NoticeResult ksf_inventory_data(@WebParam(name = "KSF_Inventory_Status") KSF_Inventory_Status inventory) {
        log.debug("库存状态变更推送的数据：" + JSON.toJSONString(inventory));
        //将webservice接收到的xml格式的数据转换为json格式的数据
        KsfInventoryDTO data = new KsfInventoryDTO();
        data.setSourceSys(inventory.getAPI_Message().getSourceSys());
        data.setTargetSys(inventory.getAPI_Message().getTargetSys());
        data.setUpdateTime(inventory.getAPI_Message().getUpdateTime());
        data.setGuid(inventory.getAPI_Message().getGuid());
        data.setSingleTargetSys(inventory.getAPI_Message().getSingleTargetSys());
        data.setAppKey(inventory.getAPI_Message().getAppKey());
        data.setIsTest(inventory.getAPI_Message().getIsTest());
        data.setIsManualSend(inventory.getAPI_Message().getIsManualSend());
        data.setKSF_Inventory(inventory.getKSF_Inventory());
        String pushData = JSON.toJSONString(data);
        //json解析的根节点 data
        String rebuild = "{\"data\": [" + pushData + "]}";

        ReceiveDataDTO receiveDataDTO = new ReceiveDataDTO();
        //todo：建完应用-api之后写回来
        receiveDataDTO.setApiCode(4L);
        receiveDataDTO.setPushData(rebuild);
        receiveDataDTO.setIfWebService(true);
        String result = apiConfig.KsfWebServicePushData(receiveDataDTO);

        //统一报文返回类型
        KSF_NoticeResult ksf_noticeResult = new KSF_NoticeResult();
        ksf_noticeResult.setSTATUS(result);
        ksf_noticeResult.setINFOTEXT(result);
        return ksf_noticeResult;
    }

}
