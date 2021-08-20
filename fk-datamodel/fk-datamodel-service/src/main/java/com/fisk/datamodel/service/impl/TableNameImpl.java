package com.fisk.datamodel.service.impl;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
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
    public ResultEntity<String> getTableName(Integer id, DataDoFieldTypeEnum type) {

        switch (type) {
            case VALUE:
                IndicatorsPO indicators = indicatorsMapper.selectById(id);
                if (indicators == null) {
                    return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
                }
                FactPO fact = factMapper.selectById(indicators.getFactId());
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, fact.getFactTableEnName());
            case WHERE:
            case COLUMN:
                DimensionAttributePO dimensionAttribute = dimensionAttributeMapper.selectById(id);
                if (dimensionAttribute == null) {
                    return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
                }
                DimensionPO dimension = dimensionMapper.selectById(dimensionAttribute.getDimensionId());
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dimension.getDimensionTabName());
            default:
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, null);
        }
    }
}
