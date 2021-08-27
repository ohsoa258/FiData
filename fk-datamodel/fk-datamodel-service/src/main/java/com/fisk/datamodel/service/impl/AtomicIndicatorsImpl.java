package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.atomicindicator.*;
import com.fisk.datamodel.entity.AtomicIndicatorsPO;
import com.fisk.datamodel.entity.IndicatorsPO;
import com.fisk.datamodel.map.AtomicIndicatorsMap;
import com.fisk.datamodel.mapper.AtomicIndicatorsMapper;
import com.fisk.datamodel.service.IAtomicIndicators;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class AtomicIndicatorsImpl
        extends ServiceImpl<AtomicIndicatorsMapper,IndicatorsPO>
        implements IAtomicIndicators {

    @Resource
    AtomicIndicatorsMapper mapper;

    @Override
    public ResultEnum addAtomicIndicators(List<AtomicIndicatorsDTO> dto)
    {
        //查询原子指标数据
        QueryWrapper<IndicatorsPO> queryWrapper=new QueryWrapper<>();
        boolean repeat=false;
        List<String> nameList=new ArrayList<>();
        for (AtomicIndicatorsDTO item: dto)
        {
            queryWrapper.lambda().eq(IndicatorsPO::getBusinessId,item.businessId)
                    .eq(IndicatorsPO::getIndicatorsName,item.indicatorsName);
            IndicatorsPO po=mapper.selectOne(queryWrapper);
            //判断是否重复
            if (po !=null)
            {
                repeat=true;
                break;
            }
            nameList.add(item.indicatorsName);
        }
        //判断输入是否重复
        HashSet set = new HashSet<>(nameList);
        if (set.size() != dto.size())
        {
            return ResultEnum.DARAMODEL_INPUT_REPEAT;
        }
        //判断是否重复
        if (repeat)
        {
            return ResultEnum.DATA_EXISTS;
        }
        return this.saveBatch(AtomicIndicatorsMap.INSTANCES.dtoToPo(dto))?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
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
