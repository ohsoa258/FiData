package com.fisk.task.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import com.fisk.task.config.SwaggerConfig;
import com.fisk.task.dto.daconfig.OverLoadCodeDTO;
import com.fisk.task.dto.pipeline.NifiStageDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import com.fisk.task.dto.task.TableTopicDTO;
import com.fisk.task.listener.pipeline.IPipelineTaskPublishCenter;
import com.fisk.task.service.nifi.INifiStage;
import com.fisk.task.service.nifi.IPipelineTableLog;
import com.fisk.task.service.pipeline.ITableTopicService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author cfk
 */
@Api(tags = {SwaggerConfig.PipelineTask})
@Slf4j
@RestController
@RequestMapping("/pipeline")
public class PipelineSupervisionController {

    @Resource
    IPipelineTableLog iPipelineTableLog;
    @Resource
    INifiStage iNifiStage;
    @Resource
    IPipelineTaskPublishCenter iPipelineTaskPublishCenter;
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
    public ResultEntity<List<NifiStageDTO>> getNifiStage(@RequestBody List<NifiCustomWorkflowDetailDTO> list) {
        ResultEntity<List<NifiStageDTO>> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data = iNifiStage.getNifiStage(list);
        objectResultEntity.code = 0;
        return objectResultEntity;
    }

    @PostMapping("/consumer")
    public void consumer(@RequestParam String message) {
        iPipelineTaskPublishCenter.msg(message, null);
    }

    @PostMapping("/updateTableTopicByComponentId")
    public void updateTableTopicByComponentId(@RequestBody TableTopicDTO tableTopicDTO) {
        iTableTopicService.updateTableTopicByComponentId(tableTopicDTO);
    }

    @PostMapping("/saveNifiStage")
    public void saveNifiStage(@RequestParam String data) {
        iNifiStage.saveNifiStage(data, null);
    }

    /**
     * 日志数据补全
     *
     * @return
     */
    @PostMapping("/getPipelineTableLog")
    public ResultEntity<List<PipelineTableLogVO>> getPipelineTableLog(@RequestParam("data") String data, @RequestParam("pipelineTableQuery") String pipelineTableQuery) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iPipelineTableLog.getPipelineTableLogs(data, pipelineTableQuery));
    }

    /**
     * 数据建模覆盖方式预览代码
     *
     * @param dto
     * @return
     */
    @PostMapping("/overlayCodePreview")
    public ResultEntity<Object> overlayCodePreview(@RequestBody OverLoadCodeDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iNifiStage.overlayCodePreview(dto));
    }

}
