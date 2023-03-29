package com.fisk.mdm.utils.mdmBEBuild;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.enums.DataRuleEnum;
import com.fisk.mdm.utils.mdmBEBuild.impl.VaildityDataRuleImpl;

public class VaildityFactoryHelper {

    public static IVaildityDataRule VaildityDataRule(Integer dataRule){
        switch (dataRule){
            case 0:
//                return new BuildDataRuleImpl();
            case 1:
                return new VaildityDataRuleImpl();

            case 2:

            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}
