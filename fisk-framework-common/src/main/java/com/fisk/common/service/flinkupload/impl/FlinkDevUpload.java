package com.fisk.common.service.flinkupload.impl;

import com.fisk.common.service.flinkupload.IFlinkUpload;
import com.fisk.common.service.flinkupload.dto.FlinkUploadParameterDTO;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author JianWenYang
 */
@Slf4j
public class FlinkDevUpload implements IFlinkUpload {

    @Override
    public void submitJob(FlinkUploadParameterDTO dto) {
        Process process = null;
        //文件夹路径
        String path = "/var/www/uploadFileResource/temp";
        try {
            //执行命令
            process = Runtime.getRuntime().exec("/root/flink-1.14.0/bin/sql-client.sh -f /root/flink-job/sql/test");
            InputStreamReader ips = new InputStreamReader(process.getInputStream());
            BufferedReader br = new BufferedReader(ips);
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            log.error("FlinkDevUpload ex:", e);
        }
    }
}
