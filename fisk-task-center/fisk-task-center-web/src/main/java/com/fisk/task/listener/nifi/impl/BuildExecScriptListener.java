package com.fisk.task.listener.nifi.impl;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.task.ExecScriptDTO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.TaskHierarchyDTO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.nifi.IExecScriptListener;
import com.fisk.task.listener.pipeline.IPipelineTaskPublishCenter;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.utils.KafkaTemplateHelper;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

/**
 * @author cfk
 */
@Component
@Slf4j
public class BuildExecScriptListener implements IExecScriptListener {
    @Resource
    DataFactoryClient dataFactoryClient;
    @Resource
    UserClient userClient;
    @Resource
    IPipelineTaskPublishCenter iPipelineTaskPublishCenter;
    @Resource
    KafkaTemplateHelper kafkaTemplateHelper;
    @Resource
    IPipelJobLog iPipelJobLog;


    @Override
    public ResultEnum execScript(String data, Acknowledgment acke) {
        ExecScriptDTO exec = new ExecScriptDTO();
        try {
            log.info("执行调度脚本参数:{}", data);
            data = "[" + data + "]";
            List<ExecScriptDTO> execScripts = JSON.parseArray(data, ExecScriptDTO.class);
            for (ExecScriptDTO execScript : execScripts) {
                exec = execScript;
                ResultEntity<NifiCustomWorkflowDetailDTO> dto = dataFactoryClient.getData(Long.parseLong(execScript.taskId));
                if (Objects.equals(dto.code, ResultEnum.SUCCESS.getCode())) {
                    NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetail = dto.data;
                    Integer dataSourceId = nifiCustomWorkflowDetail.dataSourceId;
                    String customScript = nifiCustomWorkflowDetail.customScript;
                    ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(dataSourceId);
                    if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
                        DataSourceDTO dataSource = fiDataDataSource.data;
                        Connection conn = null;
                        Statement st = null;
                        try {
                            Class.forName(dataSource.conType.getDriverName());
                            conn = DriverManager.getConnection(dataSource.conStr, dataSource.conAccount, dataSource.conPassword);
                            st = conn.createStatement();
                            //无需判断ddl语句执行结果,因为如果执行失败会进catch
                            log.info("开始执行脚本:{}", customScript);
                            st.execute(customScript);
                        } catch (Exception e) {
                            log.error(StackTraceHelper.getStackTraceInfo(e));
                        } finally {
                            try {
                                st.close();
                                conn.close();
                            } catch (SQLException e) {
                                log.error(StackTraceHelper.getStackTraceInfo(e));
                            }

                        }
                    } else {
                        log.error("userclient无法查询到目标库的连接信息");
                        throw new FkException(ResultEnum.ERROR);
                    }
                } else {
                    log.error("查询执行脚本组件报错1");
                    throw new FkException(ResultEnum.ERROR);
                }
            }
            return ResultEnum.SUCCESS;
        } catch (Exception e) {
            DispatchExceptionHandlingDTO dto = new DispatchExceptionHandlingDTO();
            dto.pipelTraceId = exec.pipelTraceId;
            dto.pipelTaskTraceId = exec.pipelTaskTraceId;
            dto.pipelJobTraceId = exec.pipelJobTraceId;
            dto.pipelStageTraceId = exec.pipelStageTraceId;
            dto.comment = "执行脚本组件报错";
            iPipelJobLog.exceptionHandlingLog(dto);
            log.error("执行脚本组件报错" + StackTraceHelper.getStackTraceInfo(e));
            throw new FkException(ResultEnum.ERROR);
        } finally {
            if (acke != null) {
                execScriptToDispatch(exec);
                acke.acknowledge();
            }
        }
    }

    /**
     * 调用管道下一级task
     *
     * @param dto dto
     * @author cfk
     * @date 2022/6/22 11:31
     */
    public void execScriptToDispatch(ExecScriptDTO dto) {
        KafkaReceiveDTO kafkaReceiveDTO = KafkaReceiveDTO.builder().build();
        kafkaReceiveDTO.pipelTraceId = dto.pipelTraceId;
        kafkaReceiveDTO.pipelTaskTraceId = dto.pipelTaskTraceId;
        kafkaReceiveDTO.pipelStageTraceId = dto.pipelStageTraceId;
        kafkaReceiveDTO.pipelJobTraceId = dto.pipelJobTraceId;
        kafkaReceiveDTO.tableType = OlapTableEnum.CUSTOMIZESCRIPT.getValue();
        NifiGetPortHierarchyDTO nifiGetPortHierarchy = new NifiGetPortHierarchyDTO();
        nifiGetPortHierarchy.nifiCustomWorkflowDetailId = Long.valueOf(dto.taskId);
        ResultEntity<TaskHierarchyDTO> nifiPortHierarchy = dataFactoryClient.getNifiPortHierarchy(nifiGetPortHierarchy);
        if (nifiPortHierarchy.code == ResultEnum.SUCCESS.getCode()) {
            TaskHierarchyDTO data = nifiPortHierarchy.data;
            kafkaReceiveDTO.topic = MqConstants.TopicPrefix.TOPIC_PREFIX + data.pipelineId + "." + OlapTableEnum.CUSTOMIZESCRIPT.getValue() + ".0." + dto.taskId;
        } else {
            log.error("查找执行脚本任务失败2" + nifiPortHierarchy.msg);
        }
        kafkaReceiveDTO.nifiCustomWorkflowDetailId = Long.valueOf(dto.taskId);
        kafkaReceiveDTO.topicType = TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue();
        log.info("执行脚本任务完成,现在去往任务发布中心" + JSON.toJSONString(kafkaReceiveDTO));
        kafkaTemplateHelper.sendMessageAsync("task.build.task.over", JSON.toJSONString(kafkaReceiveDTO));
    }
}
