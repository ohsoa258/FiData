package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.datamodel.dto.dimension.DimensionListDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDataDTO;
import com.fisk.datamodel.dto.dimensionfolder.*;
import com.fisk.datamodel.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishDataDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.DimensionAttributePO;
import com.fisk.datamodel.entity.DimensionFolderPO;
import com.fisk.datamodel.entity.DimensionPO;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.map.DimensionAttributeMap;
import com.fisk.datamodel.map.DimensionFolderMap;
import com.fisk.datamodel.map.DimensionMap;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.mapper.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.DimensionFolderMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.service.IDimensionFolder;
import com.fisk.task.client.PublishTaskClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class DimensionFolderImpl
        extends ServiceImpl<DimensionFolderMapper,DimensionFolderPO>
        implements IDimensionFolder {

    @Resource
    DimensionFolderMapper mapper;
    @Resource
    DimensionMapper dimensionMapper;
    @Resource
    BusinessAreaMapper businessAreaMapper;
    @Resource
    DimensionAttributeMapper dimensionAttributeMapper;
    @Resource
    UserHelper userHelper;
    @Resource
    PublishTaskClient publishTaskClient;

    @Override
    public ResultEnum addDimensionFolder(DimensionFolderDTO dto)
    {
        QueryWrapper<DimensionFolderPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionFolderPO::getDimensionFolderCnName,dto.dimensionFolderCnName);
        DimensionFolderPO po=mapper.selectOne(queryWrapper);
        if (po !=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        int flat=mapper.insert(DimensionFolderMap.INSTANCES.dtoToPo(dto));
        return flat>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum delDimensionFolder(List<Integer> ids)
    {
        return this.removeByIds(ids)?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public DimensionFolderDTO getDimensionFolder(int id)
    {
        DimensionFolderPO po=mapper.selectById(id);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return DimensionFolderMap.INSTANCES.poToDto(po);
    }

    @Override
    public ResultEnum updateDimensionFolder(DimensionFolderDTO dto)
    {
        DimensionFolderPO model=mapper.selectById(dto.id);
        if (model==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<DimensionFolderPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionFolderPO::getDimensionFolderCnName,dto.dimensionFolderCnName);
        DimensionFolderPO po=mapper.selectOne(queryWrapper);
        if (po !=null && po.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        model=DimensionFolderMap.INSTANCES.dtoToPo(dto);
        return mapper.updateById(model)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<DimensionFolderDataDTO> getDimensionFolderList(int businessAreaId)
    {
        BusinessAreaPO po=businessAreaMapper.selectById(businessAreaId);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        List<DimensionFolderDataDTO> listDTOS=new ArrayList<>();
        //根据业务域id,获取维度文件夹列表
        QueryWrapper<DimensionFolderPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").lambda().eq(DimensionFolderPO::getBusinessId,businessAreaId);
        List<DimensionFolderPO> dimensionFolderPOList=mapper.selectList(queryWrapper);
        if (dimensionFolderPOList==null || dimensionFolderPOList.size()==0)
        {
            return listDTOS;
        }
        listDTOS=DimensionFolderMap.INSTANCES.poListToDtoList(dimensionFolderPOList);
        //根据业务域id,获取维度列表
        QueryWrapper<DimensionPO> dimensionPOQueryWrapper=new QueryWrapper<>();
        dimensionPOQueryWrapper.orderByDesc("create_time").lambda().eq(DimensionPO::getBusinessId,businessAreaId);
        List<DimensionPO> list=dimensionMapper.selectList(dimensionPOQueryWrapper);
        if (list==null || list.size()==0)
        {
            return listDTOS;
        }
        for (DimensionFolderDataDTO item:listDTOS)
        {
            item.dimensionListDTO=DimensionMap.INSTANCES.listPoToListsDto(list.stream().filter(e->e.dimensionFolderId==item.id).collect(Collectors.toList()));
        }
        //获取业务域下所有维度id集合
        dimensionPOQueryWrapper.select("id");
        List<Integer> ids=(List)dimensionMapper.selectObjs(dimensionPOQueryWrapper);
        if (ids==null || ids.size()==0)
        {
            return listDTOS;
        }
        //根据维度id集合获取字段列表
        QueryWrapper<DimensionAttributePO> attributePOQueryWrapper=new QueryWrapper<>();
        attributePOQueryWrapper.in("dimension_id",ids);
        List<DimensionAttributePO> attributePOS=dimensionAttributeMapper.selectList(attributePOQueryWrapper);
        if (attributePOS==null || attributePOS.size()==0)
        {
            return listDTOS;
        }
        //循环赋值
        for (DimensionFolderDataDTO dimensionFolder:listDTOS)
        {
            dimensionFolder.dimensionListDTO= DimensionMap.INSTANCES.listPoToListsDto(list.stream()
                    .filter(e->e.dimensionFolderId==dimensionFolder.id)
                    .collect(Collectors.toList()));
            if (dimensionFolder.dimensionListDTO==null || dimensionFolder.dimensionListDTO.size()==0)
            {
                continue;
            }
            for (DimensionListDTO item:dimensionFolder.dimensionListDTO)
            {
                int newId=Integer.parseInt(String.valueOf(item.id));
                item.attributeList= DimensionAttributeMap.INSTANCES.poToDto(attributePOS.stream()
                        .filter(e->e.getDimensionId()==newId)
                        .sorted(Comparator.comparing(DimensionAttributePO::getCreateTime))
                        .collect(Collectors.toList()));
                //获取维度关联维度表名称和字段名称
                for (DimensionAttributeDataDTO attributeItem:item.attributeList)
                {
                    if (attributeItem.associateDimensionId !=0)
                    {
                        DimensionPO dimensionPO=dimensionMapper.selectById(attributeItem.associateDimensionId);
                        attributeItem.associateDimensionName=dimensionPO==null?"":dimensionPO.dimensionTabName;
                        DimensionAttributePO dimensionAttributePO=dimensionAttributeMapper.selectById(attributeItem.associateDimensionFieldId);
                        attributeItem.associateDimensionFieldName=dimensionAttributePO==null?"":dimensionAttributePO.dimensionFieldEnName;
                    }
                }
                //降序排列
                Collections.reverse(item.attributeList);
            }
        }
        return listDTOS;

    }

    @Override
    public ResultEnum batchPublishDimensionFolder(DimensionFolderPublishQueryDTO dto)
    {
        try{
            BusinessAreaPO businessAreaPO=businessAreaMapper.selectById(dto.businessAreaId);
            if (businessAreaPO==null)
            {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            //获取维度文件夹下所有维度
            QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.in("id",dto.dimensionIds);
            List<DimensionPO> dimensionPOList=dimensionMapper.selectList(queryWrapper);
            if (dimensionPOList==null || dimensionPOList.size()==0)
            {
                throw new FkException(ResultEnum.PUBLISH_FAILURE,"维度表为空");
            }
            //获取维度字段数据
            QueryWrapper<DimensionAttributePO> attributePOQueryWrapper=new QueryWrapper<>();
            //获取维度id集合
            List<Integer> dimensionIds=(List) dimensionMapper.selectObjs(queryWrapper.select("id"));
            List<DimensionAttributePO> dimensionAttributePOList=dimensionAttributeMapper
                    .selectList(attributePOQueryWrapper.in("dimension_id",dimensionIds));
            //遍历取值
            ModelPublishDataDTO data=new ModelPublishDataDTO();
            data.businessAreaId=businessAreaPO.getId();
            data.businessAreaName=businessAreaPO.getBusinessName();
            data.createType=CreateTypeEnum.CREATE_DIMENSION.getValue();
            data.userId=userHelper.getLoginUserInfo().id;
            List<ModelPublishTableDTO> dimensionList=new ArrayList<>();
            for (DimensionPO item:dimensionPOList)
            {
                ModelPublishTableDTO pushDto=new ModelPublishTableDTO();
                pushDto.tableId=Integer.parseInt(String.valueOf(item.id));
                pushDto.tableName=item.dimensionTabName;
                pushDto.sqlScript=item.sqlScript;
                //获取该维度下所有维度字段
                List<ModelPublishFieldDTO> fieldList=new ArrayList<>();
                List<DimensionAttributePO> attributePOList=dimensionAttributePOList.stream().filter(e->e.dimensionId==item.id).collect(Collectors.toList());
                for (DimensionAttributePO attributePO:attributePOList)
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
                        DimensionPO dimensionPO=dimensionMapper.selectById(attributePO.associateDimensionId);
                        fieldDTO.associateDimensionName=dimensionPO==null?"":dimensionPO.dimensionTabName;
                        fieldDTO.associateDimensionSqlScript=dimensionPO==null?"":dimensionPO.sqlScript;
                        DimensionAttributePO dimensionAttributePO=dimensionAttributeMapper.selectById(attributePO.associateDimensionFieldId);
                        fieldDTO.associateDimensionFieldName=dimensionAttributePO==null?"":dimensionAttributePO.dimensionFieldEnName;
                    }
                    fieldList.add(fieldDTO);
                }
                pushDto.fieldList=fieldList;
                dimensionList.add(pushDto);
            }
            data.dimensionList=dimensionList;
            String aa="";
            //发送消息
            //publishTaskClient.publishBuildAtlasDorisTableTask(dimensionAttributeAddListDTO);
        }
        catch (Exception ex){
            log.error(ex.getMessage());
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
        }
        return ResultEnum.SUCCESS;
    }


}
