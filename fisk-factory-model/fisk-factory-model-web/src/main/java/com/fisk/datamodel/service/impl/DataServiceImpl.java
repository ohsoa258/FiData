package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.datamodel.dto.atomicindicator.DimensionTimePeriodDTO;
import com.fisk.datamodel.entity.DimensionAttributePO;
import com.fisk.datamodel.entity.DimensionPO;
import com.fisk.datamodel.entity.FactAttributePO;
import com.fisk.datamodel.entity.IndicatorsPO;
import com.fisk.datamodel.mapper.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.mapper.FactAttributeMapper;
import com.fisk.datamodel.mapper.IndicatorsMapper;
import com.fisk.datamodel.service.IDataService;
import com.fisk.chartvisual.dto.chartvisual.IsDimensionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class DataServiceImpl implements IDataService {
    @Resource
    DimensionAttributeMapper dimensionAttributeMapper;
    @Resource
    IndicatorsMapper indicatorsMapper;
    @Resource
    FactAttributeMapper factAttributeMapper;
    @Resource
    DimensionMapper dimensionMapper;

    @Override
    public boolean isExistAssociate(IsDimensionDTO dto)
    {
        //是维度字段
        if (dto.dimensionOne==1)
        {
            DimensionAttributePO dimensionAttributePo=dimensionAttributeMapper.selectById(dto.fieldIdOne);
            if (dimensionAttributePo==null)
            {
                return false;
            }
            QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
            queryWrapper.select("dimension_id").lambda().eq(DimensionAttributePO::getDimensionId,dimensionAttributePo.dimensionId);
            List<Integer> dimensionIds1=(List)dimensionAttributeMapper.selectObjs(queryWrapper);
            if (!CollectionUtils.isEmpty(dimensionIds1))
            {
                dimensionIds1=dimensionIds1.stream().distinct().collect(Collectors.toList());
            }
            if (dto.dimensionTwo==1)
            {
                DimensionAttributePO dimensionAttributePo1=dimensionAttributeMapper.selectById(dto.fieldIdTwo);
                if (dimensionAttributePo1==null)
                {
                    return false;
                }
                QueryWrapper<DimensionAttributePO> queryWrapper1=new QueryWrapper<>();
                queryWrapper1.select("associate_dimension_id").lambda().ne(DimensionAttributePO::getAssociateDimensionId,0)
                        .eq(DimensionAttributePO::getDimensionId,dimensionAttributePo1.dimensionId);
                List<Integer> dimensionIds2=(List)dimensionAttributeMapper.selectObjs(queryWrapper1);
                if (!CollectionUtils.isEmpty(dimensionIds2))
                {
                    dimensionIds2=dimensionIds2.stream().distinct().collect(Collectors.toList());
                }
                //取交集
                dimensionIds1.retainAll(dimensionIds2);
                if (!CollectionUtils.isEmpty(dimensionIds1))
                {
                    return true;
                }
            }else {
                IndicatorsPO indicatorsPo=indicatorsMapper.selectById(dto.fieldIdTwo);
                if (indicatorsPo ==null)
                {
                    return false;
                }
                QueryWrapper<FactAttributePO> factAttributePoQueryWrapper=new QueryWrapper();
                factAttributePoQueryWrapper.select("associate_dimension_id").lambda().ne(FactAttributePO::getAssociateDimensionId,0)
                        .eq(FactAttributePO::getFactId,indicatorsPo.factId);
                List<Integer> factIds=(List)factAttributeMapper.selectObjs(factAttributePoQueryWrapper);
                if (!CollectionUtils.isEmpty(factIds))
                {
                    factIds=factIds.stream().distinct().collect(Collectors.toList());
                }
                //取交集
                dimensionIds1.retainAll(factIds);
                if (!CollectionUtils.isEmpty(dimensionIds1))
                {
                    return true;
                }
            }
            return false;
        }else {
            //不是维度直接返回，因为事实与事实不存在关联
            if (dto.dimensionTwo!=1)
            {
                return false;
            }
            DimensionAttributePO dimensionAttributePo1=dimensionAttributeMapper.selectById(dto.fieldIdTwo);
            if (dimensionAttributePo1==null)
            {
                return false;
            }
            QueryWrapper<DimensionAttributePO> queryWrapper1=new QueryWrapper<>();
            queryWrapper1.select("dimension_id").lambda().eq(DimensionAttributePO::getDimensionId,dimensionAttributePo1.dimensionId);
            List<Integer> dimensionIds=(List)dimensionAttributeMapper.selectObjs(queryWrapper1);
            if (!CollectionUtils.isEmpty(dimensionIds))
            {
                dimensionIds=dimensionIds.stream().distinct().collect(Collectors.toList());
            }
            IndicatorsPO indicatorsPo=indicatorsMapper.selectById(dto.fieldIdOne);
            if (indicatorsPo ==null)
            {
                return false;
            }
            QueryWrapper<FactAttributePO> factAttributePoQueryWrapper=new QueryWrapper();
            factAttributePoQueryWrapper.select("associate_dimension_id").lambda().eq(FactAttributePO::getFactId,indicatorsPo.factId);
            List<Integer> factIds=(List)factAttributeMapper.selectObjs(factAttributePoQueryWrapper);
            if (!CollectionUtils.isEmpty(factIds))
            {
                factIds=factIds.stream().distinct().collect(Collectors.toList());
            }
            //取交集
            dimensionIds.retainAll(factIds);
            if (!CollectionUtils.isEmpty(dimensionIds))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public DimensionTimePeriodDTO getDimensionDate(int indicatorsId)
    {
        DimensionTimePeriodDTO dto=new DimensionTimePeriodDTO();
        IndicatorsPO indicatorsPo=indicatorsMapper.selectById(indicatorsId);
        if (indicatorsPo==null)
        {
            return dto;
        }
        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionPO::getBusinessId,indicatorsPo.businessId)
                .eq(DimensionPO::getIsDimDateTbl,true);
        List<DimensionPO> dimensionPoList=dimensionMapper.selectList(queryWrapper);
        if (dimensionPoList ==null || dimensionPoList.size()==0)
        {
            return dto;
        }
        //查询日期字段表字段
        List<Integer> dimensionIds=(List) dimensionMapper.selectObjs(queryWrapper.select("id"));
        QueryWrapper<DimensionAttributePO> attributePoQueryWrapper=new QueryWrapper<>();
        attributePoQueryWrapper.in("dimension_id",dimensionIds).lambda()
                .eq(DimensionAttributePO::getIsDimDateField,true);
        List<DimensionAttributePO> dimensionAttributePoList=dimensionAttributeMapper.selectList(attributePoQueryWrapper);
        if (dimensionAttributePoList ==null && dimensionAttributePoList.size()==0)
        {
            return dto;
        }
        dto.dimensionTabName=dimensionPoList.get(0).dimensionTabName;
        dto.fieldId=dimensionAttributePoList.get(0).id;
        dto.dimensionAttributeField=dimensionAttributePoList.get(0).dimensionFieldEnName;
        return dto;
    }

    @Override
    public List<String> getDimensionFieldNameList(String tableName)
    {
        List<String> nameList=new ArrayList<>();
        try {
            QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.lambda().eq(DimensionPO::getDimensionTabName,tableName);
            DimensionPO po=dimensionMapper.selectOne(queryWrapper);
            if (po==null)
            {
                return nameList;
            }
            QueryWrapper<DimensionAttributePO> attributePoQueryWrapper=new QueryWrapper<>();
            attributePoQueryWrapper.select("dimension_field_en_name").lambda()
                    .eq(DimensionAttributePO::getDimensionId,po.id);
            nameList=(List)dimensionAttributeMapper.selectObjs(attributePoQueryWrapper);
        }
        catch (Exception e)
        {
            log.error("getDimensionFieldNameList:"+e);
        }
        return nameList;
    }

}
