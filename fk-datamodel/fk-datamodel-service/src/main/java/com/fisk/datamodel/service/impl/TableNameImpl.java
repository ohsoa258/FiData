package com.fisk.datamodel.service.impl;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.table.TableData;
import com.fisk.datamodel.entity.DimensionAttributePO;
import com.fisk.datamodel.entity.DimensionPO;
import com.fisk.datamodel.entity.FactPO;
import com.fisk.datamodel.entity.IndicatorsPO;
import com.fisk.datamodel.mapper.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.mapper.FactMapper;
import com.fisk.datamodel.mapper.IndicatorsMapper;
import com.fisk.datamodel.service.ITableName;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
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

    @Override
    public ResultEntity<TableData> getTableName(Integer id, DataDoFieldTypeEnum type,String field) {
        TableData tableData = new TableData();
        DimensionPO dimension;
        switch (type) {
            case VALUE:
                IndicatorsPO indicators = indicatorsMapper.selectById(id);
                if (indicators == null) {
                    return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
                }
                FactPO fact = factMapper.selectById(indicators.getFactId());


                tableData.type = DataDoFieldTypeEnum.VALUE;
                tableData.tableField = field;
                tableData.tableName = fact.factTableEnName;
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, tableData);
            case WHERE:
            case COLUMN:
                DimensionAttributePO po = dimensionAttributeMapper.selectById(id);
                if (po == null) {
                    return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
                }
                dimension = dimensionMapper.selectById(po.getDimensionId());
                if (type == DataDoFieldTypeEnum.WHERE) {
                    tableData.type = DataDoFieldTypeEnum.WHERE;
                }else {

                    tableData.type = DataDoFieldTypeEnum.COLUMN;
                }

                tableData.tableField = field;
                tableData.tableName = dimension.dimensionTabName;
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, tableData);
            default:
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, null);
        }
    }
}
