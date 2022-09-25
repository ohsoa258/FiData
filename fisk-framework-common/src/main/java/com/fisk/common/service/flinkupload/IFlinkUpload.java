package com.fisk.common.service.flinkupload;

import com.fisk.common.service.flinkupload.dto.FlinkUploadParameterDTO;

/**
 * @author JianWenYang
 */
public interface IFlinkUpload {

    /**
     * 提交任务脚本到flink
     *
     * @param dto
     */
    void submitJob(FlinkUploadParameterDTO dto);

}
