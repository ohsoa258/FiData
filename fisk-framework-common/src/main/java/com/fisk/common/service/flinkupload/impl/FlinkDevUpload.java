package com.fisk.common.service.flinkupload.impl;

import com.fisk.common.service.flinkupload.FlinkFactoryHelper;
import com.fisk.common.service.flinkupload.IFlinkCommand;
import com.fisk.common.service.flinkupload.IFlinkJobUpload;
import com.fisk.common.service.flinkupload.dto.FlinkUploadParameterDTO;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author JianWenYang
 */
@Slf4j
public class FlinkDevUpload implements IFlinkJobUpload {

    @Override
    public String submitJob(FlinkUploadParameterDTO dto) {
        Process process = null;
        String jobId = null;
        try {
            IFlinkCommand flinkCommand = FlinkFactoryHelper.flinkCommand(dto.commandEnum);
            String command = flinkCommand.buildFlinkCommand(dto);
            //执行命令
            process = Runtime.getRuntime().exec(command);
            InputStreamReader ips = new InputStreamReader(process.getInputStream());
            BufferedReader br = new BufferedReader(ips);
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                line = line.replaceAll(" ", "");
                if (line.indexOf("JobID") > -1) {
                    jobId = line.split(":")[1];
                }
            }
        } catch (IOException e) {
            log.error("FlinkDevUpload ex:", e);
        }
        return jobId;
    }
}
