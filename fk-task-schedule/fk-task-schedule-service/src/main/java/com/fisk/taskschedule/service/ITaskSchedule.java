package com.fisk.taskschedule.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEntity;
import com.fisk.taskschedule.dto.TaskCronDTO;
import com.fisk.taskschedule.dto.TaskScheduleDTO;
import com.fisk.taskschedule.entity.TaskSchedulePO;

/**
 * @author Lock
 */
public interface ITaskSchedule extends IService<TaskSchedulePO> {

    /**
     * 添加
     * @param dto dto
     * @return 执行结果
     */
    ResultEntity<TaskCronDTO> addData(TaskScheduleDTO dto);

    /**
     * 修改
     * @param dto dto
     * @return 执行结果
     */
    ResultEntity<TaskCronDTO> editData(TaskScheduleDTO dto);

    /**
     * 查询当前分支的同步方式
     * @param dto dto
     * @return 查询结果
     */
    ResultEntity<TaskScheduleDTO> getData(TaskScheduleDTO dto);
}
