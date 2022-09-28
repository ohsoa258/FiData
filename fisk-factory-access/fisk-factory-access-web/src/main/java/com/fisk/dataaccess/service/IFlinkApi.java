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
     * @param folder 文件夹
     * @return
     */
    String savePoints(String jobId, String folder);

    ResultEnum savePointsStatus(String jobId, String triggerId);

}
