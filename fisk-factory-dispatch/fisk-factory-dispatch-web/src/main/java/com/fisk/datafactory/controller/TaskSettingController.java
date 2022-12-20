package com.fisk.datafactory.controller;

import com.fisk.datafactory.dto.customworkflowdetail.TaskSettingDTO;
import com.fisk.datafactory.service.ITaskSetting;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author SongJianJian
 */
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
    public List<TaskSettingDTO> getTaskSettingsByTaskId(@PathVariable("taskId") long taskId){
        return taskSetting.getTaskSettingsByTaskId(taskId);
    }
}
