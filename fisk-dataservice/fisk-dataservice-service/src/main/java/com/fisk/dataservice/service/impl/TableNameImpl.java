package com.fisk.dataservice.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.InputParameterDTO;
import com.fisk.dataservice.dto.OutParameterDTO;
import com.fisk.dataservice.dto.TableDataDTO;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
import com.fisk.dataservice.mapper.*;
import com.fisk.dataservice.service.ITableName;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Service
@Slf4j
public class TableNameImpl implements ITableName {

    @Resource
    DimensionAttributeMapper dimensionAttributeMapper;
    @Resource
    DimensionMapper dimensionMapper;
    @Resource
    IndicatorsMapper indicatorsMapper;
    @Resource
    FactMapper factMapper;
    @Resource
    FactAttributeMapper factAttributeMapper;

    @DS("datamodel")
    @Override
    public ResultEntity<TableDataDTO> getTableName(Integer id, DataDoFieldTypeEnum type, String field,int isDimension) {
        TableDataDTO tableDataDTO = new TableDataDTO();
        DimensionPO dimension;
        switch (type) {
            // 值
            case VALUE:
                IndicatorsPO indicators = indicatorsMapper.selectById(id);
                if (indicators == null) {
                    return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
                }

                FactPO fact = factMapper.selectById(indicators.getFactId());
                FactAttributePO factAttribute = factAttributeMapper.selectById(indicators.getFactAttributeId());

                if (fact != null){
                    tableDataDTO.tableName = fact.getFactTableEnName();
                }

                if (factAttribute != null){
                    tableDataDTO.relationId = factAttribute.associateDimensionId;
                }
                tableDataDTO.id = id;
                tableDataDTO.type = DataDoFieldTypeEnum.VALUE;
                tableDataDTO.tableField = field;
                tableDataDTO.dimension = isDimension;
                tableDataDTO.tableField = factAttribute.getFactFieldEnName();
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, tableDataDTO);
            case SLICER:
            case WHERE:
            case COLUMN:
                DimensionAttributePO po = dimensionAttributeMapper.selectById(id);
                if (po == null) {
                    return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
                }
                dimension = dimensionMapper.selectById(po.getDimensionId());
                if (type == DataDoFieldTypeEnum.WHERE) {
                    tableDataDTO.type = DataDoFieldTypeEnum.WHERE;
                }else {
                    tableDataDTO.tableNameKey = dimension.getDimensionTabName()+"_key";
                    tableDataDTO.type = DataDoFieldTypeEnum.COLUMN;
                }
                tableDataDTO.id = id;
                tableDataDTO.tableField = field;
                tableDataDTO.tableName = dimension.getDimensionTabName();
                tableDataDTO.dimension = isDimension;
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, tableDataDTO);
            default:
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, null);
        }
    }

    @DS("datamodel")
    @Override
    public ResultEntity<String> getAggregation(Integer id) {
        IndicatorsPO indicators = indicatorsMapper.selectById(id);
        if (indicators == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, indicators.getCalculationLogic());
    }

    @DS("datamodel")
    @Override
    public ResultEntity<String> getDimensionName(Integer relationId) {
        DimensionAttributePO dimensionAttribute = dimensionAttributeMapper.selectById(relationId);
        if (dimensionAttribute == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        DimensionPO dimension = dimensionMapper.selectById(dimensionAttribute.getDimensionId());
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,dimension.getDimensionTabName());
    }

    @DS("datamodel")
    @Override
    public List<OutParameterDTO> getRelationShip(InputParameterDTO dto)
    {
        List<OutParameterDTO> list=new ArrayList<>();

        //获取原子指标下所有事实表id
        QueryWrapper<IndicatorsPO> indicatorsPOQueryWrapper=new QueryWrapper<>();
        indicatorsPOQueryWrapper.select("fact_id").in("id",dto.indicatorsIds);
        List<Integer> factIds=(List)indicatorsMapper.selectObjs(indicatorsPOQueryWrapper).stream().distinct().collect(Collectors.toList());
        //List<Integer> factId=
        if (factIds ==null || factIds.size()==0)
        {
            return list;
        }

        //获取维度字段所属维度id
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("dimension_id").in("id",dto.ids);
        List<Integer> ids=(List)dimensionAttributeMapper.selectObjs(queryWrapper).stream().distinct().collect(Collectors.toList());
        if (ids ==null || ids.size()==0)
        {
            return list;
        }

        //获取所有事实表下所有事实字段id
        QueryWrapper<FactAttributePO> factAttributePOQueryWrapper=new QueryWrapper<>();
        factAttributePOQueryWrapper.in("fact_id",factIds);
        List<FactAttributePO> factAttributePOList=factAttributeMapper.selectList(factAttributePOQueryWrapper);
        if (factAttributePOList==null || factAttributePOList.size()==0)
        {
            return list;
        }



        for (Integer dimensionId:ids)
        {
            List<FactAttributePO> data=factAttributePOList.stream().filter(e->dimensionId.equals(e.associateDimensionId)).collect(Collectors.toList());
            OutParameterDTO dto1=new OutParameterDTO();
            if (data ==null || data.size()==0)
            {
                dto1.whether=0;
                list.add(dto1);
                break;
            }
            //获取维度名称
            DimensionPO dimensionPO=dimensionMapper.selectById(dimensionId);
            if (dimensionPO==null)
            {
                break;
            }
            //获取事实表名称
            FactAttributePO factAttributePO=data.stream().findFirst().get();
            FactPO factPO=factMapper.selectById(factAttributePO.factId);
            if (factPO==null)
            {
                break;
            }
            dto1.factName=factPO.getFactTableEnName()+"_key";
            dto1.dimensionName=dimensionPO.getDimensionTabName()+"_key";
            dto1.whether=1;
            list.add(dto1);
        }
        return list;
    }

}
