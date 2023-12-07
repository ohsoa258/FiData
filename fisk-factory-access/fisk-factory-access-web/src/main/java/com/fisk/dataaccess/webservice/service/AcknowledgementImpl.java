package com.fisk.dataaccess.webservice.service;

import com.alibaba.fastjson.JSON;
import com.fisk.dataaccess.dto.api.ReceiveDataDTO;
import com.fisk.dataaccess.service.impl.ApiConfigImpl;
import com.fisk.dataaccess.webservice.IServerAcknowledgement;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.WsAccessDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import java.util.UUID;

/**
 * @author lsj
 * @date 2023-10-8 14:05:02
 * 该实现类用于提供webService客户端方式可调用的方法
 */
@Service
@WebService
@Slf4j
public class AcknowledgementImpl implements IServerAcknowledgement {

    @Resource
    private ApiConfigImpl apiConfig;

    @Resource
    private PublishTaskClient taskClient;

    /**
     * wms-推送确认单数据
     *
     * @param data
     * @return 执行结果
     */
    @Override
    @WebMethod()
    @WebResult(name = "KSF_Acknowledgement_Result")
    public KSF_NoticeResult ksf_acknowledgement_data(@WebParam(name = "AcknowledgementData") String data) {
        log.debug("库存状态变更推送的数据：" + JSON.toJSONString(data));
        //大批次号  本批数据不管是系统表还是父子表  大批次号都保持一致
        String fidata_batch_code = UUID.randomUUID().toString();
        ReceiveDataDTO receiveDataDTO = new ReceiveDataDTO();
        //todo：建完应用-api之后写回来
        receiveDataDTO.setApiCode(15L);
        receiveDataDTO.setPushData(data);
        receiveDataDTO.setIfWebService(true);
        receiveDataDTO.setBatchCode(fidata_batch_code);
        String msg = apiConfig.KsfWebServicePushData(receiveDataDTO);
        KSF_NoticeResult noticeResult = new KSF_NoticeResult();
        noticeResult.setINFOTEXT(msg);
        if (msg.contains("失败") || !msg.contains("成功")) {
            noticeResult.setSTATUS("0");
        } else {
            noticeResult.setSTATUS("1");
        }

        //发送消息给数据分发服务
        WsAccessDTO wsAccessDTO = new WsAccessDTO();
        wsAccessDTO.setApiConfigId(15);
        wsAccessDTO.setBatchCode(receiveDataDTO.getBatchCode());
        //发送消息给数据分发服务
        taskClient.wsAccessToConsume(wsAccessDTO);

        return noticeResult;
    }

}
