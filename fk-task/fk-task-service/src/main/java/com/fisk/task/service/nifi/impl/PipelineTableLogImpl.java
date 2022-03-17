package com.fisk.task.service.nifi.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import com.fisk.task.dto.pipeline.PipelineTableLogDTO;
import com.fisk.task.entity.PipelineTableLogPO;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.map.PipelineTableLogMap;
import com.fisk.task.mapper.PipelineTableLogMapper;
import com.fisk.task.service.nifi.IPipelineTableLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    public PipelineTableLogDTO getPipelineTableLog(NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailPO) {
        //获取一个表的状态,分开写
        QueryWrapper<PipelineTableLogPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PipelineTableLogPO::getComponentId, nifiCustomWorkflowDetailPO.id);
        //
        PipelineTableLogPO pipelineTableLogPO = pipelineTableLogMapper.selectOne(queryWrapper);
        PipelineTableLogDTO pipelineTableLogDTO =PipelineTableLogMap.INSTANCES.poToDto(pipelineTableLogPO);
        return pipelineTableLogDTO;
    }

    @Override
    public List<PipelineTableLogDTO> getPipelineTableLogs(List<NifiCustomWorkflowDetailDTO> nifiCustomWorkflowDetailDTOs) {
        List<PipelineTableLogDTO> pipelineTableLogPOS = new ArrayList<>();
        for (NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO : nifiCustomWorkflowDetailDTOs) {
            PipelineTableLogDTO pipelineTableLog = this.getPipelineTableLog(nifiCustomWorkflowDetailDTO);
            pipelineTableLogPOS.add(pipelineTableLog);
        }
        return pipelineTableLogPOS;
    }

    @Override
    public NifiCustomWorkflowVO getNifiCustomWorkflowDetail(NifiCustomWorkflowVO nifiCustomWorkflow) {
        List<Long> componentIds = nifiCustomWorkflow.componentIds;
        NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO = new NifiCustomWorkflowDetailDTO();
        List<PipelineTableLogDTO> pipelineTableLogs = new ArrayList<>();
        for (Long id : componentIds) {
            nifiCustomWorkflowDetailDTO.id = id;
            //PipelineTableLogPO pipelineTableLog =
            PipelineTableLogDTO pipelineTableLog = this.getPipelineTableLog(nifiCustomWorkflowDetailDTO);

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


}
