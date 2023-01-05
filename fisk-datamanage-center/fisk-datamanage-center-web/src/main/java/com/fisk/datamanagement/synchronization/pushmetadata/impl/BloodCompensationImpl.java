package com.fisk.datamanagement.synchronization.pushmetadata.impl;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.synchronization.pushmetadata.IBloodCompensation;

/**
 * @author JianWenYang
 */
public class BloodCompensationImpl
        implements IBloodCompensation {

    @Override
    public ResultEnum systemSynchronousBlood() {


        return ResultEnum.SUCCESS;
    }

}
