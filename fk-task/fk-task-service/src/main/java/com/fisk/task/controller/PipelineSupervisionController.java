package com.fisk.task.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import com.fisk.task.dto.pipeline.NifiStageDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogDTO;
import com.fisk.task.service.nifi.INifiStage;
import com.fisk.task.service.nifi.IPipelineTableLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
/**
 * @author cfk
 */
@Slf4j
@RestController
@RequestMapping("/pipeline")
public class PipelineSupervisionController {

    @Resource
    IPipelineTableLog iPipelineTableLog;
    @Resource
    INifiStage iNifiStage;

    @PostMapping("/getPipelineTableLogs")
    public ResultEntity<List<PipelineTableLogDTO>> getPipelineTableLogs(@RequestBody List<NifiCustomWorkflowDetailDTO> nifiCustomWorkflowDetailDTO) {
        ResultEntity<List<PipelineTableLogDTO>> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data= iPipelineTableLog.getPipelineTableLogs(nifiCustomWorkflowDetailDTO);
        objectResultEntity.code=0;
        return objectResultEntity;
    }

    @PostMapping("/getNifiCustomWorkflowDetails")
    public ResultEntity<List<NifiCustomWorkflowVO>> getNifiCustomWorkflowDetails(@RequestBody List<NifiCustomWorkflowVO> nifiCustomWorkflows) {
        ResultEntity<List<NifiCustomWorkflowVO>> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data= iPipelineTableLog.getNifiCustomWorkflowDetails(nifiCustomWorkflows);
        objectResultEntity.code=0;
        return objectResultEntity;
    }

    @PostMapping("/getNifiStage")
    public ResultEntity<NifiStageDTO> getNifiStage(@RequestBody NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO) {
        ResultEntity<NifiStageDTO> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data= iNifiStage.getNifiStage(nifiCustomWorkflowDetailDTO);
        objectResultEntity.code=0;
        return objectResultEntity;
    }

}
