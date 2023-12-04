package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataservice.dto.tablesyncmode.ApiTableSyncModeDTO;
import com.fisk.dataservice.dto.tablesyncmode.TableSyncModeDTO;
import com.fisk.dataservice.entity.TableSyncModePO;
import com.fisk.dataservice.enums.AppServiceTypeEnum;
import com.fisk.dataservice.map.TableSyncModeMap;
import com.fisk.dataservice.mapper.TableSyncModeMapper;
import com.fisk.dataservice.service.ITableSyncMode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class TableSyncModeImpl
        extends ServiceImpl<TableSyncModeMapper, TableSyncModePO>
        implements ITableSyncMode {

    @Resource
    TableSyncModeMapper mapper;

    @Override
    public ResultEnum addApiTableSyncMode(ApiTableSyncModeDTO dto) {
        TableSyncModePO po = TableSyncModeMap.INSTANCES.dtoToPo(dto);
        po.type = AppServiceTypeEnum.API.getValue();
        int flat = mapper.insert(po);
        if (flat == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum tableServiceTableSyncMode(TableSyncModeDTO dto) {
        TableSyncModePO po = this.query().eq("type_table_id", dto.typeTableId)
                .eq("type", dto.type)
                .one();
        if (po == null) {
            TableSyncModePO addPo = TableSyncModeMap.INSTANCES.tableServiceDtoToPo(dto);
            if (mapper.insert(addPo) == 0) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
            return ResultEnum.SUCCESS;
        }
        dto.id = po.id;
        po = TableSyncModeMap.INSTANCES.tableServiceDtoToPo(dto);
        if (mapper.updateById(po) == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public TableSyncModeDTO getTableServiceSyncMode(long tableServiceId, Integer type) {
        TableSyncModePO po = this.query().eq("type_table_id", tableServiceId)
                .eq("type", type)
                .one();
        if (po == null) {
            return null;
        }
        return TableSyncModeMap.INSTANCES.poToDto(po);
    }

    @Override
    public ResultEnum delTableServiceSyncMode(long tableServiceId, Integer type) {
        TableSyncModePO po = this.query().eq("type_table_id", tableServiceId)
                .eq("type", type)
                .one();
        if (po == null) {
            return ResultEnum.SUCCESS;
        }

        if (mapper.deleteByIdWithFill(po) == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEnum.SUCCESS;

    }

    public List<Integer> getTableListByPipelineId(Integer pipelineId,Integer type) {
        LambdaQueryWrapper<TableSyncModePO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableSyncModePO::getAssociatePipe,pipelineId);
        queryWrapper.eq(TableSyncModePO::getType,type);
        List<TableSyncModePO> associatePipeline = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(associatePipeline)) {
            return new ArrayList<>();
        }
        return associatePipeline.stream().map(e -> e.typeTableId).collect(Collectors.toList());
    }

    public List<Integer> getTableListByInputId(Integer inputId,Integer type) {
        LambdaQueryWrapper<TableSyncModePO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableSyncModePO::getAssociateInput,inputId);
        queryWrapper.eq(TableSyncModePO::getType,type);
        List<TableSyncModePO> associatePipeline = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(associatePipeline)) {
            return new ArrayList<>();
        }
        return associatePipeline.stream().map(e -> e.typeTableId).collect(Collectors.toList());
    }

}
