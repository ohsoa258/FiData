package com.fisk.task.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import com.fisk.task.dto.pipeline.NifiStageDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import com.fisk.task.dto.query.PipelineTableQueryDTO;
import com.fisk.task.dto.task.TableTopicDTO;
import com.fisk.task.listener.pipeline.IBuildPipelineSupervisionListener;
import com.fisk.task.service.nifi.INifiStage;
import com.fisk.task.service.nifi.IPipelineTableLog;
import com.fisk.task.service.pipeline.ITableTopicService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    @Resource
    IBuildPipelineSupervisionListener iBuildPipelineSupervisionListener;
    @Resource
    ITableTopicService iTableTopicService;

    @PostMapping("/getPipelineTableLogs")
    public ResultEntity<List<PipelineTableLogDTO>> getPipelineTableLogs(@RequestBody List<NifiCustomWorkflowDetailDTO> nifiCustomWorkflowDetailDTO) {
        ResultEntity<List<PipelineTableLogDTO>> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data = iPipelineTableLog.getPipelineTableLogs(nifiCustomWorkflowDetailDTO);
        objectResultEntity.code = 0;
        return objectResultEntity;
    }

    @PostMapping("/getNifiCustomWorkflowDetails")
    public ResultEntity<List<NifiCustomWorkflowVO>> getNifiCustomWorkflowDetails(@RequestBody List<NifiCustomWorkflowVO> nifiCustomWorkflows) {
        ResultEntity<List<NifiCustomWorkflowVO>> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data = iPipelineTableLog.getNifiCustomWorkflowDetails(nifiCustomWorkflows);
        objectResultEntity.code = 0;
        return objectResultEntity;
    }

    @PostMapping("/getNifiStage")
    public ResultEntity<NifiStageDTO> getNifiStage(@RequestBody NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO) {
        ResultEntity<NifiStageDTO> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data = iNifiStage.getNifiStage(nifiCustomWorkflowDetailDTO);
        objectResultEntity.code = 0;
        return objectResultEntity;
    }

    @PostMapping("/consumer")
    public void consumer(@RequestBody List<String> arrMessage) {
        iBuildPipelineSupervisionListener.msg(arrMessage, null);
    }

    @PostMapping("/updateTableTopicByComponentId")
    public void updateTableTopicByComponentId(@RequestBody TableTopicDTO tableTopicDTO) {
        iTableTopicService.updateTableTopicByComponentId(tableTopicDTO);
    }

    @PostMapping("/saveNifiStage")
    public void saveNifiStage(@RequestParam String data) {
        iNifiStage.saveNifiStage(data, null);
    }

/*    @ApiOperation(value = "筛选器")
    @PostMapping("/pageFilter")
    public ResultEntity<Page<PipelineTableLogVO>> pageFilter(@RequestBody PipelineTableQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iPipelineTableLog.pageFilter(dto));
    }*/

}
