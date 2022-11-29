package com.fisk.task.service.dispatchLog.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.task.dto.dispatchlog.PipelTaskLogVO;
import com.fisk.task.dto.dispatchlog.PipelTaskMergeLogVO;
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
                    continue;
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
    public List<PipelTaskMergeLogVO> getPipelTaskLogVos(List<PipelTaskLogVO> pipelTaskLogs) {
        List<PipelTaskLogVO> pipelTaskLogVos = new ArrayList<>();
        for (PipelTaskLogVO pipelTaskLog : pipelTaskLogs) {
            List<PipelTaskLogVO> byTaskId = pipelTaskLogMapper.getByTaskId(pipelTaskLog.taskId, pipelTaskLog.jobTraceId);
            byTaskId.stream().filter(Objects::nonNull)
                    .sorted(Comparator.comparing(PipelTaskLogVO::getCreateTime).reversed())
                    //.filter(e -> e.taskId.equalsIgnoreCase(pipelTaskLog.taskId))
                    .forEach(f -> {
                        f.taskName = pipelTaskLog.taskName;
                        f.tableName = pipelTaskLog.tableName;
                        f.tableId = pipelTaskLog.tableId;
                        f.typeName = DispatchLogEnum.getName(f.type).getName();
                    });
            //等于一个新对象

            pipelTaskLogVos.addAll(JSON.parseArray(JSON.toJSONString(byTaskId), PipelTaskLogVO.class));
        }
        pipelTaskLogVos.sort((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()));
        Collections.reverse(pipelTaskLogVos);
        //todo task日志,两条合成一条
        //转出一份备份
        List<PipelTaskMergeLogVO> pipelTaskMergeLogVos = new ArrayList<>();
        List<PipelTaskLogVO> logs = JSON.parseArray(JSON.toJSONString(pipelTaskLogVos), PipelTaskLogVO.class);
        //根据pipelTraceId去重,除了开始
        for (int i = 0; i < logs.size() - 1; i++) {
            for (int j = logs.size() - 1; j > i; j--) {
                if (logs.get(j).taskTraceId.equals(logs.get(i).taskTraceId) && !logs.get(j).msg.contains("开始运行")) {
                    logs.remove(j);
                }
            }
        }
        Collections.reverse(logs);
        for (int i = 0; i < logs.size() - 1; i++) {
            for (int j = logs.size() - 1; j > i; j--) {
                if (logs.get(j).taskTraceId.equals(logs.get(i).taskTraceId) && logs.get(j).msg.contains("开始运行")) {
                    logs.remove(j);
                }
            }
        }
        Collections.reverse(logs);
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < logs.size(); i++) {
            PipelTaskMergeLogVO pipelTaskMergeLogVo = new PipelTaskMergeLogVO();
            PipelTaskLogVO pipelTaskLogVo = logs.get(i);
            pipelTaskMergeLogVo.jobTraceId = pipelTaskLogVo.jobTraceId;
            pipelTaskMergeLogVo.taskTraceId = pipelTaskLogVo.taskTraceId;
            pipelTaskMergeLogVo.taskId = pipelTaskLogVo.taskId;
            pipelTaskMergeLogVo.taskName = pipelTaskLogVo.taskName;
            pipelTaskMergeLogVo.tableId = pipelTaskLogVo.tableId;
            pipelTaskMergeLogVo.tableName = pipelTaskLogVo.tableName;
            try {
                if (Objects.equals(pipelTaskLogVo.type, DispatchLogEnum.taskstart.getValue())) {
                    pipelTaskMergeLogVo.startTime = simpleDate.parse(pipelTaskLogVo.msg.substring(7, 26));
                    pipelTaskMergeLogVo.createTime = pipelTaskLogVo.createTime;
                } else if (Objects.equals(pipelTaskLogVo.type, DispatchLogEnum.taskend.getValue())) {
                    pipelTaskMergeLogVo.endTime = simpleDate.parse(pipelTaskLogVo.msg.substring(7, 26));
                    pipelTaskMergeLogVo.msg = pipelTaskLogVo.msg;
                }
            } catch (ParseException e) {
                log.error("转换时间异常", StackTraceHelper.getStackTraceInfo(e));
            }
            for (int j = 0; j < logs.size(); j++) {
                if (Objects.equals(logs.get(i).taskTraceId, logs.get(j).taskTraceId) && !Objects.equals(logs.get(i).msg, logs.get(j).msg)) {
                    try {
                        PipelTaskLogVO pipelTaskLog = logs.get(j);
                        if (Objects.equals(pipelTaskLog.type, DispatchLogEnum.taskstart.getValue())) {
                            pipelTaskMergeLogVo.startTime = simpleDate.parse(pipelTaskLog.msg.substring(7, 26));
                            pipelTaskMergeLogVo.createTime = pipelTaskLog.createTime;
                        } else if (Objects.equals(pipelTaskLog.type, DispatchLogEnum.taskend.getValue())) {
                            pipelTaskMergeLogVo.endTime = simpleDate.parse(pipelTaskLog.msg.substring(7, 26));
                            pipelTaskMergeLogVo.msg = pipelTaskLogVo.msg;
                        }
                    } catch (ParseException e) {
                        log.error("转换时间异常", StackTraceHelper.getStackTraceInfo(e));
                    }
                    pipelTaskMergeLogVos.add(pipelTaskMergeLogVo);
                }
            }
            if (Objects.nonNull(pipelTaskMergeLogVo.endTime) && Objects.nonNull(pipelTaskMergeLogVo.startTime)) {
                pipelTaskMergeLogVo.duration = (pipelTaskMergeLogVo.endTime.getTime() - pipelTaskMergeLogVo.startTime.getTime()) / 60000;
                pipelTaskMergeLogVos.add(pipelTaskMergeLogVo);
            }
            if (Objects.isNull(pipelTaskMergeLogVo.endTime) && Objects.nonNull(pipelTaskMergeLogVo.startTime)) {
                pipelTaskMergeLogVos.add(pipelTaskMergeLogVo);
            }

        }
        for (int i = 0; i < pipelTaskMergeLogVos.size() - 1; i++) {
            for (int j = pipelTaskMergeLogVos.size() - 1; j > i; j--) {
                if (pipelTaskMergeLogVos.get(j).taskTraceId.equals(pipelTaskMergeLogVos.get(i).taskTraceId)) {
                    pipelTaskMergeLogVos.remove(j);
                }
            }
        }


        return pipelTaskMergeLogVos;
    }


}
