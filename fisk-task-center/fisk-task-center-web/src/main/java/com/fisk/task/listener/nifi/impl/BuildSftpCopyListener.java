package com.fisk.task.listener.nifi.impl;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.enums.factory.TaskSettingEnum;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.sftp.SftpUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.customworkflowdetail.TaskSettingDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.TaskHierarchyDTO;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.ExecScriptDTO;
import com.fisk.task.dto.task.SftpCopyDTO;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.nifi.ISftpCopyListener;
import com.fisk.task.listener.pipeline.IPipelineTaskPublishCenter;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.utils.KafkaTemplateHelper;
import com.fisk.task.utils.StackTraceHelper;
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
    private IPipelJobLog iPipelJobLog;
    @Resource
    private KafkaTemplateHelper kafkaTemplateHelper;
    @Resource
    IPipelineTaskPublishCenter iPipelineTaskPublishCenter;

    @Override
    public ResultEnum sftpCopyTask(String data, Acknowledgment acke) {
        log.info("执行sftp文件复制参数:{}", data);
        data = "[" + data + "]";
        SftpCopyDTO dto = new SftpCopyDTO();
        try {


            List<SftpCopyDTO> sftpCopys = JSON.parseArray(data, SftpCopyDTO.class);

            for (SftpCopyDTO sftpCopy : sftpCopys) {
                dto = sftpCopy;
                TaskHierarchyDTO taskHierarchy = iPipelineTaskPublishCenter.getTaskHierarchy(sftpCopy.pipelTraceId, sftpCopy.taskId);
                NifiCustomWorkflowDetailDTO itselfPort = taskHierarchy.itselfPort;
                // 查具体的配置
                List<TaskSettingDTO> list = dataFactoryClient.getTaskSettingsByTaskId(itselfPort.pid);
                Map<String, String> map = list.stream()
                        .collect(Collectors.toMap(TaskSettingDTO::getSettingKey, TaskSettingDTO::getValue));
                // 执行sftp文件复制任务
                log.info("开始执行复制方法,连接配置:{}", JSON.toJSONString(map));
                SftpUtils.copyFile(
                        map.get(TaskSettingEnum.sftp_source_ip.getAttributeName()), 22,
                        map.get(TaskSettingEnum.sftp_source_account.getAttributeName()),
                        map.get(TaskSettingEnum.sftp_source_password.getAttributeName()),
                        map.get(TaskSettingEnum.sftp_source_rsa_file_path.getAttributeName()),
                        Integer.parseInt(map.get(TaskSettingEnum.sftp_source_authentication_type.getAttributeName())),

                        map.get(TaskSettingEnum.sftp_target_ip.getAttributeName()), 22,
                        map.get(TaskSettingEnum.sftp_target_account.getAttributeName()),
                        map.get(TaskSettingEnum.sftp_target_password.getAttributeName()),
                        map.get(TaskSettingEnum.sftp_target_rsa_file_path.getAttributeName()),
                        Integer.parseInt(map.get(TaskSettingEnum.sftp_target_authentication_type.getAttributeName())),

                        Integer.valueOf(map.get(TaskSettingEnum.sftp_source_ordering_rule.getAttributeName())),
                        Integer.valueOf(map.get(TaskSettingEnum.sftp_source_sortord.getAttributeName())),

                        Integer.valueOf(map.get(TaskSettingEnum.sftp_source_number.getAttributeName())),
                        map.get(TaskSettingEnum.sftp_source_folder.getAttributeName()),
                        map.get(TaskSettingEnum.sftp_target_folder.getAttributeName()),
                        map.get(TaskSettingEnum.sftp_target_file_name.getAttributeName()));
            }
            log.info("sftp复制任务执行完成");
            return ResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("执行sftp组件报错" + StackTraceHelper.getStackTraceInfo(e));
            DispatchExceptionHandlingDTO dispatchExceptionHandling = new DispatchExceptionHandlingDTO();
            dispatchExceptionHandling.pipelTraceId = dto.pipelTraceId;
            dispatchExceptionHandling.pipelTaskTraceId = dto.pipelTaskTraceId;
            dispatchExceptionHandling.pipelJobTraceId = dto.pipelJobTraceId;
            dispatchExceptionHandling.pipelStageTraceId = dto.pipelStageTraceId;
            dispatchExceptionHandling.comment = "执行脚本组件报错";
            iPipelJobLog.exceptionHandlingLog(dispatchExceptionHandling);
            throw new FkException(ResultEnum.ERROR);
        } finally {
            if (acke != null) {
                acke.acknowledge();
            }
            execScriptToDispatch(dto);

        }
    }

    /**
     * 调用管道下一级task
     *
     * @param dto dto
     * @author cfk
     * @date 2022/6/22 11:31
     */
    public void execScriptToDispatch(SftpCopyDTO dto) {
        log.info("开始组装结束信息:{}", JSON.toJSONString(dto));
        KafkaReceiveDTO kafkaReceiveDTO = KafkaReceiveDTO.builder().build();
        kafkaReceiveDTO.pipelTraceId = dto.pipelTraceId;
        kafkaReceiveDTO.pipelTaskTraceId = dto.pipelTaskTraceId;
        kafkaReceiveDTO.pipelStageTraceId = dto.pipelStageTraceId;
        kafkaReceiveDTO.pipelJobTraceId = dto.pipelJobTraceId;
        kafkaReceiveDTO.tableType = OlapTableEnum.SFTPFILECOPYTASK.getValue();
        NifiGetPortHierarchyDTO nifiGetPortHierarchy = new NifiGetPortHierarchyDTO();
        nifiGetPortHierarchy.nifiCustomWorkflowDetailId = Long.valueOf(dto.taskId);
        ResultEntity<TaskHierarchyDTO> nifiPortHierarchy = dataFactoryClient.getNifiPortHierarchy(nifiGetPortHierarchy);
        if (nifiPortHierarchy.code == ResultEnum.SUCCESS.getCode()) {
            TaskHierarchyDTO data = nifiPortHierarchy.data;
            kafkaReceiveDTO.topic = MqConstants.TopicPrefix.TOPIC_PREFIX + data.pipelineId + "." + OlapTableEnum.SFTPFILECOPYTASK.getValue() + ".0." + dto.taskId;
        } else {
            log.error("查找执行脚本任务失败2" + nifiPortHierarchy.msg);
        }
        kafkaReceiveDTO.nifiCustomWorkflowDetailId = Long.valueOf(dto.taskId);
        kafkaReceiveDTO.topicType = TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue();
        log.info("执行脚本任务完成,现在去往任务发布中心" + JSON.toJSONString(kafkaReceiveDTO));
        kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_TASK_OVER_FLOW, JSON.toJSONString(kafkaReceiveDTO));
    }
}
