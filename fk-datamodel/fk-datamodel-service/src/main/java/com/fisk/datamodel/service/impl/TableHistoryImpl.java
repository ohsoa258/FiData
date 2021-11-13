package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.tablehistory.TableHistoryDTO;
import com.fisk.datamodel.dto.tablehistory.TableHistoryQueryDTO;
import com.fisk.datamodel.entity.TableHistoryPO;
import com.fisk.datamodel.map.TableHistoryMap;
import com.fisk.datamodel.mapper.TableHistoryMapper;
import com.fisk.datamodel.service.ITableHistory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class TableHistoryImpl implements ITableHistory {

    @Resource
    TableHistoryMapper mapper;

    @Override
    public ResultEnum addTableHistory(TableHistoryDTO dto)
    {
        return mapper.insert(TableHistoryMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<TableHistoryDTO> getTableHistoryList(TableHistoryQueryDTO dto)
    {
        QueryWrapper<TableHistoryPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(TableHistoryPO::getTableId,dto.tableId)
                .eq(TableHistoryPO::getTableType,dto.tableType);
        return TableHistoryMap.INSTANCES.poListToDtoList(mapper.selectList(queryWrapper));
    }

}
