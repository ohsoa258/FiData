package com.fisk.dataaccess.webservice;

import com.fisk.dataaccess.webservice.service.KSF_Notice;
import com.fisk.dataaccess.webservice.service.KSF_NoticeResult;
import com.fisk.dataaccess.webservice.service.WebServiceReceiveDataDTO;
import com.fisk.dataaccess.webservice.service.WebServiceUserDTO;

/**
 * @author lsj
 * @date 2023-10-8 14:05:02
 * 该接口用于提供webService客户端方式可调用的方法--实现类的父接口
 */
public interface IWebServiceServer {

    /**
     * webService推送数据
     *
     * @param dto dto
     * @return 执行结果
     */
    String webServicePushData(WebServiceReceiveDataDTO dto);

    /**
     * 获取webService的临时token
     *
     * @param dto dto
     * @return 获取token结果
     */
    String webServiceGetToken(WebServiceUserDTO dto);

    /**
     * 康师傅前置机定制接口--通知单
     *
     * @param KSF_Notice
     * @return
     */
    KSF_NoticeResult ksf_notice(KSF_Notice KSF_Notice);

//    /**
//     * 康师傅前置机定制接口--物料主数据
//     *
//     * @param item
//     * @return
//     */
//    KSF_NoticeResult ksf_item_data(KSF_Item item);
//
//    /**
//     * 康师傅前置机定制接口--库存状态变更
//     *
//     * @param inventory_status
//     * @return
//     */
//    KSF_NoticeResult ksf_inventory_data(KSF_Inventory_Status inventory_status);
//
//    /**
//     * wms-推送确认单数据
//     *
//     * @param data
//     * @return 执行结果
//     */
//    String ksf_acknowledgement_data(String data);

}
