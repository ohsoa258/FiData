package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class TableHistoryImpl
        extends ServiceImpl<TableHistoryMapper,TableHistoryPO>
        implements ITableHistory {

    @Resource
    TableHistoryMapper mapper;

    @Override
    public ResultEnum addTableHistory(List<TableHistoryDTO> dto)
    {

        return this.saveBatch(TableHistoryMap.INSTANCES.dtoListToPoList(dto))?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
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
