package com.fisk.task.service.nifi;

import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import com.fisk.task.entity.PipelineTableLogPO;

import java.util.List;

public interface IPipelineTableLog {

    /**
     * 获取一张表的日志与状态
     *
     * @param nifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO
     * @return PipelineTableLogPO
     */
    PipelineTableLogPO getPipelineTableLog(NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO);


    /**
     * 获取一个管道所有表的日志与状态
     *
     * @param nifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO
     * @return PipelineTableLogPO
     */
    List<PipelineTableLogPO> getPipelineTableLogs(List<NifiCustomWorkflowDetailDTO> nifiCustomWorkflowDetailDTO);



    /**
     * 获取一个管道呼吸灯的状态
     *
     * @param nifiCustomWorkflowVO nifiCustomWorkflowVO
     * @return NifiStagePO
     */
    NifiCustomWorkflowVO getNifiCustomWorkflowDetail(NifiCustomWorkflowVO nifiCustomWorkflowVO);

    /**
     * 获取一个管道呼吸灯的状态
     *
     * @param nifiCustomWorkflows nifiCustomWorkflows
     * @return NifiStagePO
     */
    List<NifiCustomWorkflowVO> getNifiCustomWorkflowDetails(List<NifiCustomWorkflowVO> nifiCustomWorkflows);


}
