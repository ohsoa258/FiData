package com.fisk.common.service.flinkupload.impl;

import com.fisk.common.service.flinkupload.IFlinkCommand;
import com.fisk.common.service.flinkupload.dto.FlinkUploadParameterDTO;
import lombok.extern.slf4j.Slf4j;

/**
 * @author JianWenYang
 */
@Slf4j
public class FlinkDevCommand implements IFlinkCommand {

    @Override
    public String buildFlinkCommand(FlinkUploadParameterDTO dto) {
        StringBuilder str = new StringBuilder();
        str.append(dto.commandPath);
        str.append(" -f ");
        str.append(dto.uploadPath + dto.fileName);
        return str.toString();
    }

}
