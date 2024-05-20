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

    /**
     * 血缘补偿  按tb_meta_sync_time表的同步时间增量
     *
     * @param moduleId
     * @return
     */
    ResultEnum systemSynchronousBloodV2(List<Integer> moduleId);

}
