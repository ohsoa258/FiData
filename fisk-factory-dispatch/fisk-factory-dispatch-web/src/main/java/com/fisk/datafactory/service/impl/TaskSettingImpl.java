package com.fisk.datafactory.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datafactory.entity.TaskSettingPO;
import com.fisk.datafactory.mapper.TaskSettingMapper;
import com.fisk.datafactory.service.ITaskSetting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author cfk
 */
@Slf4j
@Service
public class TaskSettingImpl extends ServiceImpl<TaskSettingMapper, TaskSettingPO> implements ITaskSetting {


}
