package com.fisk.datafactory.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datafactory.config.SwaggerConfig;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.service.IPipelineProcessMonitor;
import com.fisk.task.dto.pipeline.NifiStageDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 管道流程监控
 * </p>
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/3/17 16:30
 */
@Api(tags = SwaggerConfig.PIPELINE_PROCESS_MONITOR)
@RestController
@RequestMapping("/pipeline")
public class PipelineProcessMonitorController {

    @Resource
    private IPipelineProcessMonitor service;

    @GetMapping("/getPipelineMonitorLogs/{workflow_id}")
    @ApiOperation(value = "根据管道workflow_id获取管道监控日志")
    public ResultEntity<List<PipelineTableLogDTO>> getPipelineMonitorLogs(@PathVariable("workflow_id") String workflowId) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getPipelineMonitorLogs(workflowId));
    }

    @PostMapping("/getNifiStage")
    public ResultEntity<NifiStageDTO> getNifiStage(@RequestBody NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getNifiStage(nifiCustomWorkflowDetailDTO));
    }

}
