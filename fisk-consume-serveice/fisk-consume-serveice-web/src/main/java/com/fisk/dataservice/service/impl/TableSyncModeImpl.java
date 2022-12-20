package com.fisk.dataservice.service.impl;

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
                .eq("type", AppServiceTypeEnum.TABLE.getValue())
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
    public TableSyncModeDTO getTableServiceSyncMode(long tableServiceId) {
        TableSyncModePO po = this.query().eq("type_table_id", tableServiceId)
                .eq("type", AppServiceTypeEnum.TABLE.getValue())
                .one();
        if (po == null) {
            return null;
        }
        return TableSyncModeMap.INSTANCES.poToDto(po);
    }

}
