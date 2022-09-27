package com.fisk.common.service.flinkupload.impl;

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
            //执行命令
            process = Runtime.getRuntime().exec("/root/flink-1.14.0/bin/sql-client.sh -f " + dto.uploadPath + "/" + dto.fileName);
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
