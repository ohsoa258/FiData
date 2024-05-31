package com.fisk.task.service.dispatchLog.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.datamodel.TableQueryDTO;
import com.fisk.datafactory.dto.tasknifi.TaskHierarchyDTO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.enums.DataFactoryEnum;
import com.fisk.mdm.client.MdmClient;
import com.fisk.task.dto.dispatchlog.DataServiceTableLogQueryVO;
import com.fisk.task.dto.dispatchlog.DataServiceTableLogVO;
import com.fisk.task.dto.dispatchlog.PipelTaskLogVO;
import com.fisk.task.dto.dispatchlog.PipelTaskMergeLogVO;
import com.fisk.task.dto.query.DataServiceTableLogQueryDTO;
import com.fisk.task.dto.tableservice.TableServiceDetailDTO;
import com.fisk.task.entity.PipelTaskLogPO;
import com.fisk.task.entity.TaskLogPO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.mapper.PipelTaskLogMapper;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.utils.StackTraceHelper;
import com.fisk.task.vo.tableservice.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cfk
 */
@Service
@Slf4j
public class PipelTaskLogImpl extends ServiceImpl<PipelTaskLogMapper, PipelTaskLogPO> implements IPipelTaskLog {
    @Value("${dataservice.dbname}")
    private String dataServiceDbName;

    @Resource
    PipelTaskLogMapper pipelTaskLogMapper;
    @Resource
    RedisUtil redisUtil;
    @Value("${nifi.pipeline.maxTime}")
    public String maxTime;
    @Resource
    private DataModelClient dataModelClient;
    @Resource
    private DataAccessClient dataAccessClient;

