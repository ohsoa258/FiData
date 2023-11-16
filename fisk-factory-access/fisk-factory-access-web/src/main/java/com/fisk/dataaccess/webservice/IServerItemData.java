package com.fisk.dataaccess.webservice;

import com.fisk.dataaccess.webservice.service.KSF_Item;
import com.fisk.dataaccess.webservice.service.KSF_NoticeResult;

/**
 * @author lsj
 * @date 2023-10-8 14:05:02
 * 该接口用于提供webService客户端方式可调用的方法--实现类的父接口
 */
public interface IServerItemData {

    /**
     * 康师傅前置机定制接口--物料主数据
     *
     * @param item
     * @return
     */
    KSF_NoticeResult ksf_item_data(KSF_Item item);

}
