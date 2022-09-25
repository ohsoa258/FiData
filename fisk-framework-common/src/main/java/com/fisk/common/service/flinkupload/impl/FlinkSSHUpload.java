package com.fisk.common.service.flinkupload.impl;

import com.fisk.common.service.flinkupload.FlinkUploadUtils;
import com.fisk.common.service.flinkupload.IFlinkUpload;
import com.fisk.common.service.flinkupload.dto.FlinkUploadParameterDTO;
import com.jcraft.jsch.Session;

/**
 * @author JianWenYang
 */
public class FlinkSSHUpload implements IFlinkUpload {

    @Override
    public void submitJob(FlinkUploadParameterDTO dto) {
        String command = "/root/flink-1.14.0/bin/sql-client.sh -f /root/flink-job/sql/test2";
        Session session = FlinkUploadUtils.SSHConnection(dto.host, dto.port, dto.user, dto.password);
        assert session != null;
        FlinkUploadUtils.ExecCommand(session, command);
        session.disconnect();
    }

}
