package com.fisk.task.service.dispatchLog.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.dispatchlog.PipelJobLogVO;
import com.fisk.task.entity.PipelJobLogPO;
import com.fisk.task.entity.PipelTaskLogPO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.mapper.PipelJobLogMapper;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelLog;
import com.fisk.task.service.dispatchLog.IPipelStageLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author cfk
 */
@Service
@Slf4j
public class PipelJobLogImpl extends ServiceImpl<PipelJobLogMapper, PipelJobLogPO> implements IPipelJobLog {
    @Resource
    PipelJobLogMapper pipelJobLogMapper;
    @Resource
    IPipelStageLog iPipelStageLog;
    @Resource
    IPipelTaskLog iPipelTaskLog;
    @Resource
    IPipelLog iPipelLog;

    @Override
    public void savePipelLog(String pipelTraceId, Map<Integer, Object> map, String pipelId) {
        log.info("job参数:pipelTraceId:{},map:{},pipelId:{}", pipelTraceId, JSON.toJSONString(map), pipelId);

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
            pipelJobLog.pipelTraceId = pipelTraceId;
            pipelJobLog.type = next.getKey();
            pipelJobLogs.add(pipelJobLog);
        }
        if (pipelJobLogs.size() != 0) {
            this.saveBatch(pipelJobLogs);
        }
    }

    @Override
    public void savePipelJobLog(String pipelTraceId, Map<Integer, Object> map, String pipelId, String jobTraceId, String componentId) {
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

    @Override
    public List<PipelJobLogVO> getPipelJobLogVos(List<PipelJobLogVO> pipelJobLogs) {
        List<PipelJobLogVO> pipelJobLogVos = new ArrayList<>();
        for (PipelJobLogVO pipelJobLog : pipelJobLogs) {
            List<PipelJobLogVO> pipelJobLogVo = pipelJobLogMapper.getPipelJobLogVo(pipelJobLog);
            pipelJobLogVo.stream()
                    .filter(Objects::nonNull)
                    .filter(e -> e.componentId != null && e.componentId.equalsIgnoreCase(pipelJobLog.componentId))
                    .forEach(f -> {
                        f.componentName = pipelJobLog.componentName;
                    });
            pipelJobLogVo.stream()
                    .filter(Objects::nonNull)
                    .forEach(f -> {
                        f.pipelName = pipelJobLog.pipelName;
                        f.typeName = DispatchLogEnum.getName(f.type).getName();
                    });
            pipelJobLogVos.addAll(pipelJobLogVo);

        }
        return pipelJobLogVos;
    }

    @Override
    public void exceptionHandlingLog(DispatchExceptionHandlingDTO dto) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //1.从小到大保存
        //任务日志
        if (!StringUtils.isEmpty(dto.pipelTaskTraceId)) {
            List<PipelTaskLogPO> list = iPipelTaskLog.query().eq("task_trace_id", dto.pipelTaskTraceId).orderByDesc("create_time").list();
            if (CollectionUtils.isNotEmpty(list)) {
                Map<Integer, Object> stageMap = new HashMap<>();
                stageMap.put(DispatchLogEnum.taskend.getValue(), simpleDateFormat.format(new Date()));
                stageMap.put(DispatchLogEnum.taskstate.getValue(), NifiStageTypeEnum.RUN_FAILED.getName());
                stageMap.put(DispatchLogEnum.taskcomment.getValue(), dto.comment);
                iPipelTaskLog.savePipelTaskLog(dto.pipelJobTraceId, dto.pipelTaskTraceId, stageMap, list.get(0).taskId, null, 0);
            }
        }
        //job日志
        if (!StringUtils.isEmpty(dto.pipelJobTraceId)) {
            List<PipelJobLogPO> list = this.query().eq("job_trace_id", dto.pipelJobTraceId).orderByDesc("create_time").list();
            if (CollectionUtils.isNotEmpty(list)) {
                PipelJobLogPO pipelJobLogPo = list.get(0);
                Map<Integer, Object> jobMap = new HashMap<>();
                //结束时间,job状态
                jobMap.put(DispatchLogEnum.jobend.getValue(), simpleDateFormat.format(new Date()));
                jobMap.put(DispatchLogEnum.jobstate.getValue(), NifiStageTypeEnum.RUN_FAILED.getName());
                this.savePipelJobLog(pipelJobLogPo.jobTraceId, jobMap, pipelJobLogPo.pipelId, pipelJobLogPo.jobTraceId, pipelJobLogPo.componentId);
            }
        }
        //管道级别
        List<PipelJobLogPO> list = this.query().eq("pipel_trace_id", dto.pipelTraceId).orderByDesc("create_time").list();
        if (CollectionUtils.isNotEmpty(list)) {
            Map<Integer, Object> pipelMap = new HashMap<>();
            pipelMap.put(DispatchLogEnum.pipelend.getValue(), simpleDateFormat.format(new Date()));
            pipelMap.put(DispatchLogEnum.pipelstate.getValue(), NifiStageTypeEnum.RUN_FAILED.getName());
            //保存管道失败日志
            this.savePipelLog(dto.pipelTraceId, pipelMap, list.get(0).pipelId);
            iPipelLog.savePipelLog(dto.pipelTraceId, pipelMap, list.get(0).pipelId);
        }


    }


}
