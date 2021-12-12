package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.entity.*;
import com.fisk.datamodel.enums.FactAttributeEnum;
import com.fisk.datamodel.enums.IndicatorsTypeEnum;
import com.fisk.datamodel.mapper.*;
import com.fisk.dataservice.dto.IndicatorDTO;
import com.fisk.dataservice.dto.IndicatorFeignDTO;
import com.fisk.dataservice.dto.TableDataDTO;
import com.fisk.datamodel.service.ITableName;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.fisk.dataservice.enums.IndicatorTypeEnum.ATOMIC_INDICATORS;
import static com.fisk.dataservice.enums.IndicatorTypeEnum.DERIVED_INDICATORS;

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
    BusinessLimitedMapper businessLimitedMapper;
    @Resource
    BusinessLimitedAttributeMapper attributeMapper;
    @Resource
    FactAttributeMapper factAttributeMapper;

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
                tableDataDTO.tableName = fact.factTableEnName;
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
                tableDataDTO.tableName = dimension.dimensionTabName;
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, tableDataDTO);
            default:
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, null);
        }
    }

    @Override
    public ResultEntity<String> getAggregation(Integer id) {
        IndicatorsPO indicators = indicatorsMapper.selectById(id);
        if (indicators == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, indicators.getCalculationLogic());
    }

    @Override
    public ResultEntity<List<IndicatorDTO>> getIndicatorsLogic(IndicatorFeignDTO dto) {
        List<IndicatorDTO> dtoList = new ArrayList<>();

        dto.getIndicatorList().stream().forEach(b -> {
            IndicatorsPO indicatorsPO = indicatorsMapper.selectById(b.getId());
            if (indicatorsPO == null){
                return;
            }

            IndicatorDTO indicator = new IndicatorDTO();
            indicator.setId(b.getId());
            indicator.setFieldName(b.getFieldName());
            indicator.setTableName(b.getTableName());
            if (indicatorsPO.getIndicatorsType() == IndicatorsTypeEnum.ATOMIC_INDICATORS.getValue()){
                // 原子指标
                indicator.setType(ATOMIC_INDICATORS);
                indicator.setCalculationLogic(indicatorsPO.getCalculationLogic());
                dtoList.add(indicator);
            }else if (indicatorsPO.getIndicatorsType() == IndicatorsTypeEnum.DERIVED_INDICATORS.getValue()){
                IndicatorsPO indicatorsPO1 = indicatorsMapper.selectById(indicatorsPO.getAtomicId());
                indicator.setFieldName(indicatorsPO1.getIndicatorsName());
                indicator.setDeriveName(indicatorsPO.getIndicatorsName());
                indicator.setType(DERIVED_INDICATORS);
                indicator.setTimePeriod(indicatorsPO.timePeriod);
                indicator.setCalculationLogic(indicatorsPO1.getCalculationLogic());

                // 派生指标
                QueryWrapper<BusinessLimitedPO> query = new QueryWrapper<>();
                query.lambda().eq(BusinessLimitedPO::getId,indicatorsPO.getBusinessLimitedId());
                BusinessLimitedPO limitedPO = businessLimitedMapper.selectOne(query);
                if (limitedPO != null){
                    QueryWrapper<BusinessLimitedAttributePO> queryWrapper = new QueryWrapper<>();
                    queryWrapper.lambda().eq(BusinessLimitedAttributePO::getBusinessLimitedId,limitedPO.getId());
                    List<BusinessLimitedAttributePO> attributePOList = attributeMapper.selectList(queryWrapper);
                    indicator.setWhereTimeLogic(attributePOList.stream().filter(e -> e!=null)
                            .map(e -> {
                                String factFieldName = this.getFactFieldName(e.getFactAttributeId(),e.getCalculationLogic(),e.getCalculationValue());
                                return factFieldName;
                            }).collect(Collectors.joining(" AND ")));
                }

                dtoList.add(indicator);
            }
        });

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,dtoList);
    }


    /**
     * 查询业务限定字段名称
     * @param id
     * @return
     */
    public String getFactFieldName(Integer id,String calculationLogic,String calculationValue){
        FactAttributePO factAttributePO = factAttributeMapper.selectById(id);
        if (factAttributePO != null){
            if (factAttributePO.attributeType == FactAttributeEnum.DIMENSION_KEY.getValue()){
                DimensionPO dimension = dimensionMapper.selectById(factAttributePO.getAssociateDimensionId());
                DimensionAttributePO dimensionAttributePO = dimensionAttributeMapper.selectById(factAttributePO.getAssociateDimensionFieldId());
                String tableName = dimension.getDimensionTabName();
                // String dimensionFieldEnName = dimensionAttribute.getDimensionFieldEnName();
                String str1 = tableName.substring(0, tableName.indexOf("_"));
                String dimensionTabName = tableName.substring(str1.length()+1, tableName.length()) + "key";
                String subQuery = " SELECT " + dimensionTabName + " FROM " + tableName + " WHERE " +
                        dimensionAttributePO.getDimensionFieldEnName() + calculationLogic + calculationValue;
                return factMapper.selectById(factAttributePO.factId).getFactTabName() + "." + dimensionTabName +"=" + "(" + subQuery +")";
            }else {
                FactPO factPO=factMapper.selectById(factAttributePO.factId);
                if (factPO !=null)
                {
                    return factPO.factTabName+"."+factAttributePO.factFieldEnName+calculationLogic + calculationValue;
                }

            }
        }
        return null;
    }
}
