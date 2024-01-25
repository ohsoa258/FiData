package com.fisk.datamanagement.synchronization.pushmetadata;

import com.fisk.common.core.response.ResultEnum;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IBloodCompensation {

    /**
     * 系统同步血缘
     *
     * @return
     */
    ResultEnum systemSynchronousBlood(String currUserName,boolean initialization, List<Integer> moduleId);



}
