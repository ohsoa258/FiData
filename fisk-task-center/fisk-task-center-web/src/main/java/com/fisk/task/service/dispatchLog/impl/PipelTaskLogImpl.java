package com.fisk.task.service.dispatchLog.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.task.dto.dispatchlog.PipelTaskLogVO;
import com.fisk.task.entity.PipelTaskLogPO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.mapper.PipelTaskLogMapper;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @author cfk
 */
@Service
@Slf4j
public class PipelTaskLogImpl extends ServiceImpl<PipelTaskLogMapper, PipelTaskLogPO> implements IPipelTaskLog {

    @Resource
    PipelTaskLogMapper pipelTaskLogMapper;

    @Override
    public void savePipelTaskLog(String jobTraceId, String pipelTaskTraceId, Map<Integer, Object> map, String taskId, String tableId, int tableType) {
        log.info("PipelTask参数:jobTraceId:{},pipelTaskTraceId:{},map:{},taskId:{}", jobTraceId, pipelTaskTraceId, JSON.toJSONString(map), taskId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<PipelTaskLogPO> pipelTaskLogs = new ArrayList<>();
        Iterator<Map.Entry<Integer, Object>> nodeMap = map.entrySet().iterator();
        Date entryDate = new Date();
        while (nodeMap.hasNext()) {
            PipelTaskLogPO pipelTaskLog = new PipelTaskLogPO();
            pipelTaskLog.jobTraceId = jobTraceId;
            Map.Entry<Integer, Object> next = nodeMap.next();
            if (Objects.isNull(next.getValue())) {
                continue;
            }
            if (next.getKey() == DispatchLogEnum.entrydate.getValue()) {
                try {
                    entryDate = sdf.parse(next.getValue().toString());
                } catch (Exception e) {
                    log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
                    throw new FkException(ResultEnum.ERROR);
                }
            }
            pipelTaskLog.msg = next.getValue().toString();
            pipelTaskLog.taskTraceId = pipelTaskTraceId;
            pipelTaskLog.taskId = taskId;
            pipelTaskLog.type = next.getKey();
            if (StringUtils.isNotEmpty(tableId)) {
                pipelTaskLog.tableId = Integer.valueOf(tableId);
            }
            pipelTaskLog.tableType = tableType;
            pipelTaskLogs.add(pipelTaskLog);
        }
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = entryDate.toInstant().atZone(zoneId).toLocalDateTime();
        pipelTaskLogs.stream().filter(Objects::nonNull)
                .forEach(e -> {
                    e.createTime = localDateTime;
                });
        if (pipelTaskLogs.size() != 0) {
            this.saveBatch(pipelTaskLogs);
        }

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

    @Override
    public List<PipelTaskLogVO> getPipelTaskLogVos(List<PipelTaskLogVO> pipelTaskLogs) {
        List<PipelTaskLogVO> pipelTaskLogVos = new ArrayList<>();
        for (PipelTaskLogVO pipelTaskLog : pipelTaskLogs) {
            List<PipelTaskLogVO> byTaskId = pipelTaskLogMapper.getByTaskId(pipelTaskLog.taskId);
            byTaskId.stream().filter(Objects::nonNull)
                    //.filter(e -> e.taskId.equalsIgnoreCase(pipelTaskLog.taskId))
                    .forEach(f -> {
                        f.taskName = pipelTaskLog.taskName;
                        f.tableName = pipelTaskLog.tableName;
                        f.tableId = pipelTaskLog.tableId;
                        f.typeName = DispatchLogEnum.getName(f.type).getName();
                    });
            pipelTaskLogVos.addAll(byTaskId);
        }
        return pipelTaskLogVos;
    }


}
