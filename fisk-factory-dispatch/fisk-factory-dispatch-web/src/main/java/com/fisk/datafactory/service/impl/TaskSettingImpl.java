package com.fisk.datafactory.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.entity.TaskSettingPO;
import com.fisk.datafactory.mapper.TaskSettingMapper;
import com.fisk.datafactory.service.ITaskSetting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author cfk
 */
@Slf4j
@Service
public class TaskSettingImpl extends ServiceImpl<TaskSettingMapper, TaskSettingPO> implements ITaskSetting {

    @Resource
    TaskSettingMapper taskSettingMapper;

    @Override
    public ResultEnum deleteByTaskId(long taskId) {
        taskSettingMapper.deleteByTaskId(taskId);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum updateTaskSetting(long taskId, Map<String, String> taskSetting) {
        //map转list
        List<TaskSettingPO> list = new ArrayList<>();
        Iterator<Map.Entry<String, String>> nodeMap = taskSetting.entrySet().iterator();
        while (nodeMap.hasNext()) {
            Map.Entry<String, String> nodeEntry = nodeMap.next();
            String key = nodeEntry.getKey();
            String value = nodeEntry.getValue();
            TaskSettingPO taskSettingPo = new TaskSettingPO();
            taskSettingPo.key = key;
            taskSettingPo.value = value;
            taskSettingPo.taskId = String.valueOf(taskId);
            list.add(JSON.parseObject(JSON.toJSONString(taskSettingPo), TaskSettingPO.class));
        }
        taskSettingMapper.deleteByTaskId(taskId);
        this.saveBatch(list);
        return ResultEnum.SUCCESS;
    }
}