    @Resource
    private MdmClient mdmClient;
    @Override
    public void savePipelTaskLog(String pipelTraceId, String jobTraceId, String pipelTaskTraceId, Map<Integer, Object> map, String taskId, String tableId, int tableType) {
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
            /*if (Objects.equals(pipelTaskLog.type, DispatchLogEnum.taskend.getValue())) {
                //pipelTaskLogMapper.updateByPipelTraceId(pipelTaskTraceId, pipelTaskLog.type);
            }*/
            //修改dag图的task的状态
            try {
                if (StringUtils.isNotEmpty(pipelTraceId)) {
                    Map<Object, Object> taskMap = new HashMap<>();
                    Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + pipelTraceId);
                    TaskHierarchyDTO dto = JSON.parseObject(hmget.get(taskId).toString(), TaskHierarchyDTO.class);
                    dto.taskProcessed = true;
                    if (Objects.equals(pipelTaskLog.type, DispatchLogEnum.taskstart.getValue())) {
                        //dto.taskStatus = DispatchLogEnum.taskstart;
                        taskMap.put(taskId, JSON.toJSONString(dto));
                        redisUtil.hmsetForDispatch(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + pipelTraceId, taskMap, Long.parseLong(maxTime));
                    } else if (Objects.equals(pipelTaskLog.type, DispatchLogEnum.taskend.getValue())) {
                        if (next.getKey() == DispatchLogEnum.taskstart.getValue()){
                            dto.taskStatus = DispatchLogEnum.taskstart;
                        }else if (next.getKey() == DispatchLogEnum.taskend.getValue()){
                            dto.taskStatus = DispatchLogEnum.taskend;
                        }
                       /* if (pipelTaskLog.msg.contains(NifiStageTypeEnum.PASS.getName())) {
                            dto.taskStatus = DispatchLogEnum.taskpass;
                        } else if (pipelTaskLog.msg.contains(NifiStageTypeEnum.RUN_FAILED.getName())) {
                            dto.taskStatus = DispatchLogEnum.taskfailure;
                        } else if (pipelTaskLog.msg.contains(NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName())) {
                            dto.taskStatus = DispatchLogEnum.taskend;
                        }*/

                        taskMap.put(taskId, JSON.toJSONString(dto));
                        redisUtil.hmsetForDispatch(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + pipelTraceId, taskMap, Long.parseLong(maxTime));
                    }
                }
            } catch (Exception e) {
                log.error("redis中task集合数据不存在:" + pipelTraceId, e);
            }
            if (StringUtils.isNotEmpty(tableId) && !Objects.equals(tableType, OlapTableEnum.CUSTOMIZESCRIPT.getValue())) {
                pipelTaskLog.tableId = Integer.parseInt(tableId);
            }
            pipelTaskLog.tableType = tableType;
            pipelTaskLogs.add(pipelTaskLog);
        }
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = entryDate.toInstant().atZone(zoneId).toLocalDateTime();
        List<PipelTaskLogPO> pipelTaskLogPOList = pipelTaskLogs.stream().filter(Objects::nonNull).collect(Collectors.toList());
        for (PipelTaskLogPO taskLogPO : pipelTaskLogPOList) {
            taskLogPO.setCreateTime(localDateTime);
            if (Objects.equals(taskLogPO.type, DispatchLogEnum.taskend.getValue())){
                String json = (String)redisUtil.get(RedisKeyEnum.PIPEL_END_TASK_TRACE_ID.getName() + ":" + pipelTaskTraceId);
                PipelTaskLogPO pipelTaskLogPO = JSON.parseObject(json, PipelTaskLogPO.class);
                if (pipelTaskLogPO != null){
                    taskLogPO.setMsg(pipelTaskLogPO.getMsg());
                    this.updateById(taskLogPO);
                    redisUtil.set(RedisKeyEnum.PIPEL_END_TASK_TRACE_ID.getName() + ":" + pipelTaskTraceId,JSON.toJSONString(taskLogPO),Long.parseLong(maxTime));
                }else {
                    this.save(taskLogPO);
                }
            }else {
                this.save(taskLogPO);
            }
        }
    }

    @Override
    public List<PipelTaskLogVO> getPipelTaskLogVo(String taskTraceId) {
        LambdaQueryWrapper<PipelTaskLogPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PipelTaskLogPO::getTaskTraceId,taskTraceId);
        List<PipelTaskLogPO> pipelTaskLogPOS = pipelTaskLogMapper.selectList(queryWrapper);
        List<PipelTaskLogVO> pipelTaskLogVOS = pipelTaskLogPOS.stream().map(i -> {
            PipelTaskLogVO pipelTaskLogVO = new PipelTaskLogVO();
            pipelTaskLogVO.setTaskId(i.getTaskId());
            pipelTaskLogVO.setTaskTraceId(i.getTaskTraceId());
            pipelTaskLogVO.setType(i.getType());
            pipelTaskLogVO.setMsg(i.getMsg());
            pipelTaskLogVO.setJobTraceId(i.getJobTraceId());
            pipelTaskLogVO.setTableId(String.valueOf(i.getTableId()));
            return pipelTaskLogVO;
        }).collect(Collectors.toList());
        return pipelTaskLogVOS;
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
    public List<PipelTaskMergeLogVO> getPipelTaskLogVos1(List<PipelTaskLogVO> pipelTaskLogs) {
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
                    pipelTaskMergeLogVo.msg = pipelTaskLogVo.msg + " taskTraceId:" + pipelTaskLogVo.taskTraceId;
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
                            pipelTaskMergeLogVo.msg = pipelTaskLogVo.msg + " taskTraceId:" + pipelTaskLogVo.taskTraceId;
                        }
                    } catch (ParseException e) {
                        log.error("转换时间异常", StackTraceHelper.getStackTraceInfo(e));
                    }
                    pipelTaskMergeLogVos.add(pipelTaskMergeLogVo);
                }
            }
            if (Objects.nonNull(pipelTaskMergeLogVo.endTime) && Objects.nonNull(pipelTaskMergeLogVo.startTime)) {
                long sec = (pipelTaskMergeLogVo.endTime.getTime() - pipelTaskMergeLogVo.startTime.getTime()) / 1000 % 60;
                long min = (pipelTaskMergeLogVo.endTime.getTime() - pipelTaskMergeLogVo.startTime.getTime()) / (60 * 1000) % 60;
                pipelTaskMergeLogVo.duration = min + "m " + sec + "s ";
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

    @Override
    public List<PipelTaskMergeLogVO> getPipelTaskLogVos(String JobTraceId) {
        List<PipelTaskMergeLogVO> result = new ArrayList<>();
        List<PipelTaskMergeLogVO> pipelTaskLogVos = pipelTaskLogMapper.getPipelTaskLogVos(JobTraceId);
        pipelTaskLogVos.stream().map(i->{
            if (Objects.nonNull(i.endTime)){
                long sec = (i.endTime.getTime() - i.startTime.getTime()) / 1000 % 60;
                long min = (i.endTime.getTime() - i.startTime.getTime()) / (60 * 1000) % 60;
                long hour = (i.endTime.getTime() - i.startTime.getTime()) / (60 * 60 * 1000);
                i.duration = hour+"h " + min + "m " + sec + "s ";
            }
            return i;
        }).collect(Collectors.toList());
        Map<String, List<PipelTaskMergeLogVO>> map = pipelTaskLogVos.stream().collect(Collectors.groupingBy(PipelTaskMergeLogVO::getTableType));
        for (Map.Entry<String, List<PipelTaskMergeLogVO>> stringListEntry : map.entrySet()) {
            TableQueryDTO tableQueryDTO = new TableQueryDTO();
            tableQueryDTO.setType(Integer.parseInt(stringListEntry.getKey()));
            List<String> ids = stringListEntry.getValue().stream().map(PipelTaskMergeLogVO::getTableId).collect(Collectors.toList());
            tableQueryDTO.setIds(ids);
            Map<Integer, String> resultMap = new HashMap<>();
            switch (Objects.requireNonNull(OlapTableEnum.getNameByValue(Integer.parseInt(stringListEntry.getKey())))){
                case DIMENSION:
                case FACT:
                case WIDETABLE:
                    ResultEntity<Object> tableNames = dataModelClient.getTableNames(tableQueryDTO);
                    if (tableNames.code != ResultEnum.SUCCESS.getCode()){
                        log.error("远程调用失败,方法名: 【getPipelTaskLogVos】");
                        throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
                    }
                    resultMap = (Map<Integer,String>)tableNames.getData();
                    break;
                case PHYSICS:
                    ResultEntity<Object> tables = dataAccessClient.getTableNames(tableQueryDTO);
                    if (tables.code != ResultEnum.SUCCESS.getCode()){
                        log.error("远程调用失败,方法名: 【getPipelTaskLogVos】");
                        throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
                    }
                    resultMap = (Map<Integer,String>)tables.getData();
                    break;
                case MDM_DATA_ACCESS:
                    ResultEntity<Object> entityNames = mdmClient.getTableNames(tableQueryDTO);
                    if (entityNames.code != ResultEnum.SUCCESS.getCode()){
                        log.error("远程调用失败,方法名: 【getPipelTaskLogVos】");
                        throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
                    }
                    resultMap = (Map<Integer,String>)entityNames.getData();
                    break;
                case CUSTOMIZESCRIPT:
                    Map<Integer,String> map1 = new HashMap<>();
                    map1.put(1,"自定义脚本任务");
                    resultMap = map1;
                    break;
                case SFTPFILECOPYTASK:
                    Map<Integer,String> map2 = new HashMap<>();
                    map2.put(1,"SFTP文件复制");
                    resultMap = map2;
                    break;
                case POWERBIDATASETREFRESHTASK:
                    Map<Integer,String> map3 = new HashMap<>();
                    map3.put(1,"PBI DataSet 刷新");
                    resultMap = map3;
                    break;
                default:
                    break;
            }
            for (PipelTaskMergeLogVO pipelTaskMergeLogVO : stringListEntry.getValue()) {
                String tableName = resultMap.get(pipelTaskMergeLogVO.getTableId());

                // -100 业务域id/应用id
                String areaId = resultMap.get("-100");
                // -200 业务域名称/应用名称
                String areaName = resultMap.get("-200");

                //表名
                if (StringUtils.isEmpty(tableName)){
                    pipelTaskMergeLogVO.setTableName(resultMap.get(1));
                }else {
                    pipelTaskMergeLogVO.setTableName(tableName);
                }

                // -100 业务域id/应用id
                if (!StringUtils.isEmpty(areaId)){
                    pipelTaskMergeLogVO.setAreaId(areaId);
                }
                // -200 业务域名称/应用名称
                if (!StringUtils.isEmpty(areaName)){
                    pipelTaskMergeLogVO.setAreaName(areaName);
                }

                result.add(pipelTaskMergeLogVO);
            }
        }
        return result;
    }

    @Override
    public ResultEntity<DataServiceTableLogQueryVO> getDataServiceTableLogVos(DataServiceTableLogQueryDTO dto) {
        DataServiceTableLogQueryVO responseVO = new DataServiceTableLogQueryVO();
        if (dto.getTableList() == null || dto.getTableType() == 0) {
            return ResultEntityBuild.build(ResultEnum.PARAMTER_NOTNULL, null);
        }
        List<Long> tableIdList = dto.getTableList().keySet().stream().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tableIdList)) {
            return ResultEntityBuild.build(ResultEnum.PARAMTER_NOTNULL, null);
        }
        dto.setTableIdList(tableIdList);
        if (dto.getCurrent() == 0) {
            dto.setCurrent(1);
        }
        if (dto.getSize() == 0) {
            dto.setSize(10);
        }
        List<DataServiceTableLogVO> array = baseMapper.getDataServiceTableLogs(dto);
        if (CollectionUtils.isEmpty(array)) {
            return ResultEntityBuild.build(ResultEnum.SUCCESS, responseVO);
        }
        array.forEach(t -> {
            String tableDisplayName = dto.getTableList().get(t.getTableId());
            t.setTableDisplayName(tableDisplayName);
        });
        if (StringUtils.isNotEmpty(dto.getKeyword())) {
            array = array.stream().filter(t -> t.tableDisplayName.contains(dto.getKeyword()) || t.getMsg().contains(dto.getKeyword())).collect(Collectors.toList());
        }
        int rowsCount = array.stream().toArray().length;
        responseVO.current = dto.current;
        responseVO.size = dto.size;
        if (rowsCount > 0) {
            responseVO.total = rowsCount;
            responseVO.page = (int) Math.ceil(1.0 * rowsCount / dto.size);
            dto.current = dto.current - 1;
            array = array.stream().skip((dto.current - 1 + 1) * dto.size).limit(dto.size).collect(Collectors.toList());
            responseVO.setDataArray(array);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, responseVO);
    }

    @Override
    public void updatePipelTaskLog(String pipelTaskTraceId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LambdaQueryWrapper<PipelTaskLogPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PipelTaskLogPO::getTaskTraceId,pipelTaskTraceId).eq(PipelTaskLogPO::getType,DispatchLogEnum.taskend.getValue());
        PipelTaskLogPO one = this.getOne(queryWrapper);
        if (one != null){
            if (one.getMsg().contains("运行成功")){
                Map<Object, Object> pipelTask = redisUtil.getAndDel(RedisKeyEnum.PIPEL_TASK.getName() + ":" + pipelTaskTraceId);
                Object endTime = pipelTask.get(DispatchLogEnum.taskend.getName());
                Object count = pipelTask.get(DispatchLogEnum.taskcount.getName());
                String msg = NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + (endTime != null ? endTime.toString() : simpleDateFormat.format(new Date())) + " - 同步条数 : " + (Objects.isNull(count) || "null".equals(count) ? 0 : count);
                pipelTaskLogMapper.updateMsgByPipelTraceId(msg,pipelTaskTraceId,DispatchLogEnum.taskend.getValue());
            }
        }
    }

    @Override
    public TableStatisticsVO getLogStatistics(Integer lookday) {
        TableStatisticsVO statisticsVO = new TableStatisticsVO();
        Integer successSum = pipelTaskLogMapper.getTableServerStatisticsLog(lookday, "运行成功");
        Integer failureSum = pipelTaskLogMapper.getTableServerStatisticsLog(lookday, "运行失败");
        Integer runningSum = pipelTaskLogMapper.getTableServerStatisticsLog(lookday, "开始运行");

        statisticsVO.runningSum = runningSum - failureSum - successSum;
        statisticsVO.failureSum = failureSum;
        statisticsVO.successSum = successSum;
        return statisticsVO;
    }

    @Override
    public List<TableGanttChartVO> getGanttChart() {
        List<TableGanttChartVO> ganttChart = pipelTaskLogMapper.getGanttChart(dataServiceDbName);
        return ganttChart;
    }

    @Override
    public List<TableTopRunningTimeVO> getTopRunningTime(Integer lookday) {
        List<TableTopRunningTimeVO> topRunningTime = pipelTaskLogMapper.getTopRunningTime(lookday,dataServiceDbName);
        return topRunningTime;
    }

    @Override
    public List<TableFaildStatisticsVO> getFaildStatistics(Integer lookday) {
        List<TableFaildStatisticsVO> faildStatisticsVOS = pipelTaskLogMapper.getFaildStatistics(lookday,dataServiceDbName);
        List<TableFaildStatisticsVO> list = new ArrayList<>();
        for (TableFaildStatisticsVO faildStatisticsVO : faildStatisticsVOS) {
            if (faildStatisticsVO.sum != 0) {
                faildStatisticsVO.success = 1.0 * faildStatisticsVO.successNum / faildStatisticsVO.sum;
                faildStatisticsVO.faild = 1.0 * faildStatisticsVO.faildNum / faildStatisticsVO.sum;
            }
            list.add(faildStatisticsVO);
        }
        return list;
    }

    @Override
    public List<TableLineChartVO> getLineChart(Integer lookday) {
        List<TableLineChartVO> lineChartVOList = pipelTaskLogMapper.getLineChart(lookday);
        return lineChartVOList;
    }

    @Override
    public List<TableServiceLineChartVO> getDetailLineChart(String tableName, Integer lookday) {
        List<TableServiceLineChartVO> detailLineChartVOList = pipelTaskLogMapper.getDetailLineChart(tableName, lookday,dataServiceDbName);
        return detailLineChartVOList;
    }

    @Override
    public Page<TableServiceDetailVO> getTableServiceDetailLog(TableServiceDetailDTO dto) {
        Page<TableServiceDetailVO> page = dto.page;
        Page<TableServiceDetailVO> tableServiceDetailLog = pipelTaskLogMapper.getTableServiceDetailLog(page, dto,dataServiceDbName);
        List<TableServiceDetailVO> data = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(tableServiceDetailLog.getRecords())) {
            for (TableServiceDetailVO pipelLineDetailVO : tableServiceDetailLog.getRecords()) {
                if (pipelLineDetailVO.runningStatus != null) {
                    if (pipelLineDetailVO.runningStatus.contains("运行成功")) {
                        pipelLineDetailVO.runningResult = "成功";
                    } else if (pipelLineDetailVO.runningStatus.contains("运行失败")) {
                        pipelLineDetailVO.runningResult = "失败";
                    }
                    pipelLineDetailVO.runningStatus = "已完成";
                } else {
                    pipelLineDetailVO.runningResult = "暂无";
                    pipelLineDetailVO.runningStatus = "未完成";
                }
                data.add(pipelLineDetailVO);
            }
        }
        tableServiceDetailLog.setRecords(data);
        return tableServiceDetailLog;
    }

    @Override
    public List<TableServiceDetailVO> getDetailLog() {
        List<TableServiceDetailVO> detailLog = pipelTaskLogMapper.getDetailLog(dataServiceDbName);
        List<TableServiceDetailVO> data = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(detailLog)) {
            for (TableServiceDetailVO tableServiceDetailVO : detailLog) {
                if (tableServiceDetailVO.runningStatus != null) {
                    if (tableServiceDetailVO.runningStatus.contains("运行成功")) {
                        tableServiceDetailVO.runningResult = "成功";
                    } else if (tableServiceDetailVO.runningStatus.contains("运行失败")) {
                        tableServiceDetailVO.runningResult = "失败";
                    }
                    tableServiceDetailVO.runningStatus = "已完成";
                } else {
                    tableServiceDetailVO.runningResult = "暂无";
                    tableServiceDetailVO.runningStatus = "未完成";
                }
                data.add(tableServiceDetailVO);
            }
        }
        return data;
    }
}
