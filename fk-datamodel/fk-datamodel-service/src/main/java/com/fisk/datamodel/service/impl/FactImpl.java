package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.fact.FactDTO;
import com.fisk.datamodel.entity.FactPO;
import com.fisk.datamodel.map.FactMap;
import com.fisk.datamodel.mapper.FactMapper;
import com.fisk.datamodel.service.IFact;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Service
public class FactImpl implements IFact {

    @Resource
    FactMapper mapper;

    @Override
    public ResultEnum addFact(FactDTO dto)
    {
        QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactPO::getBusinessProcessId,dto.businessProcessId)
                .eq(FactPO::getFactTableName,dto.factTableName);
        FactPO po=mapper.selectOne(queryWrapper);
        if (po!=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        FactPO model= FactMap.INSTANCES.dtoToPo(dto);
        return mapper.insert(model)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteFact(int id)
    {
        FactPO po=mapper.selectById(id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return mapper.deleteByIdWithFill(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public FactDTO getFact(int id)
    {
        return FactMap.INSTANCES.poToDto(mapper.selectById(id));
    }

    @Override
    public ResultEnum updateFact(FactDTO dto)
    {
        FactPO po=mapper.selectById(dto.id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return mapper.updateById(FactMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public IPage<FactDTO> getFactList(QueryDTO dto)
    {
        QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactPO::getBusinessProcessId,dto.id);
        Page<FactPO> data=new Page<>(dto.getPage(),dto.getSize());
        return FactMap.INSTANCES.pagePoToDto(mapper.selectPage(data,queryWrapper.select().orderByDesc("create_time")));
    }

}
