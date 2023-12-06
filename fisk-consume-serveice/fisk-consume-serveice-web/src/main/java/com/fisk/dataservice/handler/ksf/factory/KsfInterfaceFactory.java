package com.fisk.dataservice.handler.ksf.factory;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataservice.enums.SpecialTypeEnum;
import com.fisk.dataservice.handler.ksf.KsfWebServiceHandler;
import com.fisk.dataservice.handler.ksf.impl.KsfAcknowledgement;
import com.fisk.dataservice.handler.ksf.impl.KsfInventoryStatusChanges;
import com.fisk.dataservice.handler.ksf.impl.KsfItemData;
import com.fisk.dataservice.handler.ksf.impl.KsfNotice;

/**
 * @Author: wangjian
 * @Date: 2023-09-14
 * @Description:
 */
public class KsfInterfaceFactory {
    public static KsfWebServiceHandler getKsfWebServiceHandlerByType(SpecialTypeEnum specialTypeEnum) {
        switch (specialTypeEnum){
            case KSF_ITEM_DATA:
                return new KsfItemData();
            case KSF_NOTICE:
                return new KsfNotice();
            case KSF_INVENTORY_STATUS_CHANGES:
                return new KsfInventoryStatusChanges();
            case KSF_ACKNOWLEDGEMENT:
                return new KsfAcknowledgement();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}
