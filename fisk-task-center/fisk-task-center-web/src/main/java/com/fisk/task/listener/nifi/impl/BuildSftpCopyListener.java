package com.fisk.task.listener.nifi.impl;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.factory.TaskSettingEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.utils.sftp.SftpUtils;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.TaskSettingDTO;
import com.fisk.task.dto.task.SftpCopyDTO;
import com.fisk.task.listener.nifi.ISftpCopyListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cfk
 */
@Component
@Slf4j
public class BuildSftpCopyListener implements ISftpCopyListener {

    @Resource
    private DataFactoryClient dataFactoryClient;

    @Resource
    private SftpUtils sftpUtils;

    @Override
    public ResultEnum sftpCopyTask(String data, Acknowledgment acke) {
        log.info("执行sftp文件复制参数:{}", data);
        data = "[" + data + "]";
        List<SftpCopyDTO> sftpCopys = JSON.parseArray(data, SftpCopyDTO.class);
        for (SftpCopyDTO sftpCopy : sftpCopys) {
            // 查具体的配置
            List<TaskSettingDTO> list = dataFactoryClient.getTaskSettingsByTaskId(Long.parseLong(sftpCopy.getTaskId()));
            Map<String, String> map = list.stream()
                    .collect(Collectors.toMap(TaskSettingDTO::getKey, TaskSettingDTO::getValue));
            // 执行sftp文件复制任务
            sftpUtils.copyFile(map.get(TaskSettingEnum.sftp_source_ip.getAttributeName()), 22,
                    map.get(TaskSettingEnum.sftp_source_account.getAttributeName()),
                    map.get(TaskSettingEnum.sftp_source_password.getAttributeName()),
                    map.get(TaskSettingEnum.sftp_source_authentication_type.getAttributeName()),
                    map.get(TaskSettingEnum.sftp_target_ip.getAttributeName()), 22,
                    map.get(TaskSettingEnum.sftp_target_account.getAttributeName()),
                    map.get(TaskSettingEnum.sftp_target_password.getAttributeName()),
                    map.get(TaskSettingEnum.sftp_target_authentication_type.getAttributeName()),
                    Integer.valueOf(map.get(TaskSettingEnum.sftp_source_sortord.getAttributeName())),
                    Integer.valueOf(map.get(TaskSettingEnum.sftp_source_ordering_rule.getAttributeName())),
                    Integer.valueOf(map.get(TaskSettingEnum.sftp_source_number.getAttributeName())),
                    map.get(TaskSettingEnum.sftp_source_folder.getAttributeName()),
                    map.get(TaskSettingEnum.sftp_target_folder.getAttributeName()),
                    map.get(TaskSettingEnum.sftp_target_file_name.getAttributeName()));
        }
        return ResultEnum.SUCCESS;
    }
}
