package com.fisk.datafactory.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.advice.ControllerAOPConfig;
import com.fisk.datafactory.config.SwaggerConfig;
import com.fisk.datafactory.dto.customworkflowdetail.TaskSettingDTO;
import com.fisk.datafactory.service.ITaskSetting;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author SongJianJian
 */
@Api(tags = {SwaggerConfig.TaskSetting})
@RestController
@RequestMapping("/taskSetting")
public class TaskSettingController {

    @Resource
    private ITaskSetting taskSetting;

    /**
     * 根据任务id查询任务配置信息列表
     *
     * @param taskId 任务id
     * @return
     */
    @GetMapping("/getTaskSettingsByTaskId/{taskId}")
    @ApiOperation(value = "根据任务id查询任务配置信息列表")
    public List<TaskSettingDTO> getTaskSettingsByTaskId(@PathVariable("taskId") long taskId) {
        return taskSetting.getTaskSettingsByTaskId(taskId);
    }

    @ApiOperation("上传文件")
    @PostMapping("/uploadFile")
    @ResponseBody
    @ControllerAOPConfig(printParams = false)
    public ResultEntity<Object> uploadFile(@RequestParam("pipelineId") Integer pipelineId,@RequestParam("taskId") Integer taskId, @RequestParam("file") MultipartFile file, @RequestParam("sourceOrTarget") int sourceOrTarget) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, taskSetting.uploadSecretKeyFile(pipelineId, taskId, file, sourceOrTarget));
    }
}
