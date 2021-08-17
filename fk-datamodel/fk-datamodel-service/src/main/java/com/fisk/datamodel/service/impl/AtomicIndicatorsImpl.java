package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.atomicindicator.*;
import com.fisk.datamodel.entity.AtomicIndicatorsPO;
import com.fisk.datamodel.entity.FactAttributePO;
import com.fisk.datamodel.entity.FactPO;
import com.fisk.datamodel.entity.IndicatorsPO;
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

    @Override
    public ResultEnum addAtomicIndicators(AtomicIndicatorsDTO dto)
    {
        //查询原子指标数据
        QueryWrapper<IndicatorsPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(IndicatorsPO::getBusinessId,dto.businessId)
                .eq(IndicatorsPO::getIndicatorsName,dto.indicatorsName);
        IndicatorsPO po=mapper.selectOne(queryWrapper);
        //判断是否重复
        if (po !=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        return mapper.insert(AtomicIndicatorsMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteAtomicIndicators(int id)
    {
        IndicatorsPO po=mapper.selectById(id);
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
        IndicatorsPO po=mapper.selectById(dto.id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<IndicatorsPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(IndicatorsPO::getBusinessId,dto.businessId)
                .eq(IndicatorsPO::getIndicatorsName,dto.indicatorsName);
        IndicatorsPO model=mapper.selectOne(queryWrapper);
        if (model !=null && model.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        po.indicatorsName=dto.indicatorsName;
        po.indicatorsDes=dto.indicatorsDes;
        po.calculationLogic=dto.calculationLogic;
        return mapper.updateById(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public Page<AtomicIndicatorsResultDTO> getAtomicIndicatorList(AtomicIndicatorsQueryDTO dto)
    {
        return mapper.queryList(dto.page,dto);
    }

    @Override
    public List<AtomicIndicatorDropListDTO> atomicIndicatorDropList(int factId)
    {
        QueryWrapper<IndicatorsPO> queryWrapper=new QueryWrapper<>();
        if (factId !=0)
        {
            queryWrapper.lambda().eq(IndicatorsPO::getFactId,factId);
        }
        return AtomicIndicatorsMap.INSTANCES.poToDtoList(mapper.selectList(queryWrapper));
    }

}
