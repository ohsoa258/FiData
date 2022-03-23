package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamodel.dto.syncmode.GetTableBusinessDTO;
import com.fisk.datamodel.entity.SyncModePO;
import com.fisk.datamodel.entity.TableBusinessPO;
import com.fisk.datamodel.map.TableBusinessMap;
import com.fisk.datamodel.mapper.SyncModeMapper;
import com.fisk.datamodel.mapper.TableBusinessMapper;
import com.fisk.datamodel.service.ITableBusiness;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Service
public class TableBusinessImpl
        extends ServiceImpl<TableBusinessMapper, TableBusinessPO>
        implements ITableBusiness {

    @Resource
    SyncModeMapper syncModeMapper;
    @Resource
    TableBusinessMapper tableBusinessMapper;

    @Override
    public GetTableBusinessDTO getTableBusiness(int tableId, int tableType)
    {
        GetTableBusinessDTO dto=new GetTableBusinessDTO();
        QueryWrapper<SyncModePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(SyncModePO::getSyncTableId,tableId)
                .eq(SyncModePO::getTableType,tableType);
        SyncModePO po=syncModeMapper.selectOne(queryWrapper);
        if (po==null)
        {
            return dto;
        }
        QueryWrapper<TableBusinessPO> tableBusinessPOQueryWrapper=new QueryWrapper<>();
        tableBusinessPOQueryWrapper.lambda().eq(TableBusinessPO::getSyncId,po.id);
        TableBusinessPO tableBusinessPO=tableBusinessMapper.selectOne(tableBusinessPOQueryWrapper);
        if (tableBusinessPO==null)
        {
            return dto;
        }
        dto.details= TableBusinessMap.INSTANCES.poToDto(tableBusinessPO);
        return dto;
    }

}
