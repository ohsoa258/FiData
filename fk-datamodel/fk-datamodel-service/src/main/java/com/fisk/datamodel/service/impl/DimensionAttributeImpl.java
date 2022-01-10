package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderPublishQueryDTO;
import com.fisk.datamodel.dto.tablehistory.TableHistoryDTO;
import com.fisk.datamodel.entity.FactAttributePO;
import com.fisk.datamodel.dto.dimensionattribute.*;
import com.fisk.datamodel.entity.DimensionPO;
import com.fisk.datamodel.entity.DimensionAttributePO;
import com.fisk.datamodel.entity.FactPO;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.map.DimensionAttributeMap;
import com.fisk.datamodel.mapper.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.mapper.FactAttributeMapper;
import com.fisk.datamodel.service.IDimensionAttribute;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addOrUpdateDimensionAttribute(DimensionAttributeAddDTO dto)
    {
        //根据维度id,删除所有字段数据
        /*QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId,dimensionId);
        List<DimensionAttributePO> list=attributeMapper.selectList(queryWrapper);
        if (list !=null && list.size()>0)
        {
            for (DimensionAttributePO item:list)
            {
                int flat=attributeMapper.deleteByIdWithFill(item);
                if (flat==0)
                {
                    throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                }
            }
        }
        //批量添加维度字段数据
        List<DimensionAttributePO> poList=DimensionAttributeMap.INSTANCES.dtoListToPoList(dto);
        poList.stream().map(e->e.dimensionId=dimensionId).collect(Collectors.toList());
        if (!this.saveBatch(poList))
        {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }*/

        //判断是否存在
        DimensionPO dimensionPO=mapper.selectById(dto.dimensionId);
        if (dimensionPO==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
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
            return dimensionFolder.batchPublishDimensionFolder(queryDTO);
        }
        return ResultEnum.SUCCESS;
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
        data.sqlScript=dimensionPO.sqlScript;
        QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionAttributePO::getDimensionId,dimensionId);
        List<DimensionAttributePO> list=attributeMapper.selectList(queryWrapper);
        data.attributeDTOList=DimensionAttributeMap.INSTANCES.poListToDtoList(list);
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

}
