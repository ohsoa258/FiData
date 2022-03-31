package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorPushDTO;
import com.fisk.datamodel.dto.tableconfig.SourceFieldDTO;
import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;
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

    @Override
    public List<SourceTableDTO> getDataModelTable(int publishStatus)
    {
        List<SourceTableDTO> list=new ArrayList<>();
        list.addAll(getDimensionTable(publishStatus));
        if (publishStatus==1)
        {
            list.addAll(getDwFactTable());
        }else {
            list.addAll(getDorisFactTable());
        }
        return list;
    }

    public List<SourceTableDTO> getDimensionTable(int publishStatus)
    {
        List<SourceTableDTO> list=new ArrayList<>();
        QueryWrapper<DimensionPO> dimensionPoQueryWrapper=new QueryWrapper<>();
        //dw已发布
        if (publishStatus==1)
        {
            dimensionPoQueryWrapper.lambda().eq(DimensionPO::getIsPublish, PublicStatusEnum.PUBLIC_SUCCESS);
        }
        //doris已发布
        else {
            dimensionPoQueryWrapper.lambda().eq(DimensionPO::getDorisPublish, PublicStatusEnum.PUBLIC_SUCCESS);
        }
        List<DimensionPO> dimensionPoList=dimensionMapper.selectList(dimensionPoQueryWrapper);
        if (!CollectionUtils.isEmpty(dimensionPoList))
        {
            dimensionPoQueryWrapper.select("id");
            List<Integer> dimensionIdList=(List)dimensionMapper.selectObjs(dimensionPoQueryWrapper);
            List<DimensionAttributePO> dimensionAttributePoList=new ArrayList<>();
            if (!CollectionUtils.isEmpty(dimensionIdList))
            {
                QueryWrapper<DimensionAttributePO> dimensionAttributePoQueryWrapper=new QueryWrapper<>();
                dimensionAttributePoQueryWrapper.in("dimension_id",dimensionIdList);
                dimensionAttributePoList=dimensionAttributeMapper.selectList(dimensionAttributePoQueryWrapper);
            }
            for (DimensionPO dimensionPo:dimensionPoList)
            {
                SourceTableDTO dto=new SourceTableDTO();
                dto.id=dimensionPo.id;
                dto.tableName=dimensionPo.dimensionTabName;
                dto.type= publishStatus==DataModelTableTypeEnum.DW_DIMENSION.getValue()?DataModelTableTypeEnum.DW_DIMENSION.getValue():DataModelTableTypeEnum.DORIS_DIMENSION.getValue();
                dto.tableDes=dimensionPo.dimensionDesc==null?dimensionPo.dimensionTabName:dimensionPo.dimensionDesc;
                dto.sqlScript=dimensionPo.sqlScript;
                List<SourceFieldDTO> fieldDtoList=new ArrayList<>();
                List<DimensionAttributePO> attributePoList=dimensionAttributePoList.stream()
                        .filter(e->e.dimensionId==dimensionPo.id).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(attributePoList))
                {
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

                for (DimensionAttributePO attributePo:attributePoList)
                {
                    SourceFieldDTO field=new SourceFieldDTO();
                    field.id=attributePo.id;
                    field.fieldName=attributePo.dimensionFieldEnName;
                    field.fieldType=attributePo.dimensionFieldType;
                    field.fieldLength=attributePo.dimensionFieldLength;
                    field.fieldDes=attributePo.dimensionFieldDes;
                    field.primaryKey=attributePo.isPrimaryKey;
                    field.sourceTable=attributePo.sourceTableName;
                    field.sourceField=attributePo.sourceFieldName;
                    if (attributePo.associateDimensionId !=0 && attributePo.associateDimensionFieldId !=0)
                    {
                        field.associatedDim=true;
                        field.associatedDimId=attributePo.associateDimensionId;
                        DimensionPO dimensionPo1=dimensionMapper.selectById(attributePo.associateDimensionId);
                        if (dimensionPo1==null)
                        {
                            continue;
                        }
                        field.associatedDimName=dimensionPo1.dimensionTabName;
                        //查询关联维度字段
                        DimensionAttributePO dimensionAttributePo=dimensionAttributeMapper.selectById(attributePo.associateDimensionFieldId);
                        field.associatedDimAttributeId=attributePo.associateDimensionFieldId;
                        field.associatedDimAttributeName=dimensionAttributePo==null?"":dimensionAttributePo.dimensionFieldEnName;

                        SourceFieldDTO associatedField=new SourceFieldDTO();
                        associatedField.id=attributePo.associateDimensionId;
                        associatedField.fieldName=newFieldName+"key";
                        associatedField.fieldType="NVARCHAR";
                        associatedField.fieldLength=255;
                        associatedField.fieldDes=dimensionPo1.dimensionDesc;
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

    public List<SourceTableDTO> getDwFactTable()
    {
        List<SourceTableDTO> list=new ArrayList<>();
        QueryWrapper<FactPO> factPoQueryWrapper=new QueryWrapper<>();
        factPoQueryWrapper.lambda().eq(FactPO::getIsPublish, PublicStatusEnum.PUBLIC_SUCCESS);
        List<FactPO> factPoList=factMapper.selectList(factPoQueryWrapper);
        if (!CollectionUtils.isEmpty(factPoList))
        {
            factPoQueryWrapper.select("id");
            List<Integer> factIdList=(List)factMapper.selectObjs(factPoQueryWrapper);
            List<FactAttributePO> factAttributePoList=new ArrayList<>();
            if (!CollectionUtils.isEmpty(factIdList))
            {
                QueryWrapper<FactAttributePO> factAttributePoQueryWrapper=new QueryWrapper<>();
                factAttributePoQueryWrapper.in("fact_id",factIdList);
                factAttributePoList=factAttributeMapper.selectList(factAttributePoQueryWrapper);
            }
            for (FactPO factPo:factPoList)
            {
                SourceTableDTO dto=new SourceTableDTO();
                dto.id=factPo.id;
                dto.tableName=factPo.factTabName;
                dto.type= OlapTableEnum.FACT.getValue();
                dto.tableDes=factPo.factTableDesc;
                dto.sqlScript=factPo.sqlScript;
                List<SourceFieldDTO> fieldDtoList=new ArrayList<>();
                List<FactAttributePO> attributePoList=factAttributePoList.stream()
                        .filter(e->e.factId==factPo.id).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(attributePoList))
                {
                    continue;
                }
                for (FactAttributePO attributePo:attributePoList)
                {
                    SourceFieldDTO field=new SourceFieldDTO();
                    field.id=attributePo.id;
                    field.fieldName=attributePo.factFieldEnName;
                    field.fieldType=attributePo.factFieldType;
                    field.fieldLength=attributePo.factFieldLength;
                    field.fieldDes=attributePo.factFieldDes;
                    field.sourceTable=attributePo.sourceTableName;
                    field.sourceField=attributePo.sourceFieldName;
                    if (attributePo.attributeType== FactAttributeEnum.DIMENSION_KEY.getValue())
                    {
                        field.associatedDim=true;
                        field.associatedDimId=attributePo.associateDimensionId;
                        DimensionPO dimensionPo1=dimensionMapper.selectById(attributePo.associateDimensionId);
                        if (dimensionPo1==null)
                        {
                            continue;
                        }
                        field.associatedDimName=dimensionPo1.dimensionTabName;
                        //查询关联维度字段
                        DimensionAttributePO dimensionAttributePo=dimensionAttributeMapper.selectById(attributePo.associateDimensionFieldId);
                        field.associatedDimAttributeId=attributePo.associateDimensionFieldId;
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
                dto.fieldList=fieldDtoList;
                list.add(dto);
            }
        }
        return list;
    }

    public List<SourceTableDTO> getDorisFactTable()
    {
        List<SourceTableDTO> list=new ArrayList<>();
        //获取doris已发布表集合
        QueryWrapper<FactPO> factPoQueryWrapper=new QueryWrapper<>();
        factPoQueryWrapper.lambda().eq(FactPO::getDorisPublish, PublicStatusEnum.PUBLIC_SUCCESS);
        List<FactPO> factPoList=factMapper.selectList(factPoQueryWrapper);
        if (CollectionUtils.isEmpty(factPoList))
        {
            return list;
        }
        //获取事实表指标字段
        for (FactPO item:factPoList)
        {
            SourceTableDTO dto=new SourceTableDTO();
            dto.id=item.id;
            dto.tableName=item.factTabName;
            dto.type= DataModelTableTypeEnum.DORIS_FACT.getValue();
            dto.tableDes=item.factTableDesc==null?item.factTabName:item.factTableDesc;
            dto.sqlScript=item.sqlScript;
            List<AtomicIndicatorPushDTO> atomicIndicator = atomicIndicators.getAtomicIndicator((int) item.id);
            if (CollectionUtils.isEmpty(atomicIndicator))
            {
                continue;
            }
            List<SourceFieldDTO> fieldDtoList=new ArrayList<>();
            for (AtomicIndicatorPushDTO atomic:atomicIndicator)
            {
                SourceFieldDTO field=new SourceFieldDTO();
                field.attributeType=atomic.attributeType;
                field.id=atomic.id;
                switch (atomic.attributeType)
                {
                    case 0:
                        field.fieldName=atomic.factFieldName;
                        field.fieldType=atomic.factFieldType;
                        field.fieldLength=atomic.factFieldLength;
                        break;
                    case 1:
                        field.fieldName=atomic.dimensionTableName.substring(4)+"key";
                        field.fieldType="NVARCHAR";
                        field.fieldLength=255;
                        break;
                    case 2:
                        field.fieldName=atomic.atomicIndicatorName;
                        field.fieldType="BIGINT";
                        field.fieldLength=0;
                        //聚合逻辑
                        field.calculationLogic=atomic.aggregationLogic;
                        //聚合字段
                        field.sourceField=atomic.aggregatedField;
                    default:
                        break;
                }
                field.fieldDes=atomic.factFieldName;
                fieldDtoList.add(field);
            }
            dto.fieldList=fieldDtoList;
            list.add(dto);
        }
        return list;
    }

}
