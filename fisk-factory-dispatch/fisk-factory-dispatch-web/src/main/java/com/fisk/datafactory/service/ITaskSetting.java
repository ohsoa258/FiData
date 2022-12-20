package com.fisk.datafactory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.dto.customworkflowdetail.TaskSettingDTO;
import com.fisk.datafactory.entity.TaskSettingPO;

import java.util.List;
import java.util.Map;

/**
 * @author cfk
 */
public interface ITaskSetting extends IService<TaskSettingPO> {
    /**
     * 按照任务id删除配置
     * @param taskId
     * @return
     */
    ResultEnum deleteByTaskId(long taskId);

    /**
     * 更新任务配置
     * @param taskId
     * @param taskSetting
     * @return
     */
    ResultEnum updateTaskSetting(long taskId, Map<String,String> taskSetting);

    /**
     * 按照任务id查询当前任务下的配置数据列表
     * @param taskId
     * @return
     */
    List<TaskSettingDTO> getTaskSettingsByTaskId(long taskId);
}
