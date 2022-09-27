package com.fisk.common.service.flinkupload;

import com.fisk.common.service.flinkupload.dto.FlinkUploadParameterDTO;

/**
 * @author JianWenYang
 */
public interface IFlinkJobUpload {

    /**
     * 提交任务脚本到flink
     *
     * @param dto
     * @return
     */
    String submitJob(FlinkUploadParameterDTO dto);

}
