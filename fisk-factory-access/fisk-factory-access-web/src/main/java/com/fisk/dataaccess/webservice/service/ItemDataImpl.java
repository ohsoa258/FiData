package com.fisk.dataaccess.webservice.service;

import com.alibaba.fastjson.JSON;
import com.fisk.dataaccess.dto.api.ReceiveDataDTO;
import com.fisk.dataaccess.service.impl.ApiConfigImpl;
import com.fisk.dataaccess.webservice.IServerItemData;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.WsAccessDTO;
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
public class ItemDataImpl implements IServerItemData {

    @Resource
    private ApiConfigImpl apiConfig;

    @Resource
    private PublishTaskClient taskClient;

    /**
     * 康师傅前置机定制接口--物料主数据
     *
     * @param item
     * @return
     */
    @Override
    @WebMethod
    @WebResult(name = "KSF_ItemResult")
    public KSF_NoticeResult ksf_item_data(@WebParam(name = "KSF_Item") KSF_Item item) {
        log.debug("物料主数据推送的数据：" + JSON.toJSONString(item));
        //将webservice接收到的xml格式的数据转换为json格式的数据
        KsfItemDTO data = new KsfItemDTO();
        data.setSourceSys(item.getAPI_Message().getSourceSys());
        data.setTargetSys(item.getAPI_Message().getTargetSys());
        data.setUpdateTime(item.getAPI_Message().getUpdateTime());
        data.setGuid(item.getAPI_Message().getGuid());
        data.setSingleTargetSys(item.getAPI_Message().getSingleTargetSys());
        data.setAppKey(item.getAPI_Message().getAppKey());
        data.setIsTest(item.getAPI_Message().getIsTest());
        data.setIsManualSend(item.getAPI_Message().getIsManualSend());
        data.setItemData(item.getItemData());
        String pushData = JSON.toJSONString(data);
        //json解析的根节点 data
        String rebuild = "{\"data\": [" + pushData + "]}";

        ReceiveDataDTO receiveDataDTO = new ReceiveDataDTO();
        //todo：建完应用-api之后写回来
        receiveDataDTO.setApiCode(3L);
        receiveDataDTO.setPushData(rebuild);
        receiveDataDTO.setIfWebService(true);
        String result = apiConfig.KsfWebServicePushData(receiveDataDTO);

        KSF_NoticeResult ksf_noticeResult = new KSF_NoticeResult();
        //统一报文返回类型
        if (result.contains("失败")) {
            ksf_noticeResult.setSTATUS("1");
        } else if (!result.contains("成功")) {
            ksf_noticeResult.setSTATUS("1");
        } else {
            ksf_noticeResult.setSTATUS("0");
        }
        ksf_noticeResult.setINFOTEXT(result);
        //发送消息给数据分发服务
        WsAccessDTO wsAccessDTO = new WsAccessDTO();
        wsAccessDTO.setApiConfigId(3);
        wsAccessDTO.setBatchCode(null);
        wsAccessDTO.setSourceSys(item.getAPI_Message().getSourceSys());
        wsAccessDTO.setIsAcknowledgement(0);
        //发送消息给数据分发服务
        try {
            taskClient.wsAccessToConsume(wsAccessDTO);
        }catch (Exception e){
            log.error("调用数据分发失败，原因：",e);
        }
        return ksf_noticeResult;
    }

}
