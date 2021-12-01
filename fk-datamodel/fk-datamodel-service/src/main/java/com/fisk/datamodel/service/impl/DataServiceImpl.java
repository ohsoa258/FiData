package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.datamodel.entity.DimensionAttributePO;
import com.fisk.datamodel.entity.FactAttributePO;
import com.fisk.datamodel.entity.IndicatorsPO;
import com.fisk.datamodel.mapper.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.mapper.FactAttributeMapper;
import com.fisk.datamodel.mapper.IndicatorsMapper;
import com.fisk.datamodel.service.IDataService;
import com.fisk.dataservice.dto.isDimensionDTO;
import org.junit.Test;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class DataServiceImpl implements IDataService {
    @Resource
    DimensionAttributeMapper dimensionAttributeMapper;
    @Resource
    IndicatorsMapper indicatorsMapper;
    @Resource
    FactAttributeMapper factAttributeMapper;

    @Override
    public boolean isExistAssociate(isDimensionDTO dto)
    {
        //是维度字段
        if (dto.dimensionOne==1)
        {
            DimensionAttributePO dimensionAttributePO=dimensionAttributeMapper.selectById(dto.fieldIdOne);
            if (dimensionAttributePO==null)
            {
                return false;
            }
            QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
            queryWrapper.select("dimension_id").lambda()
                    .eq(DimensionAttributePO::getDimensionId,dimensionAttributePO.dimensionId);
            List<Integer> dimensionIds1=(List)dimensionAttributeMapper.selectObjs(queryWrapper);
            if (dimensionIds1 !=null && dimensionIds1.size()>0)
            {
                dimensionIds1=dimensionIds1.stream()
                    .distinct().collect(Collectors.toList());
            }
            if (dto.dimensionTwo==1)
            {
                DimensionAttributePO dimensionAttributePO1=dimensionAttributeMapper.selectById(dto.fieldIdTwo);
                if (dimensionAttributePO1==null)
                {
                    return false;
                }
                QueryWrapper<DimensionAttributePO> queryWrapper1=new QueryWrapper<>();
                queryWrapper1.select("associate_dimension_id").lambda()
                        .ne(DimensionAttributePO::getAssociateDimensionId,0)
                        .eq(DimensionAttributePO::getDimensionId,dimensionAttributePO1.dimensionId);
                List<Integer> dimensionIds2=(List)dimensionAttributeMapper.selectObjs(queryWrapper1);
                if (dimensionIds2 !=null && dimensionIds2.size()>0)
                {
                    dimensionIds2=dimensionIds2.stream()
                            .distinct().collect(Collectors.toList());
                }
                //取交集
                dimensionIds1.retainAll(dimensionIds2);
                if (dimensionIds1 !=null && dimensionIds1.size()>0)
                {
                    return true;
                }
            }else {
                IndicatorsPO indicatorsPO=indicatorsMapper.selectById(dto.fieldIdTwo);
                if (indicatorsPO ==null)
                {
                    return false;
                }
                QueryWrapper<FactAttributePO> factAttributePOQueryWrapper=new QueryWrapper();
                factAttributePOQueryWrapper.select("associate_dimension_id").lambda()
                        .ne(FactAttributePO::getAssociateDimensionId,0)
                        .eq(FactAttributePO::getFactId,indicatorsPO.factId);
                List<Integer> factIds=(List)factAttributeMapper.selectObjs(factAttributePOQueryWrapper);
                if (factIds !=null && factIds.size()>0)
                {
                    factIds=factIds.stream()
                            .distinct().collect(Collectors.toList());
                }
                //取交集
                dimensionIds1.retainAll(factIds);
                if (dimensionIds1 !=null && dimensionIds1.size()>0)
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

            DimensionAttributePO dimensionAttributePO1=dimensionAttributeMapper.selectById(dto.fieldIdTwo);
            if (dimensionAttributePO1==null)
            {
                return false;
            }
            QueryWrapper<DimensionAttributePO> queryWrapper1=new QueryWrapper<>();
            queryWrapper1.select("dimension_id").lambda()
                    .eq(DimensionAttributePO::getDimensionId,dimensionAttributePO1.dimensionId);
            List<Integer> dimensionIds=(List)dimensionAttributeMapper.selectObjs(queryWrapper1);
            if (dimensionIds !=null && dimensionIds.size()>0)
            {
                dimensionIds=dimensionIds.stream()
                        .distinct().collect(Collectors.toList());
            }

            IndicatorsPO indicatorsPO=indicatorsMapper.selectById(dto.fieldIdOne);
            if (indicatorsPO ==null)
            {
                return false;
            }
            QueryWrapper<FactAttributePO> factAttributePOQueryWrapper=new QueryWrapper();
            factAttributePOQueryWrapper.select("associate_dimension_id").lambda()
                    .eq(FactAttributePO::getFactId,indicatorsPO.factId);
            List<Integer> factIds=(List)factAttributeMapper.selectObjs(factAttributePOQueryWrapper);
            if (factIds !=null && factIds.size()>0)
            {
                factIds=factIds.stream()
                        .distinct().collect(Collectors.toList());
            }
            //取交集
            dimensionIds.retainAll(factIds);
            if (dimensionIds !=null && dimensionIds.size()>0)
            {
                return true;
            }
        }
        return false;
    }
}
