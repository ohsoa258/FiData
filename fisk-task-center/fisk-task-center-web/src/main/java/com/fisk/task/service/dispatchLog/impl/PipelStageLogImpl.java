package com.fisk.task.service.dispatchLog.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.entity.PipelStageLogPO;
import com.fisk.task.mapper.PipelStateLogMapper;
import com.fisk.task.service.dispatchLog.IPipelStageLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author cfk
 */
@Service
@Slf4j
public class PipelStageLogImpl extends ServiceImpl<PipelStateLogMapper, PipelStageLogPO> implements IPipelStageLog {

    @Override
    public void savePipelTaskStageLog(String stateTraceId, String pipelTaskTraceId, Map<Integer, Object> map) {
        log.info("pipelStage参数:stateTraceId:{},pipelTaskTraceId:{},map:{}", stateTraceId, pipelTaskTraceId, JSON.toJSONString(map));

        List<PipelStageLogPO> pipelStageLogs = new ArrayList<>();
        Iterator<Map.Entry<Integer, Object>> nodeMap = map.entrySet().iterator();
        while (nodeMap.hasNext()) {
            PipelStageLogPO pipelStageLog = new PipelStageLogPO();
            Map.Entry<Integer, Object> next = nodeMap.next();
            if (Objects.isNull(next.getValue())) {
                continue;
            }
            pipelStageLog.msg = next.getValue().toString();
            pipelStageLog.stateTraceId = stateTraceId;
            pipelStageLog.taskTraceId = pipelTaskTraceId;
            pipelStageLog.type = next.getKey();
            pipelStageLogs.add(pipelStageLog);
        }
        if (pipelStageLogs.size() != 0) {
            this.saveBatch(pipelStageLogs);
        }

    }
}
