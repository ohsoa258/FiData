package com.fisk.task.service.dispatchLog.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.entity.PipelTaskLogPO;
import com.fisk.task.mapper.PipelTaskLogMapper;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author cfk
 */
@Service
@Slf4j
public class PipelTaskLogImpl extends ServiceImpl<PipelTaskLogMapper, PipelTaskLogPO> implements IPipelTaskLog {
    @Override
    public void savePipelTaskLog(String jobTraceId, String pipelTaskTraceId, Map<Integer, Object> map, String taskId) {
        PipelTaskLogPO pipelTaskLog = new PipelTaskLogPO();
        List<PipelTaskLogPO> pipelTaskLogs = new ArrayList<>();
        Iterator<Map.Entry<Integer, Object>> nodeMap = map.entrySet().iterator();
        while (nodeMap.hasNext()) {
            pipelTaskLog.jobTraceId = jobTraceId;
            Map.Entry<Integer, Object> next = nodeMap.next();
            pipelTaskLog.msg = next.getValue().toString();
            pipelTaskLog.taskTraceId = pipelTaskTraceId;
            pipelTaskLog.taskId = taskId;
            pipelTaskLog.type = next.getKey();
            pipelTaskLogs.add(pipelTaskLog);
        }
        this.saveBatch(pipelTaskLogs);
    }

    @Override
    public PipelTaskLogPO getByPipelJobTraceId(String pipelJobTraceId, Long taskId) {
        List<PipelTaskLogPO> list = this.query().eq("job_trace_id", pipelJobTraceId)
                .eq("task_id", taskId).isNotNull("task_trace_id").orderByDesc("create_time").list();
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }
}
