package com.fisk.task.service.nifi.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.davis.client.ApiException;
import com.davis.client.model.ProcessorDTO;
import com.davis.client.model.ProcessorEntity;
import com.davis.client.model.ProcessorRunStatusEntity;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.tableservice.TableServiceDTO;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.mapper.TableNifiSettingMapper;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.service.nifi.ITableNifiSettingService;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author cfk
 */
@Service
@Slf4j
public class TableNifiSettingServiceImpl extends ServiceImpl<TableNifiSettingMapper, TableNifiSettingPO> implements ITableNifiSettingService {

    @Resource
    TableNifiSettingMapper tableNifiSettingMapper;

    @Override
    public TableNifiSettingPO getByTableId(long tableId, long tableType) {
        TableNifiSettingPO tableNifiSettingPO = tableNifiSettingMapper.getByTableId(tableId, tableType);
        return tableNifiSettingPO;
    }

    @Override
    public ResultEntity<TableServiceDTO> enableOrDisable(TableServiceDTO tableServiceDTO) {
        try {
        LambdaQueryWrapper<TableNifiSettingPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableNifiSettingPO::getTableAccessId,tableServiceDTO.getId());
        queryWrapper.eq(TableNifiSettingPO::getType, OlapTableEnum.DATASERVICES.getValue());
        TableNifiSettingPO tableNifiSettingPO = this.getOne(queryWrapper);
        String dispatchComponentId = tableNifiSettingPO.getDispatchComponentId();
        ProcessorEntity processorEntity = NifiHelper.getProcessorsApi().getProcessor(dispatchComponentId);
            ProcessorDTO.StateEnum state = processorEntity.getComponent().getState();

            ProcessorRunStatusEntity processorRunStatusEntity = new ProcessorRunStatusEntity();
        processorRunStatusEntity.setRevision(processorEntity.getRevision());
        processorRunStatusEntity.setDisconnectedNodeAcknowledged(false);
        if (tableServiceDTO.getEnable() == 1){
            tableServiceDTO.setEnable(0);
            if (state != ProcessorDTO.StateEnum.STOPPED){
                processorRunStatusEntity.setState(ProcessorRunStatusEntity.StateEnum.STOPPED);
                NifiHelper.getProcessorsApi().updateRunStatus(dispatchComponentId,processorRunStatusEntity);
            }
        }else if (tableServiceDTO.getEnable() == 0){
            tableServiceDTO.setEnable(1);
            if (state != ProcessorDTO.StateEnum.RUNNING){
                processorRunStatusEntity.setState(ProcessorRunStatusEntity.StateEnum.RUNNING);
                NifiHelper.getProcessorsApi().updateRunStatus(dispatchComponentId,processorRunStatusEntity);
            }
        }
            return ResultEntityBuild.buildData(ResultEnum.SUCCESS,tableServiceDTO);
        } catch (ApiException e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
            return ResultEntityBuild.buildData(ResultEnum.TASK_PUBLISH_ERROR,tableServiceDTO);
        }
    }

}
