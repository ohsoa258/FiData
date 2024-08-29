package com.fisk.task.service.pipeline.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.dto.accessmdm.LogResultDTO;
import com.fisk.task.dto.etllog.EtlLogDTO;
import com.fisk.task.entity.TBETLlogPO;
import com.fisk.task.mapper.TBETLLogMapper;
import com.fisk.task.service.pipeline.IEtlLog;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EtlLogImpl extends ServiceImpl<TBETLLogMapper, TBETLlogPO> implements IEtlLog {


    /**
     * 获取数据接入表最后同步时间
     *
     * @param tblNames
     * @return
     */
    @Override
    public LocalDateTime getAccessTblLastSyncTime(List<String> tblNames) {
        List<EtlLogDTO> etlLogs = new ArrayList<>();
        for (String tblName : tblNames) {
            List<TBETLlogPO> pos = this.list(new LambdaQueryWrapper<TBETLlogPO>()
                    .select(TBETLlogPO::getStartdate)
                    .eq(TBETLlogPO::getTablename, tblName)
                    .isNotNull(TBETLlogPO::getStartdate)
                    .orderByDesc(TBETLlogPO::getStartdate));

            EtlLogDTO etlLogDTO = new EtlLogDTO();
            if (CollectionUtils.isNotEmpty(pos)){
                etlLogDTO.setStartdate(pos.get(0).getStartdate());
                etlLogs.add(etlLogDTO);
            }
        }

        Optional<EtlLogDTO> max = etlLogs.stream().max(Comparator.comparing(EtlLogDTO::getStartdate));
        // 使用orElse提供一个默认值，例如null或者其他默认日期
        return max.map(EtlLogDTO::getStartdate)
                .orElse(null);

    }

    /**
     * 主数据获取发布日志结果
     * @param subRunIds
     * @return
     */
    @Override
    public List<LogResultDTO> getMdmTblNifiLog(@RequestBody List<String> subRunIds){
        LambdaQueryWrapper<TBETLlogPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TBETLlogPO::getCode,subRunIds);
        List<TBETLlogPO> pos = this.list(queryWrapper);
        List<LogResultDTO> result = pos.stream().map(i -> {
            LogResultDTO logResultDTO = new LogResultDTO();
            logResultDTO.setState(i.status);
            logResultDTO.setStartTime(i.startdate);
            logResultDTO.setEndTime(i.enddate);
            logResultDTO.setDataRows(i.datarows);
            logResultDTO.setErrorMsg(i.errordesc);
            logResultDTO.setSubRunId(i.code);
            return logResultDTO;
        }).collect(Collectors.toList());
        return result;
    }

}
