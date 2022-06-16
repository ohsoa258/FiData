package com.fisk.task.service.dispatchLog.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.entity.PipelStageLogPO;
import com.fisk.task.mapper.PipelStateLogMapper;
import com.fisk.task.service.dispatchLog.IPipelStageLog;
import lombok.extern.slf4j.Slf4j;
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
public class PipelStageLogImpl extends ServiceImpl<PipelStateLogMapper, PipelStageLogPO> implements IPipelStageLog {

    @Override
    public void savePipelTaskStageLog(String stateTraceId, String pipelTaskTraceId, Map<Integer, Object> map) {
        PipelStageLogPO pipelStageLog = new PipelStageLogPO();
        List<PipelStageLogPO> pipelStageLogs = new ArrayList<>();
        Iterator<Map.Entry<Integer, Object>> nodeMap = map.entrySet().iterator();
        while (nodeMap.hasNext()) {
            Map.Entry<Integer, Object> next = nodeMap.next();
            pipelStageLog.msg = next.getValue().toString();
            pipelStageLog.stateTraceId = stateTraceId;
            pipelStageLog.taskTraceId = pipelTaskTraceId;
            pipelStageLog.type = next.getKey();
            pipelStageLogs.add(pipelStageLog);
        }
        this.saveBatch(pipelStageLogs);
    }
}
