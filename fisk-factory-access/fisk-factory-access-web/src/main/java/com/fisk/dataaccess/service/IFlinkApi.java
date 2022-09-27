package com.fisk.dataaccess.service;

import com.fisk.common.core.response.ResultEnum;

/**
 * @author JianWenYang
 */
public interface IFlinkApi {

    /**
     * 保存检查点
     *
     * @param jobId
     * @return
     */
    String savePoints(String jobId);

    ResultEnum savePointsStatus(String jobId, String triggerId);

}
