package com.fisk.common.service.flinkupload.impl;

import com.fisk.common.service.flinkupload.FlinkUploadUtils;
import com.fisk.common.service.flinkupload.IFlinkJobUpload;
import com.fisk.common.service.flinkupload.dto.FlinkUploadParameterDTO;
import com.jcraft.jsch.Session;

/**
 * @author JianWenYang
 */
public class FlinkSSHUpload implements IFlinkJobUpload {

    @Override
    public String submitJob(FlinkUploadParameterDTO dto) {
        String command = "/root/flink-1.14.0/bin/sql-client.sh -f " + dto.uploadPath + dto.fileName;
        Session session = FlinkUploadUtils.SSHConnection(dto.host, dto.port, dto.user, dto.password);
        assert session != null;
        String jobId = FlinkUploadUtils.ExecCommand(session, command);
        session.disconnect();
        return jobId;
    }

}
