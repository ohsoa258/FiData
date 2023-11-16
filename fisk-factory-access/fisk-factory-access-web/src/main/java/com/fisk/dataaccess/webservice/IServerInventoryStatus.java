package com.fisk.dataaccess.webservice;

import com.fisk.dataaccess.webservice.service.KSF_Inventory_Status;
import com.fisk.dataaccess.webservice.service.KSF_Item;
import com.fisk.dataaccess.webservice.service.KSF_NoticeResult;

/**
 * @author lsj
 * @date 2023-10-8 14:05:02
 * 该接口用于提供webService客户端方式可调用的方法--实现类的父接口
 */
public interface IServerInventoryStatus {

    /**
     * 康师傅前置机定制接口--库存状态变更
     *
     * @param inventory_status
     * @return
     */
    KSF_NoticeResult ksf_inventory_data(KSF_Inventory_Status inventory_status);


}
