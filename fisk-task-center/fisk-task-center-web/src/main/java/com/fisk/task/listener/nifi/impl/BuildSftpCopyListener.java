package com.fisk.task.listener.nifi.impl;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.task.dto.task.SftpCopyDTO;
import com.fisk.task.listener.nifi.ISftpCopyListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author cfk
 */
@Component
@Slf4j
public class BuildSftpCopyListener implements ISftpCopyListener {


    @Override
    public ResultEnum sftpCopyTask(String data, Acknowledgment acke) {
        log.info("执行sftp文件复制参数:{}", data);
        data = "[" + data + "]";
        List<SftpCopyDTO> sftpCopys = JSON.parseArray(data, SftpCopyDTO.class);
        for (SftpCopyDTO sftpCopy : sftpCopys) {
            //查具体的配置




        }
        return null;
    }
}
