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
        QueryWrapper<DimensionPO> dimensionPOQueryWrapper=new QueryWrapper<>();
        //dw已发布
        if (publishStatus==1)
        {
            dimensionPOQueryWrapper.lambda().eq(DimensionPO::getIsPublish, PublicStatusEnum.PUBLIC_SUCCESS);
        }
        //doris已发布
        else {
            dimensionPOQueryWrapper.lambda().eq(DimensionPO::getDorisPublish, PublicStatusEnum.PUBLIC_SUCCESS);
        }
        List<DimensionPO> dimensionPOList=dimensionMapper.selectList(dimensionPOQueryWrapper);
        if (!CollectionUtils.isEmpty(dimensionPOList))
        {
            dimensionPOQueryWrapper.select("id");
            List<Integer> dimensionIdList=(List)dimensionMapper.selectObjs(dimensionPOQueryWrapper);
            List<DimensionAttributePO> dimensionAttributePOList=new ArrayList<>();
            if (!CollectionUtils.isEmpty(dimensionIdList))
            {
                QueryWrapper<DimensionAttributePO> dimensionAttributePOQueryWrapper=new QueryWrapper<>();
                dimensionAttributePOQueryWrapper.in("dimension_id",dimensionIdList);
                dimensionAttributePOList=dimensionAttributeMapper.selectList(dimensionAttributePOQueryWrapper);
            }
            for (DimensionPO dimensionPO:dimensionPOList)
            {
                SourceTableDTO dto=new SourceTableDTO();
                dto.id=dimensionPO.id;
                dto.tableName=dimensionPO.dimensionTabName;
                dto.type= publishStatus==DataModelTableTypeEnum.DW_DIMENSION.getValue()?DataModelTableTypeEnum.DW_DIMENSION.getValue():DataModelTableTypeEnum.DORIS_DIMENSION.getValue();
                dto.tableDes=dimensionPO.dimensionDesc==null?dimensionPO.dimensionTabName:dimensionPO.dimensionDesc;
                dto.sqlScript=dimensionPO.sqlScript;
                List<SourceFieldDTO> fieldDTOS=new ArrayList<>();
                List<DimensionAttributePO> attributePOS=dimensionAttributePOList.stream()
                        .filter(e->e.dimensionId==dimensionPO.id).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(attributePOS))
                {
                    continue;
                }
                for (DimensionAttributePO attributePO:attributePOS)
                {
                    SourceFieldDTO field=new SourceFieldDTO();
                    field.id=attributePO.id;
                    field.fieldName=attributePO.dimensionFieldEnName;
                    field.fieldType=attributePO.dimensionFieldType;
                    field.fieldLength=attributePO.dimensionFieldLength;
                    field.fieldDes=attributePO.dimensionFieldDes;
                    field.primaryKey=attributePO.isPrimaryKey;
                    field.sourceTable=attributePO.sourceTableName;
                    field.sourceField=attributePO.sourceFieldName;
                    if (attributePO.associateDimensionId !=0 && attributePO.associateDimensionFieldId !=0)
                    {
                        field.associatedDim=true;
                        field.associatedDimId=attributePO.associateDimensionId;
                        DimensionPO dimensionPO1=dimensionMapper.selectById(attributePO.associateDimensionId);
                        field.associatedDimName=dimensionPO1==null?"":dimensionPO1.dimensionTabName;
                        //查询关联维度字段
                        DimensionAttributePO dimensionAttributePO=dimensionAttributeMapper.selectById(attributePO.associateDimensionFieldId);
                        field.associatedDimAttributeId=attributePO.associateDimensionFieldId;
                        field.associatedDimAttributeName=dimensionAttributePO==null?"":dimensionAttributePO.dimensionFieldEnName;
                    }
                    fieldDTOS.add(field);
                }
                dto.fieldList=fieldDTOS;
                list.add(dto);
            }
        }
        return list;
    }

    public List<SourceTableDTO> getDwFactTable()
    {
        List<SourceTableDTO> list=new ArrayList<>();
        QueryWrapper<FactPO> factPOQueryWrapper=new QueryWrapper<>();
        factPOQueryWrapper.lambda().eq(FactPO::getIsPublish, PublicStatusEnum.PUBLIC_SUCCESS);
        List<FactPO> factPOList=factMapper.selectList(factPOQueryWrapper);
        if (!CollectionUtils.isEmpty(factPOList))
        {
            factPOQueryWrapper.select("id");
            List<Integer> factIdList=(List)factMapper.selectObjs(factPOQueryWrapper);
            List<FactAttributePO> factAttributePOList=new ArrayList<>();
            if (!CollectionUtils.isEmpty(factIdList))
            {
                QueryWrapper<FactAttributePO> factAttributePOQueryWrapper=new QueryWrapper<>();
                factAttributePOQueryWrapper.in("fact_id",factIdList);
                factAttributePOList=factAttributeMapper.selectList(factAttributePOQueryWrapper);
            }
            for (FactPO factPO:factPOList)
            {
                SourceTableDTO dto=new SourceTableDTO();
                dto.id=factPO.id;
                dto.tableName=factPO.factTabName;
                dto.type= OlapTableEnum.FACT.getValue();
                dto.tableDes=factPO.factTableDesc;
                dto.sqlScript=factPO.sqlScript;
                List<SourceFieldDTO> fieldDTOS=new ArrayList<>();
                List<FactAttributePO> attributePOS=factAttributePOList.stream()
                        .filter(e->e.factId==factPO.id).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(attributePOS))
                {
                    continue;
                }
                for (FactAttributePO attributePO:attributePOS)
                {
                    SourceFieldDTO field=new SourceFieldDTO();
                    field.id=attributePO.id;
                    field.fieldName=attributePO.factFieldEnName;
                    field.fieldType=attributePO.factFieldType;
                    field.fieldLength=attributePO.factFieldLength;
                    field.fieldDes=attributePO.factFieldDes;
                    field.sourceTable=attributePO.sourceTableName;
                    field.sourceField=attributePO.sourceFieldName;
                    if (attributePO.attributeType== FactAttributeEnum.DIMENSION_KEY.getValue())
                    {
                        field.associatedDim=true;
                        field.associatedDimId=attributePO.associateDimensionId;
                        DimensionPO dimensionPO1=dimensionMapper.selectById(attributePO.associateDimensionId);
                        field.associatedDimName=dimensionPO1==null?"":dimensionPO1.dimensionTabName;
                        //查询关联维度字段
                        DimensionAttributePO dimensionAttributePO=dimensionAttributeMapper.selectById(attributePO.associateDimensionFieldId);
                        field.associatedDimAttributeId=attributePO.associateDimensionFieldId;
                        field.associatedDimAttributeName=dimensionAttributePO==null?"":dimensionAttributePO.dimensionFieldEnName;
                    }
                    fieldDTOS.add(field);
                }
                dto.fieldList=fieldDTOS;
                list.add(dto);
            }
        }
        return list;
    }

    public List<SourceTableDTO> getDorisFactTable()
    {
        List<SourceTableDTO> list=new ArrayList<>();
        //获取doris已发布表集合
        QueryWrapper<FactPO> factPOQueryWrapper=new QueryWrapper<>();
        factPOQueryWrapper.lambda().eq(FactPO::getDorisPublish, PublicStatusEnum.PUBLIC_SUCCESS);
        List<FactPO> factPOList=factMapper.selectList(factPOQueryWrapper);
        if (CollectionUtils.isEmpty(factPOList))
        {
            return list;
        }
        //获取事实表指标字段
        for (FactPO item:factPOList)
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
            List<SourceFieldDTO> fieldDTOS=new ArrayList<>();
            for (AtomicIndicatorPushDTO atomic:atomicIndicator)
            {
                SourceFieldDTO field=new SourceFieldDTO();
                field.attributeType=atomic.attributeType;
                field.id=atomic.id;
                switch (atomic.attributeType)
                {
                    case 1:
                        field.fieldName=atomic.factFieldName;
                        field.fieldType=atomic.factFieldType;
                        field.fieldLength=atomic.factFieldLength;
                        break;
                    case 2:
                        field.fieldName=atomic.dimensionTableName+"_key";
                        field.fieldType="NVARCHAR";
                        field.fieldLength=255;
                        field.associatedDimId=(int)atomic.id;
                        break;
                    case 3:
                        field.fieldName=atomic.atomicIndicatorName;
                        field.fieldType="BIGINT";
                        field.fieldLength=0;
                }
                field.fieldDes=atomic.factFieldName;
                fieldDTOS.add(field);
            }
            dto.fieldList=fieldDTOS;
            list.add(dto);
        }
        return list;
    }

}
