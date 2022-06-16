package com.fisk.task.service.dispatchLog.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.entity.PipelJobLogPO;
import com.fisk.task.mapper.PipelJobLogMapper;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author cfk
 */
@Service
@Slf4j
public class PipelJobLogImpl extends ServiceImpl<PipelJobLogMapper, PipelJobLogPO> implements IPipelJobLog {

    @Override
    public void savePipelLogAndJobLog(String pipelTraceId, Map<Integer, Object> map, String pipelId, String jobTraceId, String componentId) {
        log.info("job参数:pipelTraceId:{},map:{},pipelId:{},jobTraceId:{},componentId{}", pipelTraceId, JSON.toJSONString(map), pipelId, jobTraceId, componentId);

        List<PipelJobLogPO> pipelJobLogs = new ArrayList<>();
        Iterator<Map.Entry<Integer, Object>> nodeMap = map.entrySet().iterator();
        while (nodeMap.hasNext()) {
            PipelJobLogPO pipelJobLog = new PipelJobLogPO();
            Map.Entry<Integer, Object> next = nodeMap.next();
            if (Objects.isNull(next.getValue())) {
                continue;
            }
            pipelJobLog.msg = next.getValue().toString();
            pipelJobLog.pipelId = pipelId;
            pipelJobLog.jobTraceId = jobTraceId;
            pipelJobLog.pipelTraceId = pipelTraceId;
            pipelJobLog.type = next.getKey();
            pipelJobLog.componentId = componentId;
            pipelJobLogs.add(pipelJobLog);
        }
        if (pipelJobLogs.size() != 0) {
            this.saveBatch(pipelJobLogs);
        }

    }

    @Override
    public PipelJobLogPO getByPipelTraceId(String pipelTraceId, Long componentId) {
        List<PipelJobLogPO> list = this.query().eq("pipel_trace_id", pipelTraceId)
                .eq("component_id", componentId).isNotNull("job_trace_id").eq("del_flag", 1).orderByDesc("create_time").list();
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }
}
