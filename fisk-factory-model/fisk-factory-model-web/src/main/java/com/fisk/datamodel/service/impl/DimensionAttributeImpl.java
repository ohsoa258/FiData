package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderPublishQueryDTO;
import com.fisk.datamodel.entity.*;
import com.fisk.datamodel.dto.dimensionattribute.*;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.enums.SyncModeEnum;
import com.fisk.datamodel.enums.TableHistoryTypeEnum;
import com.fisk.datamodel.map.DimensionAttributeMap;
import com.fisk.datamodel.map.SyncModeMap;
import com.fisk.datamodel.map.TableBusinessMap;
import com.fisk.datamodel.mapper.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.mapper.FactAttributeMapper;
import com.fisk.datamodel.service.IDimensionAttribute;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class DimensionAttributeImpl
        extends ServiceImpl<DimensionAttributeMapper, DimensionAttributePO>
        implements IDimensionAttribute {
    @Resource
    DimensionMapper mapper;
    @Resource
    DimensionAttributeMapper attributeMapper;
    @Resource
    FactAttributeMapper factAttributeMapper;
    @Resource
    DimensionFolderImpl dimensionFolder;
    @Resource
    SyncModeImpl syncMode;
    @Resource
    TableBusinessImpl tableBusiness;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addOrUpdateDimensionAttribute(DimensionAttributeAddDTO dto)
    {
        //判断是否存在
        DimensionPO dimensionPO=mapper.selectById(dto.dimensionId);
        if (dimensionPO==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //添加增量配置
        SyncModePO syncModePO = SyncModeMap.INSTANCES.dtoToPo(dto.syncModeDTO);
        boolean syncMode = this.syncMode.saveOrUpdate(syncModePO);
        boolean tableBusiness=true;
        if (dto.syncModeDTO.syncMode== SyncModeEnum.CUSTOM_OVERRIDE.getValue())
        {
            QueryWrapper<SyncModePO> syncModePOQueryWrapper=new QueryWrapper<>();
            syncModePOQueryWrapper.lambda().eq(SyncModePO::getSyncTableId,dto.syncModeDTO.syncTableId)
                    .eq(SyncModePO::getTableType,dto.syncModeDTO.tableType);
            SyncModePO po=this.syncMode.getOne(syncModePOQueryWrapper);
            if (po==null)
            {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            dto.syncModeDTO.syncTableBusinessDTO.syncId=(int)po.id;
            tableBusiness= this.tableBusiness.saveOrUpdate(TableBusinessMap.INSTANCES.dtoToPo(dto.syncModeDTO.syncTableBusinessDTO));
        }
        if (!syncMode || !tableBusiness)
        {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //删除维度字段属性
        List<Integer> ids=(List)dto.list.stream().filter(e->e.id!=0).map(DimensionAttributeDTO::getId).collect(Collectors.toList());
        if (ids!=null && ids.size()>0)
        {
            QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
            queryWrapper.notIn("id",ids).lambda().eq(DimensionAttributePO::getDimensionId,dto.dimensionId);
            List<DimensionAttributePO> list=attributeMapper.selectList(queryWrapper);
            if (list!=null && list.size()>0)
            {
                boolean flat=this.remove(queryWrapper);
                if (!flat)
                {
                    return ResultEnum.SAVE_DATA_ERROR;
                }
            }
        }
        //添加或修改维度字段
        List<DimensionAttributePO> poList=DimensionAttributeMap.INSTANCES.dtoListToPoList(dto.list);
        poList.stream().map(e->e.dimensionId=dto.dimensionId).collect(Collectors.toList());
        boolean result=this.saveOrUpdateBatch(poList);
        //是否发布
        if (dto.isPublish)
        {
            DimensionFolderPublishQueryDTO queryDTO=new DimensionFolderPublishQueryDTO();
            List<Integer> dimensionIds=new ArrayList<>();
            dimensionIds.add(dto.dimensionId);
            //修改发布状态
            dimensionPO.isPublish= PublicStatusEnum.PUBLIC_ING.getValue();
            if (mapper.updateById(dimensionPO)==0)
            {
                throw new FkException(ResultEnum.PUBLISH_FAILURE);
            }
            queryDTO.dimensionIds=dimensionIds;
            queryDTO.businessAreaId=dimensionPO.businessId;
            queryDTO.remark=dto.remark;
            queryDTO.syncMode=dto.syncModeDTO.syncMode;
            queryDTO.openTransmission=dto.openTransmission;
            return dimensionFolder.batchPublishDimensionFolder(queryDTO);
        }
        return result==true?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEntity<List<ModelPublishFieldDTO>> selectDimensionAttributeList(Integer dimensionId){
        Map<String, Object> conditionHashMap = new HashMap<>();
        List<ModelPublishFieldDTO> fieldList=new ArrayList<>();
        conditionHashMap.put("dimension_id",dimensionId);
        conditionHashMap.put("del_flag",1);
        List<DimensionAttributePO> dimensionAttributePOS = attributeMapper.selectByMap(conditionHashMap);
        for (DimensionAttributePO attributePO:dimensionAttributePOS)
        {
            ModelPublishFieldDTO fieldDTO=new ModelPublishFieldDTO();
            fieldDTO.fieldId=attributePO.id;
            fieldDTO.fieldEnName=attributePO.dimensionFieldEnName;
            fieldDTO.fieldType=attributePO.dimensionFieldType;
            fieldDTO.fieldLength=attributePO.dimensionFieldLength;
            fieldDTO.attributeType=attributePO.attributeType;
            fieldDTO.isPrimaryKey=attributePO.isPrimaryKey;
            fieldDTO.sourceFieldName=attributePO.sourceFieldName;
            fieldDTO.associateDimensionId=attributePO.associateDimensionId;
            fieldDTO.associateDimensionFieldId=attributePO.associateDimensionFieldId;
            //判断是否关联维度
            if (attributePO.associateDimensionId !=0 && attributePO.associateDimensionFieldId !=0 )
            {
                DimensionPO dimensionPO=mapper.selectById(attributePO.associateDimensionId);
                fieldDTO.associateDimensionName=dimensionPO==null?"":dimensionPO.dimensionTabName;
                fieldDTO.associateDimensionSqlScript=dimensionPO==null?"":dimensionPO.sqlScript;
                DimensionAttributePO dimensionAttributePO=attributeMapper.selectById(attributePO.associateDimensionFieldId);
                fieldDTO.associateDimensionFieldName=dimensionAttributePO==null?"":dimensionAttributePO.dimensionFieldEnName;
            }
            fieldList.add(fieldDTO);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, fieldList);
    }

    @Override
    public ResultEnum deleteDimensionAttribute(List<Integer> ids)
    {
        //判断字段是否与其他表有关联
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.in("associate_dimension_field_id",ids);
        List<DimensionAttributePO> list=attributeMapper.selectList(queryWrapper);
        if (list.size()>0)
        {
            return ResultEnum.FIELDS_ASSOCIATED;
        }

        //判断字段是否与事实表有关联
        QueryWrapper<FactAttributePO> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.in("associate_dimension_field_id",ids);
        List<FactAttributePO> poList=factAttributeMapper.selectList(queryWrapper1);
        if (poList.size()>0)
        {
            return ResultEnum.FIELDS_ASSOCIATED;
        }

        DimensionAttributePO po=attributeMapper.selectById(ids.get(0));
        return attributeMapper.deleteBatchIds(ids)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public DimensionAttributeUpdateDTO getDimensionAttribute(int id)
    {
        return DimensionAttributeMap.INSTANCES.poToDetailDto(attributeMapper.selectById(id));
    }

    @Override
    public DimensionAttributeListDTO getDimensionAttributeList(int dimensionId)
    {
        DimensionAttributeListDTO data=new DimensionAttributeListDTO();
        DimensionPO dimensionPO=mapper.selectById(dimensionId);
        if (dimensionPO==null)
        {
            return data;
        }
        //获取sql脚本
        data.sqlScript=dimensionPO.sqlScript;
        //获取表字段详情
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId,dimensionId);
        List<DimensionAttributePO> list=attributeMapper.selectList(queryWrapper);
        data.attributeDTOList=DimensionAttributeMap.INSTANCES.poListToDtoList(list);
        //获取增量配置信息
        QueryWrapper<SyncModePO> syncModePOQueryWrapper=new QueryWrapper<>();
        syncModePOQueryWrapper.lambda().eq(SyncModePO::getSyncTableId,dimensionPO.id)
                .eq(SyncModePO::getTableType, TableHistoryTypeEnum.TABLE_DIMENSION);
        SyncModePO syncModePO=syncMode.getOne(syncModePOQueryWrapper);
        if(syncModePO==null)
        {
            return data;
        }
        data.syncModeDTO=SyncModeMap.INSTANCES.poToDto(syncModePO);
        if (syncModePO.syncMode!= SyncModeEnum.CUSTOM_OVERRIDE.getValue())
        {
            return data;
        }
        QueryWrapper<TableBusinessPO> tableBusinessPOQueryWrapper=new QueryWrapper<>();
        tableBusinessPOQueryWrapper.lambda().eq(TableBusinessPO::getSyncId,syncModePO.id);
        TableBusinessPO tableBusinessPO=tableBusiness.getOne(tableBusinessPOQueryWrapper);
        if (tableBusinessPO==null)
        {
            return data;
        }
        data.syncModeDTO.syncTableBusinessDTO=TableBusinessMap.INSTANCES.poToDto(tableBusinessPO);
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum updateDimensionAttribute(DimensionAttributeUpdateDTO dto)
    {
        DimensionAttributePO po=attributeMapper.selectById(dto.id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId,po.dimensionId)
                .eq(DimensionAttributePO::getDimensionFieldEnName,dto.dimensionFieldEnName);
        DimensionAttributePO model=attributeMapper.selectOne(queryWrapper);
        if (model !=null && model.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        //更改维度表发布状态
        ////DimensionPO dimensionPO=mapper.selectById(po.dimensionId);
        ////dimensionPO.isPublish=PublicStatusEnum.UN_PUBLIC.getValue();
        //更改维度字段数据
        po.dimensionFieldCnName=dto.dimensionFieldCnName;
        po.dimensionFieldDes=dto.dimensionFieldDes;
        po.dimensionFieldLength=dto.dimensionFieldLength;
        po.dimensionFieldEnName=dto.dimensionFieldEnName;
        po.dimensionFieldType=dto.dimensionFieldType;
        ////po=DimensionAttributeMap.INSTANCES.updateDtoToPo(dto);
        return attributeMapper.updateById(po)>0? ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<DimensionAttributeAssociationDTO> getDimensionAttributeData(int id)
    {
        QueryWrapper <DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").lambda().eq(DimensionAttributePO::getDimensionId,id);
        List<DimensionAttributePO> list=attributeMapper.selectList(queryWrapper);
        return DimensionAttributeMap.INSTANCES.poToNameListDTO(list);
    }

    @Override
    public List<ModelMetaDataDTO> getDimensionMetaDataList(List<Integer> factIds)
    {
        List<ModelMetaDataDTO> list=new ArrayList<>();
        if (CollectionUtils.isEmpty(factIds))
        {
            return list;
        }
        //根据事实表id查询所有字段
        QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("associate_dimension_id").in("fact_id",factIds)
        .lambda().ne(FactAttributePO::getAssociateDimensionId,0);
        List<Integer> dimensionIds=(List)factAttributeMapper.selectObjs(queryWrapper);
        dimensionIds=dimensionIds.stream().distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dimensionIds))
        {
            for (Integer id:dimensionIds)
            {
                ModelMetaDataDTO dto=getDimensionMetaData(id);
                list.add(dto);
            }
        }
        return list;
    }

    @Override
    public ModelMetaDataDTO getDimensionMetaData(int id)
    {
        ModelMetaDataDTO data=new ModelMetaDataDTO();
        DimensionPO po=mapper.selectById(id);
        if (po==null)
        {
            return data;
        }
        data.tableName =po.dimensionTabName;
        data.id=po.id;
        data.appId=po.businessId;
        //获取注册表相关数据
        /*ResultEntity<AppRegistrationDTO> appAbbreviation = client.getData(po.appId);
        if (appAbbreviation.code==ResultEnum.SUCCESS.getCode() || appAbbreviation.data !=null)
        {
            data.appbAbreviation=appAbbreviation.data.appAbbreviation;
        }
        //获取来源表相关数据
        ResultEntity<TableAccessDTO> tableAccess = client.getTableAccess(po.tableSourceId);
        if (tableAccess.code==ResultEnum.SUCCESS.getCode() || tableAccess.data !=null)
        {
            data.sourceTableName=tableAccess.data.tableName;
        }*/
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId,id);
        List<ModelAttributeMetaDataDTO> dtoList=new ArrayList<>();
        List<DimensionAttributePO> list=attributeMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list))
        {
            return data;
        }
        for (DimensionAttributePO item:list) {
            ModelAttributeMetaDataDTO dto = new ModelAttributeMetaDataDTO();
            dto.fieldEnName = item.dimensionFieldEnName;
            dto.fieldLength = item.dimensionFieldLength;
            dto.fieldType = item.dimensionFieldType;
            dto.fieldId= String.valueOf(item.id);
            dto.attributeType=1;
            dtoList.add(dto);
            //判断维度是否关联维度
            if (item.associateDimensionId !=0 && item.associateDimensionFieldId !=0)
            {
                DimensionPO po1=mapper.selectById(item.associateDimensionId);
                if (po1 !=null)
                {
                    ModelAttributeMetaDataDTO dto1 = new ModelAttributeMetaDataDTO();
                    dto1.attributeType=2;
                    dto1.associationTable=po1.dimensionTabName;
                    dtoList.add(dto1);
                }
            }
        }
        data.dto=dtoList;
        return data;
    }

    /**
     * 生成时间日期维度表数据
     * @param list
     * @param dimensionId
     * @return
     */
    public ResultEnum addTimeTableAttribute(List<DimensionAttributeDTO> list,int dimensionId)
    {
        List<DimensionAttributePO> poList=DimensionAttributeMap.INSTANCES.dtoListToPoList(list);
        poList.stream().map(e->e.dimensionId=dimensionId).collect(Collectors.toList());
        return this.saveOrUpdateBatch(poList)==true?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<DimensionAttributeUpdateDTO> getDimensionAttributeDataList(int dimensionId)
    {
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId,dimensionId);
        List<DimensionAttributePO> list=attributeMapper.selectList(queryWrapper);
        return DimensionAttributeMap.INSTANCES.poToDetailDtoList(list);
    }



}
