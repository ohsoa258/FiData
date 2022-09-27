package com.fisk.common.service.flinkupload.impl;

import com.fisk.common.service.flinkupload.FlinkFactoryHelper;
import com.fisk.common.service.flinkupload.FlinkUploadUtils;
import com.fisk.common.service.flinkupload.IFlinkCommand;
import com.fisk.common.service.flinkupload.IFlinkJobUpload;
import com.fisk.common.service.flinkupload.dto.FlinkUploadParameterDTO;
import com.jcraft.jsch.Session;

/**
 * @author JianWenYang
 */
public class FlinkSSHUpload implements IFlinkJobUpload {

    @Override
    public String submitJob(FlinkUploadParameterDTO dto) {
        IFlinkCommand flinkCommand = FlinkFactoryHelper.flinkCommand(dto.commandEnum);
        String command = flinkCommand.buildFlinkCommand(dto);
        Session session = FlinkUploadUtils.SSHConnection(dto.host, dto.port, dto.user, dto.password);
        assert session != null;
        String jobId = FlinkUploadUtils.ExecCommand(session, command);
        session.disconnect();
        return jobId;
    }

}
