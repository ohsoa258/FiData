package com.fisk.datamodel.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleParameterDTO;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorPushDTO;
import com.fisk.datamodel.dto.tableconfig.SourceFieldDTO;
import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableSourceFieldConfigDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableSourceTableConfigDTO;
import com.fisk.datamodel.entity.*;
import com.fisk.datamodel.enums.DataModelTableTypeEnum;
import com.fisk.datamodel.enums.FactAttributeEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.mapper.*;
import com.fisk.datamodel.service.IDataModelTable;
import com.fisk.task.enums.OlapTableEnum;
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
public class DataModelTableImpl implements IDataModelTable {

    @Resource
    DimensionMapper dimensionMapper;
    @Resource
    DimensionAttributeMapper dimensionAttributeMapper;
    @Resource
    FactMapper factMapper;
    @Resource
    FactAttributeMapper factAttributeMapper;
    @Resource
    AtomicIndicatorsImpl atomicIndicators;
    @Resource
    WideTableMapper wideTableMapper;
    @Resource
    BusinessAreaMapper businessAreaMapper;

    @Override
    public List<SourceTableDTO> getDataModelTable(int publishStatus) {
        List<SourceTableDTO> list = new ArrayList<>();
        list.addAll(getDimensionTable(publishStatus, 0));
        if (publishStatus == 1) {
            list.addAll(getDwFactTable(0));
        } else if (publishStatus == 2) {
            list.addAll(getDorisFactTable(0));
        } else {
            list.addAll(getDorisWideTable(0));
        }
        return list;
    }

