package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.atomicindicator.*;
import com.fisk.datamodel.entity.*;
import com.fisk.datamodel.enums.FactAttributeEnum;
import com.fisk.datamodel.enums.IndicatorsTypeEnum;
import com.fisk.datamodel.map.AtomicIndicatorsMap;
import com.fisk.datamodel.mapper.*;
import com.fisk.datamodel.service.IAtomicIndicators;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class AtomicIndicatorsImpl
        extends ServiceImpl<AtomicIndicatorsMapper,IndicatorsPO>
        implements IAtomicIndicators {

    @Resource
    AtomicIndicatorsMapper mapper;
    @Resource
    FactAttributeMapper factAttributeMapper;
    @Resource
    DimensionMapper dimensionMapper;
    @Resource
    IndicatorsMapper indicatorsMapper;
    @Resource
    FactMapper factMapper;
    @Resource
    BusinessAreaMapper businessAreaMapper;
    @Resource
    BusinessProcessMapper businessProcessMapper;

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

    @Override
    public List<AtomicIndicatorFactDTO> atomicIndicatorPush(int businessAreaId)
    {
        List<AtomicIndicatorFactDTO> list=new ArrayList<>();
        //判断业务域是否存在
        BusinessAreaPO businessAreaPO=businessAreaMapper.selectById(businessAreaId);
        if (businessAreaPO==null)
        {
            return list;
        }
        //获取业务域下的所有业务过程
        QueryWrapper<BusinessProcessPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessProcessPO::getBusinessId,businessAreaId);
        List<BusinessProcessPO> businessProcessPOList=businessProcessMapper.selectList(queryWrapper);
        if (businessProcessPOList==null || businessProcessPOList.size()==0)
        {
            return list;
        }
        for (BusinessProcessPO item:businessProcessPOList)
        {
            AtomicIndicatorFactDTO data=new AtomicIndicatorFactDTO();
            QueryWrapper<FactPO> factPOQueryWrapper=new QueryWrapper<>();
            factPOQueryWrapper.lambda().eq(FactPO::getBusinessProcessId,item.id);
            List<FactPO> factPOList=factMapper.selectList(factPOQueryWrapper);
            if (factPOList==null || factPOList.size()==0)
            {
                break;
            }
            for (FactPO factPO:factPOList)
            {
                data.factTable=factPO.factTableEnName;
                List<AtomicIndicatorPushDTO> atomicIndicator=getAtomicIndicator((int)factPO.id);
                if (atomicIndicator!=null)
                {
                    data.list=atomicIndicator;
                    list.add(data);
                }
            }

        }
        return list;
    }

    /**
     * 根据事实表获取所有原子指标
     * @param factId
     * @return
     */
    public List<AtomicIndicatorPushDTO> getAtomicIndicator(int factId)
    {
        List<AtomicIndicatorPushDTO> data=new ArrayList<>();
        //获取事实表关联的维度
        QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("associate_dimension_id").lambda().eq(FactAttributePO::getFactId,factId)
                .eq(FactAttributePO::getAttributeType, FactAttributeEnum.ASSOCIATED_DIMENSION.getValue());
        List<Object> list=factAttributeMapper.selectObjs(queryWrapper);
        List<Integer> ids= (List<Integer>)(List)list.stream().distinct().collect(Collectors.toList());
        if (ids==null || ids.size()==0)
        {
            return null;
        }
        QueryWrapper<DimensionPO> dimensionQueryWrapper=new QueryWrapper<>();
        dimensionQueryWrapper.in("id",ids);
        List<DimensionPO> dimensionPOList=dimensionMapper.selectList(dimensionQueryWrapper);
        for (DimensionPO item:dimensionPOList)
        {
            AtomicIndicatorPushDTO dto=new AtomicIndicatorPushDTO();
            dto.attributeType=FactAttributeEnum.ASSOCIATED_DIMENSION.getValue();
            dto.dimensionTableName=item.dimensionTabName;
            data.add(dto);
        }
        //获取事实表下所有原子指标
        QueryWrapper<IndicatorsPO> indicatorsQueryWrapper=new QueryWrapper<>();
        indicatorsQueryWrapper.lambda().eq(IndicatorsPO::getFactId,factId)
                .eq(IndicatorsPO::getIndicatorsType, IndicatorsTypeEnum.ATOMIC_INDICATORS.getValue());
        List<IndicatorsPO> indicatorsPO=indicatorsMapper.selectList(indicatorsQueryWrapper);
        for (IndicatorsPO item:indicatorsPO)
        {
            AtomicIndicatorPushDTO dto=new AtomicIndicatorPushDTO();
            dto.atomicIndicatorName=item.indicatorsName;
            dto.aggregationLogic=item.calculationLogic;
            //获取聚合字段
            FactAttributePO factAttributePO=factAttributeMapper.selectById(item.factAttributeId);
            dto.aggregatedField=factAttributePO==null?"":factAttributePO.factFieldEnName;
            data.add(dto);
        }
        return data;
    }

}
