package com.fisk.dataaccess.service;

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

    /**
     * 检查点状态
     *
     * @param jobId
     * @param triggerId
     * @return
     */
    String savePointsStatus(String jobId, String triggerId);

}
