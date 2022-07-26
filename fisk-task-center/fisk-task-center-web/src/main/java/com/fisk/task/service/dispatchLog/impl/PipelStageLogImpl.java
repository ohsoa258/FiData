package com.fisk.task.service.dispatchLog.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.dto.dispatchlog.PipelStageLogVO;
import com.fisk.task.entity.PipelStageLogPO;
import com.fisk.task.entity.PipelTaskLogPO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.mapper.PipelStateLogMapper;
import com.fisk.task.service.dispatchLog.IPipelStageLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author cfk
 */
@Service
@Slf4j
public class PipelStageLogImpl extends ServiceImpl<PipelStateLogMapper, PipelStageLogPO> implements IPipelStageLog {
    @Resource
    IPipelTaskLog iPipelTaskLog;
    @Resource
    PipelStateLogMapper pipelStateLogMapper;

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

    @Override
    public List<PipelStageLogVO> getPipelStageLogVos(String taskId) {
        //拿这个taskid,根据时间排序,taskTraceId分组,找到最近的三个taskTraceId
        List<PipelTaskLogPO> list = iPipelTaskLog.query().eq("task_id", taskId)
                .eq("del_flag", 1).select("task_trace_id").groupBy("task_trace_id").list();
        if (CollectionUtils.isNotEmpty(list)) {
            List<PipelStageLogVO> pipelStateLogs = pipelStateLogMapper.getPipelStateLogs(list.get(0).taskTraceId);
            pipelStateLogs.stream()
                    .filter(Objects::nonNull)
                    .forEach(f -> {
                        f.typeName = DispatchLogEnum.getName(f.type).getName();
                        f.msg = NifiStageTypeEnum.getName(Integer.parseInt(f.msg)).getName();
                    });
            return pipelStateLogs;

        }
        return null;
    }
}
