package com.fisk.datafactory.service;

import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.task.dto.pipeline.NifiStageDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogDTO;

import java.util.List;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/3/17 16:41
 */
public interface IPipelineProcessMonitor {
    /**
     * 根据管道id获取管道监控日志
     *
     * @param workflowId 管道workflowId(guid)
     * @return 管道中每个表的运行日志
     */
    List<PipelineTableLogDTO> getPipelineMonitorLogs(String workflowId);

    /**
     * 查询当前表的运行轨迹
     *
     * @param nifiCustomWorkflowDetailDTO 组件dto
     * @return 组件运行状态dto
     */
    NifiStageDTO getNifiStage(NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO);
}
