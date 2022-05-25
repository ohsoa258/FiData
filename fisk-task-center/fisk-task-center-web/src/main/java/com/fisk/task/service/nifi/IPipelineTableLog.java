package com.fisk.task.service.nifi;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import com.fisk.task.dto.pipeline.PipelineTableLogDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import com.fisk.task.dto.query.PipelineTableQueryDTO;
import com.fisk.task.entity.PipelineTableLogPO;

import java.util.List;

/**
 * @author cfk
 */
public interface IPipelineTableLog extends IService<PipelineTableLogPO> {

    /**
     * 获取一张表的日志与状态
     *
     * @param nifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO
     * @return PipelineTableLogPO
     */
    List<PipelineTableLogDTO> getPipelineTableLog(NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO);


    /**
     * 获取一个管道所有表的日志与状态
     *
     * @param nifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO
     * @return PipelineTableLogPO
     */
    List<PipelineTableLogDTO> getPipelineTableLogs(List<NifiCustomWorkflowDetailDTO> nifiCustomWorkflowDetailDTO);


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

    /**
     * 分页查询
     *
     * @return 日志列表
     */
    List<PipelineTableLogVO> getPipelineTableLogs(String data, String pipelineTableQuery);


}