    public List<SourceTableDTO> getDimensionTable(int publishStatus, int tableId) {
        List<SourceTableDTO> list = new ArrayList<>();
        QueryWrapper<DimensionPO> dimensionPoQueryWrapper = new QueryWrapper<>();
        //dw已发布
        if (publishStatus == 1) {
            dimensionPoQueryWrapper.lambda().eq(DimensionPO::getIsPublish, PublicStatusEnum.PUBLIC_SUCCESS);
        }
        //doris已发布
        else {
            dimensionPoQueryWrapper.lambda().eq(DimensionPO::getDorisPublish, PublicStatusEnum.PUBLIC_SUCCESS);
        }
        if (tableId != 0) {
            dimensionPoQueryWrapper.lambda().eq(DimensionPO::getId, tableId);
        }
        List<DimensionPO> dimensionPoList = dimensionMapper.selectList(dimensionPoQueryWrapper);
        if (!CollectionUtils.isEmpty(dimensionPoList)) {
            dimensionPoQueryWrapper.select("id");
            List<Integer> dimensionIdList = (List) dimensionMapper.selectObjs(dimensionPoQueryWrapper);
            List<DimensionAttributePO> dimensionAttributePoList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(dimensionIdList)) {
                QueryWrapper<DimensionAttributePO> dimensionAttributePoQueryWrapper = new QueryWrapper<>();
                dimensionAttributePoQueryWrapper.in("dimension_id", dimensionIdList);
                dimensionAttributePoList = dimensionAttributeMapper.selectList(dimensionAttributePoQueryWrapper);
            }
            for (DimensionPO dimensionPo : dimensionPoList) {
                SourceTableDTO dto = new SourceTableDTO();
                dto.id = dimensionPo.id;
                dto.tableName = dimensionPo.dimensionTabName;
                dto.type = publishStatus == DataModelTableTypeEnum.DW_DIMENSION.getValue() ? DataModelTableTypeEnum.DW_DIMENSION.getValue() : DataModelTableTypeEnum.DORIS_DIMENSION.getValue();
                dto.tableDes = dimensionPo.dimensionDesc == null ? dimensionPo.dimensionTabName : dimensionPo.dimensionDesc;
                dto.sqlScript = dimensionPo.sqlScript;
                List<SourceFieldDTO> fieldDtoList = new ArrayList<>();
                List<DimensionAttributePO> attributePoList = dimensionAttributePoList.stream()
                        .filter(e -> e.dimensionId == dimensionPo.id).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(attributePoList)) {
                    continue;
                }
                //创建维度表主键key
                SourceFieldDTO dimensionKey=new SourceFieldDTO();
                String newFieldName=dimensionPo.dimensionTabName.substring(4);
                dimensionKey.fieldName=newFieldName+"key";
                dimensionKey.fieldType="NVARCHAR";
                dimensionKey.fieldLength=255;
                dimensionKey.fieldDes="";
                fieldDtoList.add(dimensionKey);
                for (DimensionAttributePO attributePo : attributePoList) {
                    SourceFieldDTO field = pushField(attributePo);
                    if (attributePo.associateDimensionId != 0 && attributePo.associateDimensionFieldId != 0) {
                        DimensionPO dimensionPo1 = dimensionMapper.selectById(attributePo.associateDimensionId);
                        if (dimensionPo1 == null) {
                            continue;
                        }
                        SourceFieldDTO associatedField = new SourceFieldDTO();
                        associatedField.id = attributePo.associateDimensionId;
                        associatedField.fieldName = newFieldName + "key";
                        associatedField.fieldType = "NVARCHAR";
                        associatedField.fieldLength = 255;
                        associatedField.fieldDes = dimensionPo1.dimensionDesc;
                        fieldDtoList.add(associatedField);
                    }
                    fieldDtoList.add(field);
                }
                dto.fieldList=fieldDtoList;
                list.add(dto);
            }
        }
        return list;
    }

    public SourceFieldDTO pushField(DimensionAttributePO attributePo){
        SourceFieldDTO field=new SourceFieldDTO();
        field.id=attributePo.id;
        field.fieldName=attributePo.dimensionFieldEnName;
        field.fieldType=attributePo.dimensionFieldType;
        field.fieldLength=attributePo.dimensionFieldLength;
        field.fieldDes=attributePo.dimensionFieldDes;
        field.primaryKey=attributePo.isPrimaryKey;
        field.sourceTable=attributePo.sourceTableName;
        field.sourceField=attributePo.sourceFieldName;
        if (attributePo.associateDimensionId != 0 || attributePo.associateDimensionFieldId != 0) {
            field.associatedDim = true;
            field.associatedDimId = attributePo.associateDimensionId;
            DimensionPO dimensionPo1 = dimensionMapper.selectById(attributePo.associateDimensionId);
            if (dimensionPo1 == null) {
                return field;
            }
            field.associatedDimName = dimensionPo1.dimensionTabName;
            //查询关联维度字段
            DimensionAttributePO dimensionAttributePo = dimensionAttributeMapper.selectById(attributePo.associateDimensionFieldId);
            field.associatedDimAttributeId = attributePo.associateDimensionFieldId;
            field.associatedDimAttributeName = dimensionAttributePo == null ? "" : dimensionAttributePo.dimensionFieldEnName;
        }
        return field;
    }

    /**
     * 获取dw库已发布的事实表
     *
     * @return
     */
    public List<SourceTableDTO> getDwFactTable(int tableId) {
        List<SourceTableDTO> list = new ArrayList<>();
        QueryWrapper<FactPO> factPoQueryWrapper = new QueryWrapper<>();
        factPoQueryWrapper.lambda().eq(FactPO::getIsPublish, PublicStatusEnum.PUBLIC_SUCCESS);
        if (tableId != 0) {
            factPoQueryWrapper.lambda().eq(FactPO::getId, tableId);
        }
        List<FactPO> factPoList = factMapper.selectList(factPoQueryWrapper);
        if (!CollectionUtils.isEmpty(factPoList)) {
            factPoQueryWrapper.select("id");
            List<Integer> factIdList = (List) factMapper.selectObjs(factPoQueryWrapper);
            List<FactAttributePO> factAttributePoList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(factIdList)) {
                QueryWrapper<FactAttributePO> factAttributePoQueryWrapper = new QueryWrapper<>();
                factAttributePoQueryWrapper.in("fact_id", factIdList);
                factAttributePoList = factAttributeMapper.selectList(factAttributePoQueryWrapper);
            }
            for (FactPO factPo : factPoList) {
                SourceTableDTO dto = new SourceTableDTO();
                dto.id = factPo.id;
                dto.tableName = factPo.factTabName;
                dto.type = OlapTableEnum.FACT.getValue();
                dto.tableDes = factPo.factTableDesc;
                dto.sqlScript = factPo.sqlScript;
                List<SourceFieldDTO> fieldDtoList = new ArrayList<>();
                List<FactAttributePO> attributePoList = factAttributePoList.stream()
                        .filter(e -> e.factId == factPo.id).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(attributePoList)) {
                    continue;
                }
                for (FactAttributePO attributePo : attributePoList) {
                    SourceFieldDTO field = new SourceFieldDTO();
                    field.id = attributePo.id;
                    field.fieldName = attributePo.factFieldEnName;
                    field.fieldType = attributePo.factFieldType;
                    field.fieldLength = attributePo.factFieldLength;
                    field.fieldDes = attributePo.factFieldDes;
                    field.sourceTable = attributePo.sourceTableName;
                    field.sourceField = attributePo.sourceFieldName;
                    if (attributePo.attributeType == FactAttributeEnum.DIMENSION_KEY.getValue()) {
                        field.associatedDim = true;
                        field.associatedDimId = attributePo.associateDimensionId;
                        DimensionPO dimensionPo1 = dimensionMapper.selectById(attributePo.associateDimensionId);
                        if (dimensionPo1 == null) {
                            continue;
                        }
                        field.associatedDimName = dimensionPo1.dimensionTabName;
                        //查询关联维度字段
                        DimensionAttributePO dimensionAttributePo = dimensionAttributeMapper.selectById(attributePo.associateDimensionFieldId);
                        field.associatedDimAttributeId = attributePo.associateDimensionFieldId;
                        field.associatedDimAttributeName=dimensionAttributePo==null?"":dimensionAttributePo.dimensionFieldEnName;

                        SourceFieldDTO associatedField=new SourceFieldDTO();
                        associatedField.id=attributePo.associateDimensionId;
                        String newFieldName=dimensionPo1.dimensionTabName.substring(4);
                        associatedField.fieldName=newFieldName+"key";
                        associatedField.fieldType="NVARCHAR";
                        associatedField.fieldLength=255;
                        associatedField.fieldDes=dimensionPo1.dimensionDesc;
                        associatedField.attributeType=FactAttributeEnum.DIMENSION_KEY.getValue();
                        fieldDtoList.add(associatedField);
                    }
                    fieldDtoList.add(field);
                }
                dto.fieldList = fieldDtoList;
                list.add(dto);
            }
        }
        return list;
    }

    /**
     * 获取doris已发布的事实表
     *
     * @param tableId
     * @return
     */
    public List<SourceTableDTO> getDorisFactTable(int tableId) {
        List<SourceTableDTO> list = new ArrayList<>();
        //获取doris已发布表集合
        QueryWrapper<FactPO> factPoQueryWrapper = new QueryWrapper<>();
        factPoQueryWrapper.lambda().eq(FactPO::getDorisPublish, PublicStatusEnum.PUBLIC_SUCCESS);
        if (tableId != 0) {
            factPoQueryWrapper.lambda().eq(FactPO::getId, tableId);
        }
        List<FactPO> factPoList = factMapper.selectList(factPoQueryWrapper);
        if (CollectionUtils.isEmpty(factPoList)) {
            return list;
        }
        //获取事实表指标字段
        for (FactPO item : factPoList) {
            SourceTableDTO dto = new SourceTableDTO();
            dto.id = item.id;
            dto.tableName = item.factTabName;
            dto.type = DataModelTableTypeEnum.DORIS_FACT.getValue();
            dto.tableDes = item.factTableDesc == null ? item.factTabName : item.factTableDesc;
            dto.sqlScript = item.sqlScript;
            List<AtomicIndicatorPushDTO> atomicIndicator = atomicIndicators.getAtomicIndicator((int) item.id);
            if (CollectionUtils.isEmpty(atomicIndicator)) {
                continue;
            }
            List<SourceFieldDTO> fieldDtoList = new ArrayList<>();
            //原子指标、退化维度
            for (AtomicIndicatorPushDTO atomic : atomicIndicator) {
                SourceFieldDTO field = new SourceFieldDTO();
                field.attributeType = atomic.attributeType;
                field.id = atomic.id;
                switch (atomic.attributeType) {
                    case 0:
                        field.fieldName = atomic.factFieldName;
                        field.fieldType = atomic.factFieldType;
                        field.fieldLength = atomic.factFieldLength;
                        break;
                    case 1:
                        field.fieldName = atomic.dimensionTableName.substring(4) + "key";
                        field.fieldType = "NVARCHAR";
                        field.fieldLength = 255;
                        field.associatedDimId = (int) atomic.dimensionTableId;
                        field.associatedDim = true;
                        break;
                    case 2:
                        field.fieldName = atomic.atomicIndicatorName;
                        field.fieldType = "BIGINT";
                        field.fieldLength = 0;
                        //聚合逻辑
                        field.calculationLogic = atomic.aggregationLogic;
                        //聚合字段
                        field.sourceField = atomic.aggregatedField;
                    default:
                        break;
                }
                field.fieldDes = atomic.factFieldName;
                fieldDtoList.add(field);
            }
            //派生指标
            List<AtomicIndicatorPushDTO> derivedIndicators = atomicIndicators.getDerivedIndicators((int) item.id);
            for (AtomicIndicatorPushDTO atomic : derivedIndicators) {
                SourceFieldDTO field = new SourceFieldDTO();
                field.attributeType = atomic.attributeType;
                field.id = atomic.id;
                field.fieldName = atomic.atomicIndicatorName;
                field.fieldType = "BIGINT";
                field.fieldLength = 0;
                field.atomicId = atomic.atomicId;
                //时间周期
                field.calculationLogic = atomic.aggregationLogic;
                fieldDtoList.add(field);
            }
            dto.fieldList = fieldDtoList;
            list.add(dto);
        }
        return list;
    }

    /**
     * 获取doris已发布的宽表
     *
     * @return
     */
    public List<SourceTableDTO> getDorisWideTable(int tableId) {
        List<SourceTableDTO> list = new ArrayList<>();
        //宽表已发布数据
        QueryWrapper<WideTableConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WideTableConfigPO::getDorisPublish, PublicStatusEnum.PUBLIC_SUCCESS);
        if (tableId != 0) {
            queryWrapper.lambda().eq(WideTableConfigPO::getId, tableId);
        }
        List<WideTableConfigPO> wideTableConfigPoList = wideTableMapper.selectList(queryWrapper);
        for (WideTableConfigPO item : wideTableConfigPoList) {
            SourceTableDTO dto = new SourceTableDTO();
            dto.id = item.id;
            dto.tableName = item.name;
            dto.type = DataModelTableTypeEnum.WIDE_TABLE.getValue();
            dto.sqlScript = item.sqlScript;
            dto.fieldList = new ArrayList<>();
            WideTableFieldConfigDTO wideTableFieldDto = JSON.parseObject(item.configDetails, WideTableFieldConfigDTO.class);
            if (wideTableFieldDto == null || CollectionUtils.isEmpty(wideTableFieldDto.entity)) {
                continue;
            }
            for (WideTableSourceTableConfigDTO fieldTableDto : wideTableFieldDto.entity) {
                for (WideTableSourceFieldConfigDTO fieldConfigDTO : fieldTableDto.columnConfig) {
                    SourceFieldDTO field = new SourceFieldDTO();
                    field.id = fieldConfigDTO.fieldId;
                    field.attributeType = fieldTableDto.tableType;
                    field.fieldName = fieldConfigDTO.fieldName;
                    dto.fieldList.add(field);
                }
            }
            list.add(dto);
        }
        return list;
    }

    /**
     * 获取数仓中每个表中的业务元数据配置
     *
     * @param parameterDto
     * @return
     */
    @Override
    public TableRuleInfoDTO setTableRule(TableRuleParameterDTO parameterDto) {
        TableRuleInfoDTO data = new TableRuleInfoDTO();
        List<TableRuleInfoDTO> filedList = new ArrayList<>();
        if (parameterDto.type == 1 || parameterDto.type == 2) {
            DimensionPO dimensionPo = dimensionMapper.selectById(parameterDto.tableId);
            if (dimensionPo == null) {
                return data;
            }
            BusinessAreaPO businessAreaPo = businessAreaMapper.selectById(dimensionPo.businessId);
            if (businessAreaPo == null) {
                return data;
            }
            data.businessName = businessAreaPo.getBusinessName();
            data.type = 1;
            data.name = dimensionPo.dimensionTabName;
            data.dataResponsiblePerson = businessAreaPo.getBusinessAdmin();
            List<SourceTableDTO> dimensionTable = getDimensionTable(parameterDto.type, parameterDto.tableId);
            for (SourceTableDTO item : dimensionTable) {
                for (SourceFieldDTO field : item.fieldList) {
                    TableRuleInfoDTO dto = new TableRuleInfoDTO();
                    dto.name = field.fieldName;
                    dto.type = 2;
                    dto.businessName = businessAreaPo.getBusinessName();
                    dto.dataResponsiblePerson = businessAreaPo.getBusinessAdmin();
                    filedList.add(dto);
                }
            }
        } else if (parameterDto.type == 3 || parameterDto.type == 4) {
            FactPO factPo = factMapper.selectById(parameterDto.tableId);
            if (factPo == null) {
                return data;
            }
            BusinessAreaPO businessAreaPo = businessAreaMapper.selectById(factPo.businessId);
            if (businessAreaPo == null) {
                return data;
            }
            data.businessName = businessAreaPo.getBusinessName();
            data.dataResponsiblePerson = businessAreaPo.getBusinessAdmin();
            data.type = 1;
            data.name = factPo.factTabName;
            List<SourceTableDTO> dwFactTable;
            if (parameterDto.type == 3) {
                dwFactTable = getDwFactTable(parameterDto.tableId);
            } else {
                dwFactTable = getDorisFactTable(parameterDto.tableId);
            }
            for (SourceTableDTO item : dwFactTable) {
                for (SourceFieldDTO field : item.fieldList) {
                    TableRuleInfoDTO dto = new TableRuleInfoDTO();
                    dto.name = field.fieldName;
                    dto.type = 2;
                    dto.businessName = businessAreaPo.getBusinessName();
                    dto.dataResponsiblePerson = businessAreaPo.getBusinessAdmin();
                    filedList.add(dto);
                }
            }
        }
        data.fieldRules = filedList;
        return data;
    }

}
