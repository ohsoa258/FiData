package com.fisk.task.service.dispatchLog.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.dto.dispatchlog.LogStatisticsForChartVO;
import com.fisk.task.dto.dispatchlog.LogStatisticsVO;
import com.fisk.task.dto.dispatchlog.PipelLogVO;
import com.fisk.task.dto.dispatchlog.PipelMergeLog;
import com.fisk.task.entity.PipelLogPO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.mapper.PipelLogMapper;
import com.fisk.task.service.dispatchLog.IPipelLog;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    public List<PipelMergeLog> getPipelLogVos(PipelLogVO pipelLog) {
        List<PipelLogVO> list = pipelLogMapper.getPipelLogs(pipelLog);
        list.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(PipelLogVO::getCreateTime).reversed())
                .forEach(f -> {
                    f.typeName = DispatchLogEnum.getName(f.type).getName();
                    f.pipelName = pipelLog.pipelName;
                });
        //todo 管道日志:开始结束,合并为一条
        List<PipelMergeLog> pipelMergeLogs = new ArrayList<>();
        //转出一份备份
        List<PipelLogVO> pipelLogs = JSON.parseArray(JSON.toJSONString(list), PipelLogVO.class);
        //根据pipelTraceId去重,除了开始
        for (int i = 0; i < pipelLogs.size() - 1; i++) {
            for (int j = pipelLogs.size() - 1; j > i; j--) {
                if (pipelLogs.get(j).pipelTraceId.equals(pipelLogs.get(i).pipelTraceId) && !pipelLogs.get(j).msg.contains("开始运行")) {
                    pipelLogs.remove(j);
                }
            }
        }
        Collections.reverse(pipelLogs);
        for (int i = 0; i < pipelLogs.size() - 1; i++) {
            for (int j = pipelLogs.size() - 1; j > i; j--) {
                if (pipelLogs.get(j).pipelTraceId.equals(pipelLogs.get(i).pipelTraceId) && pipelLogs.get(j).msg.contains("开始运行")) {
                    pipelLogs.remove(j);
                }
            }
        }
        Collections.reverse(pipelLogs);
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < pipelLogs.size(); i++) {
            PipelMergeLog pipelMergeLog = new PipelMergeLog();
            PipelLogVO pipelLogVo = pipelLogs.get(i);
            pipelMergeLog.pipelId = pipelLogVo.pipelId;
            pipelMergeLog.pipelTraceId = pipelLogVo.pipelTraceId;
            pipelMergeLog.pipelName = pipelLogVo.pipelName;
            try {
                if (Objects.equals(pipelLogVo.type, DispatchLogEnum.pipelstart.getValue())) {
                    pipelMergeLog.startTime = simpleDate.parse(pipelLogVo.msg.substring(7, 26));
                    pipelMergeLog.createTime = pipelLogVo.createTime;
                } else if (Objects.equals(pipelLogVo.type, DispatchLogEnum.pipelend.getValue())) {
                    pipelMergeLog.endTime = simpleDate.parse(pipelLogVo.msg.substring(7, 26));
                    pipelMergeLog.pipelStatu = "已完成";
                    if (pipelLogVo.msg.contains("运行成功")) {
                        pipelMergeLog.result = "成功";
                    } else if (pipelLogVo.msg.contains("运行失败")) {
                        pipelMergeLog.result = "失败";
                    }
                }
            } catch (ParseException e) {
                log.error("转换时间异常", StackTraceHelper.getStackTraceInfo(e));
            }
            for (int j = 0; j < pipelLogs.size(); j++) {
                if (Objects.equals(pipelLogs.get(i).pipelTraceId, pipelLogs.get(j).pipelTraceId) && !Objects.equals(pipelLogs.get(i).msg, pipelLogs.get(j).msg)) {
                    try {
                        PipelLogVO LogVo = pipelLogs.get(j);
                        if (Objects.equals(LogVo.type, DispatchLogEnum.pipelstart.getValue())) {
                            pipelMergeLog.startTime = simpleDate.parse(LogVo.msg.substring(7, 26));
                            pipelMergeLog.createTime = LogVo.createTime;
                        } else if (Objects.equals(LogVo.type, DispatchLogEnum.pipelend.getValue())) {
                            pipelMergeLog.endTime = simpleDate.parse(LogVo.msg.substring(7, 26));
                            pipelMergeLog.pipelStatu = "已完成";
                            if (LogVo.msg.contains("运行成功")) {
                                pipelMergeLog.result = "成功";
                            } else if (LogVo.msg.contains("运行失败")) {
                                pipelMergeLog.result = "失败";
                            }
                        }
                    } catch (ParseException e) {
                        log.error("转换时间异常", StackTraceHelper.getStackTraceInfo(e));
                    }
                    pipelMergeLogs.add(pipelMergeLog);
                }

            }
            if (Objects.nonNull(pipelMergeLog.endTime) && Objects.nonNull(pipelMergeLog.startTime)) {
                pipelMergeLog.duration = (pipelMergeLog.endTime.getTime() - pipelMergeLog.startTime.getTime()) / 60000;
            } else {
                pipelMergeLog.pipelStatu = "正在运行";
                pipelMergeLogs.add(pipelMergeLog);
            }

        }
        for (int i = 0; i < pipelMergeLogs.size() - 1; i++) {
            for (int j = pipelMergeLogs.size() - 1; j > i; j--) {
                if (pipelMergeLogs.get(j).equals(pipelMergeLogs.get(i))) {
                    pipelMergeLogs.remove(j);
                }
            }
        }
        return pipelMergeLogs;
    }

    @Override
    public LogStatisticsForChartVO getLogStatisticsForChart(PipelLogVO pipelLog) {
        LogStatisticsForChartVO logStatisticsForChart = new LogStatisticsForChartVO();
        List<LogStatisticsVO> successLog = pipelLogMapper.getStatisticsLog(pipelLog.lookday, Integer.parseInt(pipelLog.pipelId), "运行成功");
        List<LogStatisticsVO> failureLog = pipelLogMapper.getStatisticsLog(pipelLog.lookday, Integer.parseInt(pipelLog.pipelId), "运行失败");
        List<LogStatisticsVO> amountLog = pipelLogMapper.getStatisticsLog(pipelLog.lookday, Integer.parseInt(pipelLog.pipelId), "开始运行");
        if (CollectionUtils.isNotEmpty(amountLog)) {


            for (int i = 0; i < amountLog.size(); i++) {
                boolean successReplenish = true;
                boolean failureReplenish = true;
                if (CollectionUtils.isNotEmpty(successLog)) {
                    for (int j = 0; j < successLog.size(); j++) {
                        if (Objects.equals(amountLog.get(i).days, successLog.get(j).days)) {
                            successReplenish = false;
                        }
                    }
                }
                if (successReplenish) {
                    LogStatisticsVO logStatistics = JSON.parseObject(JSON.toJSONString(amountLog.get(i)), LogStatisticsVO.class);
                    logStatistics.sum = 0;
                    successLog.add(logStatistics);
                }
                if (CollectionUtils.isNotEmpty(failureLog)) {
                    for (int k = 0; k < failureLog.size(); k++) {
                        if (Objects.equals(amountLog.get(i).days, failureLog.get(k).days)) {
                            failureReplenish = false;
                        }
                    }
                }
                if (failureReplenish) {
                    LogStatisticsVO logStatistics = JSON.parseObject(JSON.toJSONString(amountLog.get(i)), LogStatisticsVO.class);
                    logStatistics.sum = 0;
                    failureLog.add(logStatistics);
                }
            }
        }
        logStatisticsForChart.amountLog = amountLog;
        logStatisticsForChart.failureLog = failureLog;
        logStatisticsForChart.successLog = successLog;
        return logStatisticsForChart;
    }


    public static void main(String[] args) {
        String dd = "[{\"id\":643,\"createTime\":\"2022-11-17T17:08:00\",\"createUser\":null,\"updateTime\":null,\"updateUser\":null,\"delFlag\":1,\"pipelId\":\"298\",\"pipelTraceId\":\"4839d472-626c-4ecf-bb1f-b90f983430f0\",\"msg\":\"运行成功 - 2022-11-17 17:07:59\",\"type\":2,\"typeName\":\"管道结束\",\"pipelName\":\"USA_PLM_PP\"},{\"id\":640,\"createTime\":\"2022-11-17T17:00:01\",\"createUser\":null,\"updateTime\":null,\"updateUser\":null,\"delFlag\":1,\"pipelId\":\"298\",\"pipelTraceId\":\"4839d472-626c-4ecf-bb1f-b90f983430f0\",\"msg\":\"开始运行 - 2022-11-17 17:00:00\",\"type\":1,\"typeName\":\"管道开始\",\"pipelName\":\"USA_PLM_PP\"},{\"id\":638,\"createTime\":\"2022-11-17T16:05:58\",\"createUser\":null,\"updateTime\":null,\"updateUser\":null,\"delFlag\":1,\"pipelId\":\"298\",\"pipelTraceId\":\"7db08566-9a9f-4e32-b0d8-0888bbc79f96\",\"msg\":\"运行成功 - 2022-11-17 16:05:57\",\"type\":2,\"typeName\":\"管道结束\",\"pipelName\":\"USA_PLM_PP\"},{\"id\":636,\"createTime\":\"2022-11-17T16:00:00\",\"createUser\":null,\"updateTime\":null,\"updateUser\":null,\"delFlag\":1,\"pipelId\":\"298\",\"pipelTraceId\":\"7db08566-9a9f-4e32-b0d8-0888bbc79f96\",\"msg\":\"开始运行 - 2022-11-17 16:00:00\",\"type\":1,\"typeName\":\"管道开始\",\"pipelName\":\"USA_PLM_PP\"},{\"id\":633,\"createTime\":\"2022-11-17T15:05:56\",\"createUser\":null,\"updateTime\":null,\"updateUser\":null,\"delFlag\":1,\"pipelId\":\"298\",\"pipelTraceId\":\"9ddce5d1-8daa-400a-a62e-64a1a6330c43\",\"msg\":\"运行成功 - 2022-11-17 15:05:55\",\"type\":2,\"typeName\":\"管道结束\",\"pipelName\":\"USA_PLM_PP\"},{\"id\":631,\"createTime\":\"2022-11-17T15:00:00\",\"createUser\":null,\"updateTime\":null,\"updateUser\":null,\"delFlag\":1,\"pipelId\":\"298\",\"pipelTraceId\":\"9ddce5d1-8daa-400a-a62e-64a1a6330c43\",\"msg\":\"开始运行 - 2022-11-17 15:00:00\",\"type\":1,\"typeName\":\"管道开始\",\"pipelName\":\"USA_PLM_PP\"}]";
        List<PipelLogVO> pipelLogs = JSON.parseArray(dd, PipelLogVO.class);
        List<PipelLogVO> pipelLogs1 = JSON.parseArray(dd, PipelLogVO.class);
        System.out.println("去重前:" + pipelLogs.size());
        for (int i = 0; i < pipelLogs.size() - 1; i++) {
            for (int j = pipelLogs.size() - 1; j > i; j--) {
                if (pipelLogs.get(j).pipelTraceId.equals(pipelLogs.get(i).pipelTraceId) && !pipelLogs.get(j).msg.contains("开始运行")) {
                    pipelLogs.remove(j);
                }
            }
        }
        Collections.reverse(pipelLogs1);
        for (int i = 0; i < pipelLogs1.size() - 1; i++) {
            for (int j = pipelLogs1.size() - 1; j > i; j--) {
                if (pipelLogs1.get(j).pipelTraceId.equals(pipelLogs1.get(i).pipelTraceId)) {
                    pipelLogs1.remove(j);
                }
            }
        }
        System.out.println(JSON.toJSONString(pipelLogs));
        System.out.println(JSON.toJSONString(pipelLogs1));
    }
}
