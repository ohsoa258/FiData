package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.datamodel.dto.tableconfig.SourceFieldDTO;
import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;
import com.fisk.datamodel.entity.DimensionAttributePO;
import com.fisk.datamodel.entity.DimensionPO;
import com.fisk.datamodel.entity.FactAttributePO;
import com.fisk.datamodel.entity.FactPO;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.mapper.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.mapper.FactAttributeMapper;
import com.fisk.datamodel.mapper.FactMapper;
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

    @Override
    public List<SourceTableDTO> getDataModelTable()
    {
        List<SourceTableDTO> list=new ArrayList<>();
        list.addAll(getDimensionTable());
        list.addAll(getFactTable());
        return list;
    }

    public List<SourceTableDTO> getDimensionTable()
    {
        List<SourceTableDTO> list=new ArrayList<>();
        QueryWrapper<DimensionPO> dimensionPOQueryWrapper=new QueryWrapper<>();
        dimensionPOQueryWrapper.lambda().eq(DimensionPO::getIsPublish, PublicStatusEnum.PUBLIC_SUCCESS);
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
                dto.type= OlapTableEnum.DIMENSION.getValue();
                dto.tableDes=dimensionPO.dimensionDesc;
                dto.sqlScript=dimensionPO.sqlScript;
                List<SourceFieldDTO> fieldDTOS=new ArrayList<>();
                List<DimensionAttributePO> attributePOS=dimensionAttributePOList.stream()
                        .filter(e->e.dimensionId==dimensionPO.id).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(attributePOS))
                {
                    for (DimensionAttributePO attributePO:attributePOS)
                    {
                        SourceFieldDTO field=new SourceFieldDTO();
                        field.id=attributePO.id;
                        field.fieldName=attributePO.dimensionFieldEnName;
                        field.fieldType=attributePO.dimensionFieldType;
                        field.fieldLength=attributePO.dimensionFieldLength;
                        field.fieldDes=attributePO.dimensionFieldDes;
                        field.primaryKey=attributePO.isPrimaryKey;
                        fieldDTOS.add(field);
                    }
                }
                dto.fieldList=fieldDTOS;
                list.add(dto);
            }
        }
        return list;
    }

    public List<SourceTableDTO> getFactTable()
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
                if (!CollectionUtils.isEmpty(attributePOS))
                {
                    for (FactAttributePO attributePO:attributePOS)
                    {
                        SourceFieldDTO field=new SourceFieldDTO();
                        field.id=attributePO.id;
                        field.fieldName=attributePO.factFieldEnName;
                        field.fieldType=attributePO.factFieldType;
                        field.fieldLength=attributePO.factFieldLength;
                        field.fieldDes=attributePO.factFieldDes;
                        fieldDTOS.add(field);
                    }
                }
                dto.fieldList=fieldDTOS;
                list.add(dto);
            }
        }
        return list;
    }

}
