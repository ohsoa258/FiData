package com.fisk.dataaccess.webservice;

import com.fisk.dataaccess.webservice.service.KSF_NoticeResult;

/**
 * @author lsj
 * @date 2023-10-8 14:05:02
 * 该接口用于提供webService客户端方式可调用的方法--实现类的父接口
 */
public interface IServerAcknowledgement {

    /**
     * wms-推送确认单数据
     *
     * @param data
     * @return 执行结果
     */
    KSF_NoticeResult ksf_acknowledgement_data(String data);

}
