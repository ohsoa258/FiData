package com.fisk.dataservice.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.table.TableDataDTO;
import com.fisk.dataservice.entity.DimensionAttributePO;
import com.fisk.dataservice.entity.DimensionPO;
import com.fisk.dataservice.entity.FactPO;
import com.fisk.dataservice.entity.IndicatorsPO;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
import com.fisk.dataservice.mapper.DimensionAttributeMapper;
import com.fisk.dataservice.mapper.DimensionMapper;
import com.fisk.dataservice.mapper.FactMapper;
import com.fisk.dataservice.mapper.IndicatorsMapper;
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

    @DS("datamodel")
    @Override
    public ResultEntity<TableDataDTO> getTableName(Integer id, DataDoFieldTypeEnum type, String field) {
        TableDataDTO tableDataDTO = new TableDataDTO();
        DimensionPO dimension;
        switch (type) {
            case VALUE:
                IndicatorsPO indicators = indicatorsMapper.selectById(id);
                if (indicators == null) {
                    return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
                }
                FactPO fact = factMapper.selectById(indicators.getFactId());

                tableDataDTO.id = id;
                tableDataDTO.type = DataDoFieldTypeEnum.VALUE;
                tableDataDTO.tableField = field;
                tableDataDTO.tableName = fact.getFactTableEnName();
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
}
