package com.fisk.common.service.flinkupload;

import com.fisk.common.service.flinkupload.dto.FlinkUploadParameterDTO;

/**
 * @author JianWenYang
 */
public interface IFlinkCommand {

    /**
     * 构建flink命令脚本
     *
     * @param dto
     * @return
     */
    String buildFlinkCommand(FlinkUploadParameterDTO dto);

}
