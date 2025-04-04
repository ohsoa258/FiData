package com.fisk.task.service.nifi.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import com.fisk.task.dto.pipeline.PipelineTableLogDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import com.fisk.task.dto.query.PipelineTableQueryDTO;
import com.fisk.task.entity.PipelineTableLogPO;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.map.PipelineTableLogMap;
import com.fisk.task.mapper.PipelineTableLogMapper;
import com.fisk.task.service.nifi.IPipelineTableLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cfk
 */
@Service
@Slf4j
public class PipelineTableLogImpl extends ServiceImpl<PipelineTableLogMapper, PipelineTableLogPO> implements IPipelineTableLog {

    @Resource
    PipelineTableLogMapper pipelineTableLogMapper;

    @Override
    public List<PipelineTableLogDTO> getPipelineTableLog(NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailPO) {
        //获取一个表的状态,分开写
        QueryWrapper<PipelineTableLogPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PipelineTableLogPO::getComponentId, nifiCustomWorkflowDetailPO.id);
        //
        List<PipelineTableLogPO> pipelineTableLogs = pipelineTableLogMapper.selectList(queryWrapper);
        List<PipelineTableLogDTO> pipelineTableLogDtos = PipelineTableLogMap.INSTANCES.listPoToDto(pipelineTableLogs);
        return pipelineTableLogDtos;
    }

    @Override
    public List<PipelineTableLogDTO> getPipelineTableLogs(List<NifiCustomWorkflowDetailDTO> nifiCustomWorkflowDetailDTOs) {
        List<PipelineTableLogDTO> pipelineTableLogPOS = new ArrayList<>();
        for (NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO : nifiCustomWorkflowDetailDTOs) {
            List<PipelineTableLogDTO> pipelineTableLog = this.getPipelineTableLog(nifiCustomWorkflowDetailDTO);
            pipelineTableLogPOS.addAll(pipelineTableLog);
        }
        return pipelineTableLogPOS.stream().sorted(Comparator.comparing(PipelineTableLogDTO::getCreateTime).reversed()).collect(Collectors.toList());
    }

    @Override
    public NifiCustomWorkflowVO getNifiCustomWorkflowDetail(NifiCustomWorkflowVO nifiCustomWorkflow) {
        List<Long> componentIds = nifiCustomWorkflow.componentIds;
        NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO = new NifiCustomWorkflowDetailDTO();
        List<PipelineTableLogDTO> pipelineTableLogs = new ArrayList<>();
        for (Long id : componentIds) {
            PipelineTableLogDTO pipelineTableLog = pipelineTableLogMapper.selectByComponentId(id);

            if (pipelineTableLog != null) {
                pipelineTableLogs.add(pipelineTableLog);
            }
        }
        //List<Integer> status = pipelineTableLogs.stream().map(a -> a.state).collect(Collectors.toList());
        int statu = NifiStageTypeEnum.NOT_RUN.getValue();
        //一方通行
        if (pipelineTableLogs.size() != 0) {
            boolean statuResolution = true;
            for (PipelineTableLogDTO pipelineTableLog : pipelineTableLogs) {
                if (Objects.equals(pipelineTableLog.state, NifiStageTypeEnum.RUN_FAILED.getValue())) {
                    statu = NifiStageTypeEnum.RUN_FAILED.getValue();
                    statuResolution = false;
                    break;
                }
            }
            if (statuResolution) {
                for (PipelineTableLogDTO pipelineTableLog : pipelineTableLogs) {
                    if (Objects.equals(pipelineTableLog.state, NifiStageTypeEnum.RUNNING.getValue())) {
                        statu = NifiStageTypeEnum.RUNNING.getValue();
                        statuResolution = false;
                        break;
                    }
                }
            }
            if (statuResolution) {
                statu = NifiStageTypeEnum.RUNNING.getValue();
            }
        }
        nifiCustomWorkflow.breathingLamp = statu;
        return nifiCustomWorkflow;
    }

    @Override
    public List<NifiCustomWorkflowVO> getNifiCustomWorkflowDetails(List<NifiCustomWorkflowVO> nifiCustomWorkflows) {
        for (NifiCustomWorkflowVO nifiCustomWorkflowVO : nifiCustomWorkflows) {
            this.getNifiCustomWorkflowDetail(nifiCustomWorkflowVO);
        }
        return nifiCustomWorkflows;
    }

    @Override
    public List<PipelineTableLogVO> getPipelineTableLogs(String data, String pipelineTableQuery) {
        PipelineTableQueryDTO pipelineTableQueryDto = JSON.parseObject(pipelineTableQuery, PipelineTableQueryDTO.class);
        List<PipelineTableLogVO> pipelineTableLogs = JSON.parseArray(data, PipelineTableLogVO.class);
        String key = pipelineTableQueryDto.keyword;
        List<PipelineTableLogVO> pipelineTableLogVos = new ArrayList<>();
        for (PipelineTableLogVO pipelineTableLog : pipelineTableLogs) {
            OlapTableEnum tableType = pipelineTableLog.tableType;
            Long tableId = pipelineTableLog.tableId;
            log.info("表类别:{},表id:{}", tableType.getName(), tableId);
            List<PipelineTableLogVO> pipelineTableLogs1 = pipelineTableLogMapper.getPipelineTableLogs(Math.toIntExact(tableId), tableType.getValue(), pipelineTableQueryDto.keyword);

            List<PipelineTableLogVO> collect = pipelineTableLogs1.stream().map(
                    e -> {
                        e.tableName = pipelineTableLog.tableName;
                        e.tableType = pipelineTableLog.tableType;
                        e.appId = pipelineTableLog.appId;
                        return e;
                    }
            ).collect(Collectors.toList());
//
//            filter(f -> {
//                if (StringUtils.isNotBlank(key)) {
//
//                    f.tableName.equalsIgnoreCase(key);
//                    f.comment.equalsIgnoreCase(key);
//                    (f.dispatchType + "").equalsIgnoreCase(key);
//                    (f.state + "").equalsIgnoreCase(key);
//                }
//                return true;
//            })

            if (StringUtils.isNotBlank(key)) {
//                collect.stream()
//                        .filter(e -> e.tableName.contains(key))
//                        .filter(e -> e.comment.contains(key))
//                        .filter(e -> (e.dispatchType + "").contains(key))
//                        .filter(e -> (e.state + "").contains(key)).collect(Collectors.toList());

                List<PipelineTableLogVO> collect1 = collect.stream().filter(e -> e.tableName.contains(key)).collect(Collectors.toList());
                List<PipelineTableLogVO> collect2 = collect.stream().filter(e -> e.comment.contains(key)).collect(Collectors.toList());
                List<PipelineTableLogVO> collect3 = collect.stream().filter(e -> (e.dispatchType + "").contains(key)).collect(Collectors.toList());
                List<PipelineTableLogVO> collect4 = collect.stream().filter(e -> (e.state + "").contains(key)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect1)) {

                    pipelineTableLogVos.addAll(collect1);
                }
                if (CollectionUtils.isNotEmpty(collect2)) {

                    pipelineTableLogVos.addAll(collect2);
                }
                if (CollectionUtils.isNotEmpty(collect3)) {

                    pipelineTableLogVos.addAll(collect3);
                }
                if (CollectionUtils.isNotEmpty(collect4)) {

                    pipelineTableLogVos.addAll(collect4);
                }

            } else {

                pipelineTableLogVos.addAll(collect);
            }
        }
        return pipelineTableLogVos.stream().sorted(Comparator.comparing(PipelineTableLogVO::getStartTime).reversed()).collect(Collectors.toList());
    }

    /**
     * 获取数据接入应用下的实时表最后同步时间
     *
     * @param tblIds
     * @return
     */
    @Override
    public LocalDateTime getRealTimeTblLastSyncTime(List<Long> tblIds) {
        List<Date> times = new ArrayList<>();
        for (Long tblId : tblIds) {
            List<PipelineTableLogPO> pos = this.list(new LambdaQueryWrapper<PipelineTableLogPO>()
                    .select(PipelineTableLogPO::getStartTime)
                    .eq(PipelineTableLogPO::getTableId, tblId)
                    .eq(PipelineTableLogPO::getTableType, OlapTableEnum.PHYSICS_RESTAPI.getValue())
                    .isNotNull(PipelineTableLogPO::getStartTime)
                    .orderByDesc(PipelineTableLogPO::getStartTime));

            if (CollectionUtils.isNotEmpty(pos)){
                times.add(pos.get(0).getStartTime());
            }
        }
        Optional<Date> max = times.stream().max(Comparator.comparing(Date::getTime));
        return max.map(date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()).orElse(null);
    }


}
