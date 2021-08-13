package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.atomicindicator.*;
import com.fisk.datamodel.entity.AtomicIndicatorsPO;
import com.fisk.datamodel.entity.FactAttributePO;
import com.fisk.datamodel.entity.FactPO;
import com.fisk.datamodel.map.AtomicIndicatorsMap;
import com.fisk.datamodel.mapper.AtomicIndicatorsMapper;
import com.fisk.datamodel.mapper.FactAttributeMapper;
import com.fisk.datamodel.mapper.FactMapper;
import com.fisk.datamodel.service.IAtomicIndicators;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class AtomicIndicatorsImpl implements IAtomicIndicators {

    @Resource
    AtomicIndicatorsMapper mapper;
    @Resource
    FactAttributeMapper factAttributeMapper;

    @Override
    public ResultEnum addAtomicIndicators(AtomicIndicatorsDTO dto)
    {
        //查询原子指标数据
        QueryWrapper<AtomicIndicatorsPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(AtomicIndicatorsPO::getFactAttributeId,dto.factAttributeId)
                .eq(AtomicIndicatorsPO::getCalculationLogic,dto.calculationLogic)
                .eq(AtomicIndicatorsPO::getIndicatorsName,dto.indicatorsName);
        AtomicIndicatorsPO po=mapper.selectOne(queryWrapper);
        //判断是否重复
        if (po !=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        //查询factId
        FactAttributePO factAttributePO=factAttributeMapper.selectById(dto.factAttributeId);
        if (factAttributePO==null)
        {
            return  ResultEnum.DATA_NOTEXISTS;
        }
        AtomicIndicatorsPO model=AtomicIndicatorsMap.INSTANCES.dtoToPo(dto);
        model.factId=factAttributePO.factId;
        return mapper.insert(model)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteAtomicIndicators(int id)
    {
        AtomicIndicatorsPO po=mapper.selectById(id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return mapper.deleteByIdWithFill(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public AtomicIndicatorsDetailDTO getAtomicIndicatorDetails(int id)
    {
        return mapper.atomicIndicatorsDetailDTO(id);
    }

    @Override
    public ResultEnum updateAtomicIndicatorDetails(AtomicIndicatorsDTO dto)
    {
        AtomicIndicatorsPO po=mapper.selectById(dto.id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return mapper.updateById(AtomicIndicatorsMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public Page<AtomicIndicatorsResultDTO> getAtomicIndicatorList(AtomicIndicatorsQueryDTO dto)
    {
        return mapper.queryList(dto.page,dto);
    }

    @Override
    public List<AtomicIndicatorDropListDTO> atomicIndicatorDropList(int factId)
    {
        QueryWrapper<AtomicIndicatorsPO> queryWrapper=new QueryWrapper<>();
        if (factId !=0)
        {
            queryWrapper.lambda().eq(AtomicIndicatorsPO::getFactId,factId);
        }
        return AtomicIndicatorsMap.INSTANCES.poToDtoList(mapper.selectList(queryWrapper));
    }

}
