package com.fisk.dataservice.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.TableDataDTO;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
import com.fisk.dataservice.mapper.*;
import com.fisk.dataservice.service.ITableName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
                FactAttributePO factAttribute = factAttributeMapper.selectById(indicators.getFactId());

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
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, tableDataDTO);
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
}
