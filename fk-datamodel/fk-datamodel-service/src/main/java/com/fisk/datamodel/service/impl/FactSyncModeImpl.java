package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.factsyncmode.FactSyncModeDTO;
import com.fisk.datamodel.dto.factsyncmode.FactSyncModePushDTO;
import com.fisk.datamodel.entity.FactAttributePO;
import com.fisk.datamodel.entity.FactPO;
import com.fisk.datamodel.entity.FactSyncModePO;
import com.fisk.datamodel.enums.SyncModeEnum;
import com.fisk.datamodel.map.FactSyncModeMap;
import com.fisk.datamodel.mapper.FactAttributeMapper;
import com.fisk.datamodel.mapper.FactMapper;
import com.fisk.datamodel.mapper.FactSyncModeMapper;
import com.fisk.datamodel.service.IFactSyncMode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Service
public class FactSyncModeImpl implements IFactSyncMode {

    @Resource
    FactSyncModeMapper mapper;
    @Resource
    FactMapper factMapper;
    @Resource
    FactAttributeMapper factAttributeMapper;

    @Override
    public ResultEnum addFactSyncMode(FactSyncModeDTO dto)
    {
        //判断是否重复添加
        QueryWrapper<FactSyncModePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactSyncModePO::getSyncFactId,dto.syncFactId);
        FactSyncModePO po=mapper.selectOne(queryWrapper);
        if (po !=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        if (dto.syncMode != SyncModeEnum.CUSTOM_OVERRIDE.getValue())
        {
            dto.customDeleteCondition="";
            dto.customInsertCondition="";
        }
        return mapper.insert(FactSyncModeMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public FactSyncModeDTO getFactSyncMode(int factId)
    {
        FactSyncModeDTO dto=new FactSyncModeDTO();
        //判断是否存在
        QueryWrapper<FactSyncModePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactSyncModePO::getSyncFactId,factId);
        FactSyncModePO po=mapper.selectOne(queryWrapper);
        if (po==null)
        {
            return dto;
        }
        dto=FactSyncModeMap.INSTANCES.poToDto(po);
        return dto;
    }

    @Override
    public ResultEnum updateFactSyncMode(FactSyncModeDTO dto)
    {
        FactSyncModePO po=mapper.selectById(dto.id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        if (dto.syncMode != SyncModeEnum.CUSTOM_OVERRIDE.getValue())
        {
            dto.customDeleteCondition="";
            dto.customInsertCondition="";
        }
        return mapper.updateById(FactSyncModeMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public FactSyncModePushDTO factSyncModePush(long id)
    {
        FactSyncModePushDTO dto=new FactSyncModePushDTO();
        FactSyncModePO po=mapper.selectById(id);
        if (po==null)
        {
            return dto;
        }
        dto=FactSyncModeMap.INSTANCES.poToPushDto(po);
        //获取事实表名称
        FactPO factPO=factMapper.selectById(po.syncFactId);
        if (factPO !=null)
        {
            dto.factTableName=factPO.factTableEnName;
        }
        //获取事实字段名称以及字段来源字段id
        FactAttributePO factAttributePO=factAttributeMapper.selectById(po.syncFactFieldId);
        if (factAttributePO !=null)
        {
            dto.factTableField=factAttributePO.factFieldEnName;
            //dto.sourceFieldId=factAttributePO.tableSourceFieldId;
        }
        return dto;
    }

}
