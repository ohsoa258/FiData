package com.fisk.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.entity.TaskDwDimPO;
import com.fisk.task.mapper.TaskDwDimMapper;
import com.fisk.task.service.ITaskDwDim;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaskDwDimImpl extends ServiceImpl<TaskDwDimMapper, TaskDwDimPO> implements ITaskDwDim {

}
