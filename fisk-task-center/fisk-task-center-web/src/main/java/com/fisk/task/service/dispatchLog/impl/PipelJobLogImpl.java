package com.fisk.task.service.dispatchLog.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.dispatchlog.PipelJobLogVO;
import com.fisk.task.entity.PipelJobLogPO;
import com.fisk.task.entity.PipelTaskLogPO;
import com.fisk.task.entity.PipelLogPO;
import com.fisk.task.dto.dispatchlog.*;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.mapper.PipelJobLogMapper;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelLog;
import com.fisk.task.service.dispatchLog.IPipelStageLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.ParseException;
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
    public List<PipelJobMergeLogVO> getPipelJobLogVos(List<PipelJobLogVO> pipelJobLogs) {
        List<PipelJobLogVO> pipelJobLogVos = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(pipelJobLogs)) {
            for (PipelJobLogVO pipelJobLog : pipelJobLogs) {
                List<PipelJobLogVO> pipelJobLogVo = pipelJobLogMapper.getPipelJobLogVo(pipelJobLog);
                pipelJobLogVo.stream()
                        .filter(Objects::nonNull)
                        .filter(e -> e.componentId != null && e.componentId.equalsIgnoreCase(pipelJobLog.componentId))
                        .sorted(Comparator.comparing(PipelJobLogVO::getCreateTime).reversed())
                        .forEach(f -> {
                            f.componentName = pipelJobLog.componentName;
                        });
                pipelJobLogVo.stream()
                        .filter(Objects::nonNull)
                        .forEach(f -> {
                            f.pipelName = pipelJobLog.pipelName;
                            f.typeName = DispatchLogEnum.getName(f.type).getName();
                        });
                pipelJobLogVos.addAll(JSON.parseArray(JSON.toJSONString(pipelJobLogVo), PipelJobLogVO.class));
            }
            pipelJobLogVos.sort((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()));
            Collections.reverse(pipelJobLogVos);
        }
        //todo job日志,两条合成一条
        //转出一份备份
        List<PipelJobMergeLogVO> pipelJobMergeLogVos = new ArrayList<>();
        List<PipelJobLogVO> logs = JSON.parseArray(JSON.toJSONString(pipelJobLogVos), PipelJobLogVO.class);
        //根据pipelTraceId去重,除了开始
        for (int i = 0; i < logs.size() - 1; i++) {
            for (int j = logs.size() - 1; j > i; j--) {
                if (logs.get(j).jobTraceId.equals(logs.get(i).jobTraceId) && !logs.get(j).msg.contains("开始运行")) {
                    logs.remove(j);
                }
            }
        }
        Collections.reverse(logs);
        for (int i = 0; i < logs.size() - 1; i++) {
            for (int j = logs.size() - 1; j > i; j--) {
                if (logs.get(j).jobTraceId.equals(logs.get(i).jobTraceId) && logs.get(j).msg.contains("开始运行")) {
                    logs.remove(j);
                }
            }
        }
        Collections.reverse(logs);
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < logs.size(); i++) {
            PipelJobMergeLogVO pipelJobMergeLogVo = new PipelJobMergeLogVO();
            PipelJobLogVO pipelJobLogVo = logs.get(i);
            pipelJobMergeLogVo.pipelTraceId = pipelJobLogVo.pipelTraceId;
            pipelJobMergeLogVo.jobTraceId = pipelJobLogVo.jobTraceId;
            pipelJobMergeLogVo.pipelId = pipelJobLogVo.pipelId;
            pipelJobMergeLogVo.pipelName = pipelJobLogVo.pipelName;
            pipelJobMergeLogVo.componentId = pipelJobLogVo.componentId;
            pipelJobMergeLogVo.componentName = pipelJobLogVo.componentName;
            try {
                if (Objects.equals(pipelJobLogVo.type, DispatchLogEnum.jobstart.getValue())) {
                    pipelJobMergeLogVo.startTime = simpleDate.parse(pipelJobLogVo.msg.substring(7, 26));
                    pipelJobMergeLogVo.createTime = pipelJobLogVo.createTime;
                } else if (Objects.equals(pipelJobLogVo.type, DispatchLogEnum.jobend.getValue())) {
                    pipelJobMergeLogVo.endTime = simpleDate.parse(pipelJobLogVo.msg.substring(7, 26));
                    if (pipelJobLogVo.msg.contains("运行成功")) {
                        pipelJobMergeLogVo.result = "成功";
                    } else if (pipelJobLogVo.msg.contains("运行失败")) {
                        pipelJobMergeLogVo.result = "失败";
                    }
                }
            } catch (ParseException e) {
                log.error("转换时间异常", StackTraceHelper.getStackTraceInfo(e));
            }
            for (int j = 0; j < logs.size(); j++) {
                if (Objects.equals(logs.get(i).jobTraceId, logs.get(j).jobTraceId) && !Objects.equals(logs.get(i).msg, logs.get(j).msg)) {
                    try {
                        PipelJobLogVO pipelJobLog = logs.get(j);
                        if (Objects.equals(pipelJobLog.type, DispatchLogEnum.jobstart.getValue())) {
                            pipelJobMergeLogVo.startTime = simpleDate.parse(pipelJobLog.msg.substring(7, 26));
                            pipelJobMergeLogVo.createTime = pipelJobLogVo.createTime;
                        } else if (Objects.equals(pipelJobLog.type, DispatchLogEnum.jobend.getValue())) {
                            pipelJobMergeLogVo.endTime = simpleDate.parse(pipelJobLog.msg.substring(7, 26));
                            if (pipelJobLog.msg.contains("运行成功")) {
                                pipelJobMergeLogVo.result = "成功";
                            } else if (pipelJobLog.msg.contains("运行失败")) {
                                pipelJobMergeLogVo.result = "失败";
                            }
                        }
                    } catch (ParseException e) {
                        log.error("转换时间异常", StackTraceHelper.getStackTraceInfo(e));
                    }
                    pipelJobMergeLogVos.add(pipelJobMergeLogVo);
                }
            }
            if (Objects.nonNull(pipelJobMergeLogVo.endTime) && Objects.nonNull(pipelJobMergeLogVo.startTime)) {
                pipelJobMergeLogVo.duration = (pipelJobMergeLogVo.endTime.getTime() - pipelJobMergeLogVo.startTime.getTime()) / 60000;
                pipelJobMergeLogVos.add(pipelJobMergeLogVo);
            }

            if (Objects.isNull(pipelJobMergeLogVo.endTime) && Objects.nonNull(pipelJobMergeLogVo.startTime)) {
                pipelJobMergeLogVos.add(pipelJobMergeLogVo);
            }

        }
        for (int i = 0; i < pipelJobMergeLogVos.size() - 1; i++) {
            for (int j = pipelJobMergeLogVos.size() - 1; j > i; j--) {
                if (pipelJobMergeLogVos.get(j).equals(pipelJobMergeLogVos.get(i))) {
                    pipelJobMergeLogVos.remove(j);
                }
            }
        }
        return pipelJobMergeLogVos;
    }

    @Override
    public void exceptionHandlingLog(DispatchExceptionHandlingDTO dto) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //1.从小到大保存
        //任务日志
        dto.JobName = Objects.equals(dto.JobName, null) ? "" : dto.JobName;
        dto.pipleName = Objects.equals(dto.pipleName, null) ? "" : dto.pipleName;
        if (!StringUtils.isEmpty(dto.pipelTaskTraceId)) {
            List<PipelTaskLogPO> list = iPipelTaskLog.query().eq("task_trace_id", dto.pipelTaskTraceId).orderByDesc("create_time").list();
            if (CollectionUtils.isNotEmpty(list)) {
                Map<Integer, Object> stageMap = new HashMap<>();
                stageMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + simpleDateFormat.format(new Date()) + " - " + dto.comment);
                //stageMap.put(DispatchLogEnum.taskstate.getValue(), dto.pipleName + dto.JobName + " " + NifiStageTypeEnum.RUN_FAILED.getName());
                //stageMap.put(DispatchLogEnum.taskcomment.getValue(), dto.pipleName + dto.JobName + " " + dto.comment);
                log.info("第一处调用保存task日志");
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
                jobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + simpleDateFormat.format(new Date()) + " - " + dto.comment);
                //jobMap.put(DispatchLogEnum.jobstate.getValue(), dto.JobName + " " + NifiStageTypeEnum.RUN_FAILED.getName());
                this.savePipelJobLog(pipelJobLogPo.pipelTraceId, jobMap, pipelJobLogPo.pipelId, pipelJobLogPo.jobTraceId, pipelJobLogPo.componentId);
            }
        }
        //管道级别
        List<PipelJobLogPO> list = this.query().eq("pipel_trace_id", dto.pipelTraceId).orderByDesc("create_time").list();
        if (CollectionUtils.isNotEmpty(list)) {
            Map<Integer, Object> pipelMap = new HashMap<>();
            pipelMap.put(DispatchLogEnum.pipelend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + simpleDateFormat.format(new Date()) + " - " + dto.comment);
            //pipelMap.put(DispatchLogEnum.pipelstate.getValue(), dto.pipleName + " " + NifiStageTypeEnum.RUN_FAILED.getName());
            //保存管道失败日志
            log.info("第三处调用保存job日志");
            this.savePipelLog(dto.pipelTraceId, pipelMap, list.get(0).pipelId);
            iPipelLog.savePipelLog(dto.pipelTraceId, pipelMap, list.get(0).pipelId);
        }


    }

    public static void main(String[] args) {
        List<PipelJobLogVO> pipelJobLogVos = new ArrayList<>();
        for(int i=0;i<4;i++){
            PipelJobLogVO pipelJobLogVO = new PipelJobLogVO();
            pipelJobLogVO.msg="开始运行 - 2022-11-28 10:00:00";
            pipelJobLogVO.jobTraceId="ccc"+i;
            pipelJobLogVO.type = 4;
            pipelJobLogVos.add(pipelJobLogVO);
        }
        //转出一份备份
        List<PipelJobMergeLogVO> pipelJobMergeLogVos = new ArrayList<>();
        List<PipelJobLogVO> logs = JSON.parseArray(JSON.toJSONString(pipelJobLogVos), PipelJobLogVO.class);
        //根据pipelTraceId去重,除了开始
        for (int i = 0; i < logs.size() - 1; i++) {
            for (int j = logs.size() - 1; j > i; j--) {
                if (logs.get(j).jobTraceId.equals(logs.get(i).jobTraceId) && !logs.get(j).msg.contains("开始运行")) {
                    logs.remove(j);
                }
            }
        }
        Collections.reverse(logs);
        for (int i = 0; i < logs.size() - 1; i++) {
            for (int j = logs.size() - 1; j > i; j--) {
                if (logs.get(j).jobTraceId.equals(logs.get(i).jobTraceId) && logs.get(j).msg.contains("开始运行")) {
                    logs.remove(j);
                }
            }
        }
        Collections.reverse(logs);
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < logs.size(); i++) {
            PipelJobMergeLogVO pipelJobMergeLogVo = new PipelJobMergeLogVO();
            PipelJobLogVO pipelJobLogVo = logs.get(i);
            pipelJobMergeLogVo.pipelTraceId = pipelJobLogVo.pipelTraceId;
            pipelJobMergeLogVo.jobTraceId = pipelJobLogVo.jobTraceId;
            pipelJobMergeLogVo.pipelId = pipelJobLogVo.pipelId;
            pipelJobMergeLogVo.pipelName = pipelJobLogVo.pipelName;
            pipelJobMergeLogVo.componentId = pipelJobLogVo.componentId;
            pipelJobMergeLogVo.componentName = pipelJobLogVo.componentName;
            try {
                if (Objects.equals(pipelJobLogVo.type, DispatchLogEnum.jobstart.getValue())) {
                    pipelJobMergeLogVo.startTime = simpleDate.parse(pipelJobLogVo.msg.substring(7, 26));
                    pipelJobMergeLogVo.createTime = pipelJobLogVo.createTime;
                } else if (Objects.equals(pipelJobLogVo.type, DispatchLogEnum.jobend.getValue())) {
                    pipelJobMergeLogVo.endTime = simpleDate.parse(pipelJobLogVo.msg.substring(7, 26));
                    if (pipelJobLogVo.msg.contains("运行成功")) {
                        pipelJobMergeLogVo.result = "成功";
                    } else if (pipelJobLogVo.msg.contains("运行失败")) {
                        pipelJobMergeLogVo.result = "失败";
                    }
                }
            } catch (ParseException e) {
                log.error("转换时间异常", StackTraceHelper.getStackTraceInfo(e));
            }
            for (int j = 0; j < logs.size(); j++) {
                if (Objects.equals(logs.get(i).jobTraceId, logs.get(j).jobTraceId) && !Objects.equals(logs.get(i).msg, logs.get(j).msg)) {
                    try {
                        PipelJobLogVO pipelJobLog = logs.get(j);
                        if (Objects.equals(pipelJobLog.type, DispatchLogEnum.jobstart.getValue())) {
                            pipelJobMergeLogVo.startTime = simpleDate.parse(pipelJobLog.msg.substring(7, 26));
                            pipelJobMergeLogVo.createTime = pipelJobLogVo.createTime;
                        } else if (Objects.equals(pipelJobLog.type, DispatchLogEnum.jobend.getValue())) {
                            pipelJobMergeLogVo.endTime = simpleDate.parse(pipelJobLog.msg.substring(7, 26));
                            if (pipelJobLog.msg.contains("运行成功")) {
                                pipelJobMergeLogVo.result = "成功";
                            } else if (pipelJobLog.msg.contains("运行失败")) {
                                pipelJobMergeLogVo.result = "失败";
                            }
                        }
                    } catch (ParseException e) {
                        log.error("转换时间异常", StackTraceHelper.getStackTraceInfo(e));
                    }
                    pipelJobMergeLogVos.add(pipelJobMergeLogVo);
                }
            }
            if (Objects.nonNull(pipelJobMergeLogVo.endTime) && Objects.nonNull(pipelJobMergeLogVo.startTime)) {
                pipelJobMergeLogVo.duration = (pipelJobMergeLogVo.endTime.getTime() - pipelJobMergeLogVo.startTime.getTime()) / 60000;
                pipelJobMergeLogVos.add(pipelJobMergeLogVo);
            }
            if (Objects.isNull(pipelJobMergeLogVo.endTime) && Objects.nonNull(pipelJobMergeLogVo.startTime)) {
                pipelJobMergeLogVos.add(pipelJobMergeLogVo);
            }

        }
        for (int i = 0; i < pipelJobMergeLogVos.size() - 1; i++) {
            for (int j = pipelJobMergeLogVos.size() - 1; j > i; j--) {
                if (pipelJobMergeLogVos.get(j).equals(pipelJobMergeLogVos.get(i))) {
                    pipelJobMergeLogVos.remove(j);
                }
            }
        }
        System.out.println(pipelJobMergeLogVos.size());
    }


}
