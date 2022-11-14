package com.fisk.task.service.dispatchLog.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.dto.dispatchlog.PipelLogVO;
import com.fisk.task.entity.PipelLogPO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.mapper.PipelLogMapper;
import com.fisk.task.service.dispatchLog.IPipelLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author cfk
 */
@Service
@Slf4j
public class PipelLogImpl extends ServiceImpl<PipelLogMapper, PipelLogPO> implements IPipelLog {

    @Resource
    PipelLogMapper pipelLogMapper;

    @Override
    public void savePipelLog(String pipelTraceId, Map<Integer, Object> map, String pipelId) {
        log.info("job参数:pipelTraceId:{},map:{},pipelId:{}", pipelTraceId, JSON.toJSONString(map), pipelId);

        List<PipelLogPO> pipelLogs = new ArrayList<>();
        Iterator<Map.Entry<Integer, Object>> nodeMap = map.entrySet().iterator();
        while (nodeMap.hasNext()) {
            PipelLogPO pipelLog = new PipelLogPO();
            Map.Entry<Integer, Object> next = nodeMap.next();
            if (Objects.isNull(next.getValue())) {
                continue;
            }
            pipelLog.msg = next.getValue().toString();
            pipelLog.pipelId = pipelId;
            pipelLog.pipelTraceId = pipelTraceId;
            pipelLog.type = next.getKey();
            pipelLogs.add(pipelLog);
        }
        if (pipelLogs.size() != 0) {
            this.saveBatch(pipelLogs);
        }
    }

    @Override
    public List<PipelLogVO> getPipelLogVos(PipelLogVO pipelLog) {
        List<PipelLogVO> list = pipelLogMapper.getPipelLogs(pipelLog);
        list.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(PipelLogVO::getCreateTime).reversed())
                .forEach(f -> {
                    f.typeName = DispatchLogEnum.getName(f.type).getName();
                    f.pipelName = pipelLog.pipelName;
                });
        return list;
    }
}
