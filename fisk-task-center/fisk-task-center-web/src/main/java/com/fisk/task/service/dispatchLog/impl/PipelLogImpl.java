package com.fisk.task.service.dispatchLog.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflow.DispatchEmailDTO;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author cfk
 */
@Service
@Slf4j
public class PipelLogImpl extends ServiceImpl<PipelLogMapper, PipelLogPO> implements IPipelLog {

    @Resource
    PipelLogMapper pipelLogMapper;
    @Resource
    DataFactoryClient dataFactoryClient;
    @Value("${nifi.pipeline.dispatch-email-url-prefix}")
    private String dispatchEmailUrlPrefix;

    @Override
    public void savePipelLog(String pipelTraceId, Map<Integer, Object> map, String pipelId) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("job参数1:pipelTraceId:{},map:{},pipelId:{}", pipelTraceId, JSON.toJSONString(map), pipelId);

        List<PipelLogPO> pipelLogs = new ArrayList<>();
        Iterator<Map.Entry<Integer, Object>> nodeMap = map.entrySet().iterator();
        while (nodeMap.hasNext()) {
            PipelLogPO pipelLog = new PipelLogPO();
            Map.Entry<Integer, Object> next = nodeMap.next();
            if (Objects.isNull(next.getValue())) {
                continue;
            }
            pipelLog.msg = next.getValue().toString();

            if (Objects.equals(DispatchLogEnum.pipelend.getValue(), next.getKey())) {
                //先更新掉
                pipelLogMapper.updateByPipelTraceId(pipelTraceId, next.getKey());
                DispatchEmailDTO dispatchEmail = new DispatchEmailDTO();
                dispatchEmail.nifiCustomWorkflowId = Integer.parseInt(pipelId);
                dispatchEmail.msg = pipelLog.msg;
                dispatchEmail.result = pipelLog.msg.contains("运行成功") ? "【运行成功】" : "【运行失败】";
                dispatchEmail.pipelTraceId = pipelLog.pipelTraceId;
                //    /**
                //     * 运行时长
                //     */
                //    public String duration;
                List<PipelLogPO> pos = this.query().eq("pipel_trace_id", pipelTraceId).list();
                if (CollectionUtils.isNotEmpty(pos)) {
                    PipelLogPO pipelLogPo = pos.get(0);
                    try {
                        Date date = new Date();
                        Date parse = format.parse(pipelLogPo.msg.substring(7, 26));
                        Long second = (date.getTime() - parse.getTime()) / 1000 % 60;
                        Long minutes = (date.getTime() - parse.getTime()) / (60 * 1000) % 60;
                        dispatchEmail.duration = minutes + "m " + second + "s";
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                dispatchEmail.url = "【"+ dispatchEmailUrlPrefix +"/#/DataFactory/pipelineSettings?pipelTraceId="
                        + dispatchEmail.pipelTraceId+"】";
                try {
                    Map<String, String> hashMap = new HashMap<>();
                    hashMap.put("运行结果", dispatchEmail.result);
                    hashMap.put("运行时长", dispatchEmail.duration);
                    hashMap.put("运行详情", dispatchEmail.msg);
                    hashMap.put("TraceID", dispatchEmail.pipelTraceId);
                    hashMap.put("页面地址", dispatchEmail.url);
                    dispatchEmail.body = hashMap;
                    dataFactoryClient.pipelineSendEmails(dispatchEmail);
                } catch (Exception e) {
                    log.error("发邮件出错,但是不影响主流程");
                }

            }


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
                long sec = (pipelMergeLog.endTime.getTime() - pipelMergeLog.startTime.getTime()) / 1000 % 60;
                long min = (pipelMergeLog.endTime.getTime() - pipelMergeLog.startTime.getTime()) / (60 * 1000) % 60;

                pipelMergeLog.duration = min + "m " + sec + "s ";
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
        // 处理超过50分钟的失败任务-把和当前时间比超过50分钟并且没有结束时间的任务指定为失败是不合理的，先注释；
       // handleIsFailStatus(pipelMergeLogs);
        return pipelMergeLogs;
    }

    /**
     * 把和当前时间比并且结束时间是空的日志直接改为已失败；
     * @param pipelMergeLogs
     */
    private void handleIsFailStatus(List<PipelMergeLog> pipelMergeLogs){
        for (PipelMergeLog item : pipelMergeLogs){
            Date endTime = item.getEndTime();
            if (endTime == null){
                Date currDate = new Date();
                long totalTime = (currDate.getTime() - item.startTime.getTime()) / (1000 * 60);
                if (totalTime >= 50){
                    item.result = "失败";
                    item.pipelStatu = "已失败";
                }
            }
        }
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

    @Override
    public ResultEntity<String> getPipelIdByTraceId(String pipelTraceId) {
        String pipelId = pipelLogMapper.getPipelIdByTraceId(pipelTraceId);
        return StringUtils.isEmpty(pipelId) ? null : ResultEntityBuild.buildData(ResultEnum.SUCCESS, pipelId);
    }
}
