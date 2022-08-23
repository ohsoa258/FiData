package com.fisk.datamodel.service.impl.dimension;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import com.fisk.datamodel.dto.dimension.DimensionListDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDataDTO;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderDTO;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderDataDTO;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderPublishQueryDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishDataDTO;
import com.fisk.datamodel.dto.tablehistory.TableHistoryDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.SyncModePO;
import com.fisk.datamodel.entity.dimension.DimensionAttributePO;
import com.fisk.datamodel.entity.dimension.DimensionFolderPO;
import com.fisk.datamodel.entity.dimension.DimensionPO;
import com.fisk.datamodel.enums.CreateTypeEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.enums.TableHistoryTypeEnum;
import com.fisk.datamodel.map.dimension.DimensionAttributeMap;
import com.fisk.datamodel.map.dimension.DimensionFolderMap;
import com.fisk.datamodel.map.dimension.DimensionMap;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.mapper.SyncModeMapper;
import com.fisk.datamodel.mapper.dimension.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.dimension.DimensionFolderMapper;
import com.fisk.datamodel.mapper.dimension.DimensionMapper;
import com.fisk.datamodel.service.IDimensionFolder;
import com.fisk.datamodel.service.impl.TableHistoryImpl;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
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
    TableHistoryImpl tableHistory;
    @Resource
    PublishTaskClient publishTaskClient;
    @Resource
    DimensionImpl dimensionImpl;
    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    SyncModeMapper syncModeMapper;

    @Override
    public ResultEnum addDimensionFolder(DimensionFolderDTO dto)
    {
        //判断共享维度文件夹是否已存在
        if (dto.share)
        {
            QueryWrapper<DimensionFolderPO> folderPoQueryWrapper=new QueryWrapper<>();
            folderPoQueryWrapper.lambda()
                    .eq(DimensionFolderPO::getDimensionFolderCnName,dto.dimensionFolderCnName)
                    .eq(DimensionFolderPO::getShare,true);
            List<DimensionFolderPO> poList=mapper.selectList(folderPoQueryWrapper);
            if (poList !=null && poList.size()>0)
            {
                return ResultEnum.DATA_NOTEXISTS;
            }
        }
        //判断同个业务域下是否存在相同维度文件夹
        QueryWrapper<DimensionFolderPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionFolderPO::getBusinessId,dto.businessId)
                .eq(DimensionFolderPO::getDimensionFolderCnName,dto.dimensionFolderCnName);
        DimensionFolderPO po=mapper.selectOne(queryWrapper);
        if (po !=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        int flat=mapper.insert(DimensionFolderMap.INSTANCES.dtoToPo(dto));
        return flat>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum delDimensionFolder(List<Integer> ids)
    {
        try {
            QueryWrapper<DimensionFolderPO> folderPoQueryWrapper=new QueryWrapper<>();
            folderPoQueryWrapper.select("id").in("id",ids);
            List<Integer> folderIds=(List)mapper.selectObjs(folderPoQueryWrapper);
            if (CollectionUtils.isEmpty(folderIds))
            {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.select("id").in("dimension_folder_id",folderIds);
            List<Integer> dimIds=(List) dimensionMapper.selectObjs(queryWrapper);
            if (dimIds==null || ids.size()==0)
            {
                return ResultEnum.SUCCESS;
            }
            //判断维度表是否存在关联
            boolean isExistAssociated=false;
            for (Integer id:dimIds)
            {
                ResultEnum resultEnum = dimensionImpl.deleteDimension(id);
                if (resultEnum.getCode()==ResultEnum.TABLE_ASSOCIATED.getCode())
                {
                    isExistAssociated=true;
                }
            }
            if (isExistAssociated)
            {
                return ResultEnum.TABLE_ASSOCIATED;
            }
            return mapper.deleteBatchIds(ids)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResultEnum.SUCCESS;
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
    public ResultEnum updateDimensionFolder(DimensionFolderDTO dto) {
        //判断数据是否存在
        DimensionFolderPO model = mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //判断共享维度文件夹是否已存在
        if (dto.share)
        {
            QueryWrapper<DimensionFolderPO> folderPoQueryWrapper=new QueryWrapper<>();
            folderPoQueryWrapper.lambda()
                    .eq(DimensionFolderPO::getDimensionFolderCnName,dto.dimensionFolderCnName)
                    .eq(DimensionFolderPO::getShare,true);
            List<DimensionFolderPO> poList=mapper.selectList(folderPoQueryWrapper);
            if (poList !=null && poList.size()>0)
            {
                if (poList.get(0).id != dto.id)
                {
                    return ResultEnum.DATA_NOTEXISTS;
                }
            }
        }
        //判断同个业务域下是否存在相同维度文件夹
        QueryWrapper<DimensionFolderPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionFolderPO::getBusinessId,dto.businessId)
                .eq(DimensionFolderPO::getDimensionFolderCnName,dto.dimensionFolderCnName);
        DimensionFolderPO po=mapper.selectOne(queryWrapper);
        if (po !=null && po.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        //维度文件夹更改不能更改业务域id
        dto.businessId=model.businessId;
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
        List<DimensionFolderDataDTO> listDtoList=new ArrayList<>();
        //根据业务域id,获取维度文件夹列表
        QueryWrapper<DimensionFolderPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").lambda()
                .eq(DimensionFolderPO::getShare,true)
                .or()
                .eq(DimensionFolderPO::getBusinessId,businessAreaId);
        List<DimensionFolderPO> dimensionFolderPoList=mapper.selectList(queryWrapper);
        if (dimensionFolderPoList==null || dimensionFolderPoList.size()==0)
        {
            return listDtoList;
        }
        listDtoList=DimensionFolderMap.INSTANCES.poListToDtoList(dimensionFolderPoList);
        List<Integer> dimIds=(List) dimensionFolderPoList.stream().map(DimensionFolderPO::getId)
                .collect(Collectors.toList());
        //根据业务域id,获取维度列表
        QueryWrapper<DimensionPO> dimensionPoQueryWrapper=new QueryWrapper<>();
        dimensionPoQueryWrapper.orderByDesc("create_time")
                .in("dimension_folder_id",dimIds.toArray());
        List<DimensionPO> list=dimensionMapper.selectList(dimensionPoQueryWrapper);
        if (list==null || list.size()==0)
        {
            return listDtoList;
        }
        for (DimensionFolderDataDTO item:listDtoList)
        {
            item.dimensionListDTO=DimensionMap.INSTANCES.listPoToListsDto(list.stream().filter(e->e.dimensionFolderId==item.id).collect(Collectors.toList()));
        }
        //获取业务域下所有维度id集合
        dimensionPoQueryWrapper.select("id");
        List<Integer> ids=(List)dimensionMapper.selectObjs(dimensionPoQueryWrapper);
        if (ids==null || ids.size()==0)
        {
            return listDtoList;
        }
        //根据维度id集合获取字段列表
        QueryWrapper<DimensionAttributePO> attributePoQueryWrapper=new QueryWrapper<>();
        attributePoQueryWrapper.in("dimension_id",ids);
        List<DimensionAttributePO> attributePoList=dimensionAttributeMapper.selectList(attributePoQueryWrapper);
        if (attributePoList==null || attributePoList.size()==0)
        {
            return listDtoList;
        }
        //循环赋值
        for (DimensionFolderDataDTO dimensionFolder:listDtoList)
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
                item.attributeList= DimensionAttributeMap.INSTANCES.poToDto(attributePoList.stream()
                        .filter(e->e.getDimensionId()==newId)
                        .sorted(Comparator.comparing(DimensionAttributePO::getCreateTime))
                        .collect(Collectors.toList()));
                //获取维度关联维度表名称和字段名称
                for (DimensionAttributeDataDTO attributeItem:item.attributeList)
                {
                    if (attributeItem.associateDimensionId !=0)
                    {
                        DimensionPO dimensionPo=dimensionMapper.selectById(attributeItem.associateDimensionId);
                        attributeItem.associateDimensionName=dimensionPo==null?"":dimensionPo.dimensionTabName;
                        DimensionAttributePO dimensionAttributePo=dimensionAttributeMapper.selectById(attributeItem.associateDimensionFieldId);
                        attributeItem.associateDimensionFieldName=dimensionAttributePo==null?"":dimensionAttributePo.dimensionFieldEnName;
                    }
                }
                //降序排列
                Collections.reverse(item.attributeList);
            }
        }
        return listDtoList;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum batchPublishDimensionFolder(DimensionFolderPublishQueryDTO dto)
    {
        try{
            BusinessAreaPO businessAreaPo=businessAreaMapper.selectById(dto.businessAreaId);
            if (businessAreaPo==null)
            {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            //获取维度文件夹下所有维度
            QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.in("id",dto.dimensionIds);
            List<DimensionPO> dimensionPoList=dimensionMapper.selectList(queryWrapper);
            if (CollectionUtils.isEmpty(dimensionPoList))
            {
                throw new FkException(ResultEnum.PUBLISH_FAILURE,"维度表为空");
            }
            //更改发布状态
            for (DimensionPO item:dimensionPoList)
            {
                item.isPublish= PublicStatusEnum.PUBLIC_ING.getValue();
                if (dimensionMapper.updateById(item)==0)
                {
                    throw new FkException(ResultEnum.PUBLISH_FAILURE);
                }
            }
            //获取维度字段数据
            QueryWrapper<DimensionAttributePO> attributePoQueryWrapper=new QueryWrapper<>();
            //获取维度id集合
            List<Integer> dimensionIds=(List) dimensionMapper.selectObjs(queryWrapper.select("id"));
            List<DimensionAttributePO> dimensionAttributePoList=dimensionAttributeMapper
                    .selectList(attributePoQueryWrapper.in("dimension_id",dimensionIds));
            //遍历取值
            ModelPublishDataDTO data=new ModelPublishDataDTO();
            data.businessAreaId=businessAreaPo.getId();
            data.businessAreaName=businessAreaPo.getBusinessName();
            data.userId=userHelper.getLoginUserInfo().id;
            data.openTransmission=dto.openTransmission;
            List<ModelPublishTableDTO> dimensionList=new ArrayList<>();
            //获取表增量配置信息
            QueryWrapper<SyncModePO> syncModePoQueryWrapper=new QueryWrapper<>();
            syncModePoQueryWrapper.lambda().eq(SyncModePO::getTableType, TableHistoryTypeEnum.TABLE_DIMENSION.getValue());
            List<SyncModePO> syncModePoList=syncModeMapper.selectList(syncModePoQueryWrapper);
            //发布历史添加数据
            addTableHistory(dto);
            for (DimensionPO item:dimensionPoList)
            {
                //拼接数据
                ModelPublishTableDTO pushDto=new ModelPublishTableDTO();
                pushDto.tableId=Integer.parseInt(String.valueOf(item.id));
                pushDto.tableName=item.dimensionTabName;
                pushDto.createType=CreateTypeEnum.CREATE_DIMENSION.getValue();
                ResultEntity<Map<String, String>> converMap = publishTaskClient.converSql(pushDto.tableName, item.sqlScript, "");
                Map<String, String> data1 = converMap.data;
                pushDto.queryEndTime = data1.get(SystemVariableTypeEnum.END_TIME.getValue());
                pushDto.sqlScript = data1.get(SystemVariableTypeEnum.QUERY_SQL.getValue());
                pushDto.queryStartTime = data1.get(SystemVariableTypeEnum.START_TIME.getValue());
                //获取维度表同步方式
                if (dto.syncMode !=0)
                {
                    pushDto.synMode=dto.syncMode;
                }
                else {
                    Optional<SyncModePO> first = syncModePoList.stream().filter(e -> e.syncTableId == item.id).findFirst();
                    if (first.isPresent())
                    {
                        pushDto.synMode=first.get().syncMode;
                    }
                }
                //获取该维度下所有维度字段
                List<ModelPublishFieldDTO> fieldList=new ArrayList<>();
                List<DimensionAttributePO> attributePoList=dimensionAttributePoList.stream().filter(e->e.dimensionId==item.id).collect(Collectors.toList());
                for (DimensionAttributePO attributePo:attributePoList)
                {
                    fieldList.add(pushField(attributePo));
                }
                pushDto.fieldList=fieldList;
                dimensionList.add(pushDto);
            }
            data.dimensionList=dimensionList;
            //发送消息
            publishTaskClient.publishBuildAtlasDorisTableTask(data);
        }
        catch (Exception ex){
            log.error(ex.getMessage());
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
        }
        return ResultEnum.SUCCESS;
    }

    public ModelPublishFieldDTO pushField(DimensionAttributePO attributePo){
        ModelPublishFieldDTO fieldDTO=new ModelPublishFieldDTO();
        fieldDTO.fieldId=attributePo.id;
        fieldDTO.fieldEnName=attributePo.dimensionFieldEnName;
        fieldDTO.fieldType=attributePo.dimensionFieldType;
        fieldDTO.fieldLength=attributePo.dimensionFieldLength;
        fieldDTO.attributeType=attributePo.attributeType;
        fieldDTO.isPrimaryKey=attributePo.isPrimaryKey;
        fieldDTO.sourceFieldName=attributePo.sourceFieldName;
        fieldDTO.associateDimensionId=attributePo.associateDimensionId;
        fieldDTO.associateDimensionFieldId=attributePo.associateDimensionFieldId;
        //判断是否关联维度
        if (attributePo.associateDimensionId !=0 && attributePo.associateDimensionFieldId !=0 )
        {
            DimensionPO dimensionPo=dimensionMapper.selectById(attributePo.associateDimensionId);
            fieldDTO.associateDimensionName=dimensionPo==null?"":dimensionPo.dimensionTabName;
            fieldDTO.associateDimensionSqlScript=dimensionPo==null?"":dimensionPo.sqlScript;
            DimensionAttributePO dimensionAttributePo=dimensionAttributeMapper.selectById(attributePo.associateDimensionFieldId);
            fieldDTO.associateDimensionFieldName=dimensionAttributePo==null?"":dimensionAttributePo.dimensionFieldEnName;
        }
        return fieldDTO;
    }

    private void addTableHistory(DimensionFolderPublishQueryDTO dto)
    {
        List<TableHistoryDTO> list=new ArrayList<>();
        for (Integer id:dto.dimensionIds)
        {
            TableHistoryDTO data=new TableHistoryDTO();
            data.remark=dto.remark;
            data.tableId=id;
            data.tableType=CreateTypeEnum.CREATE_DIMENSION.getValue();
            data.openTransmission = dto.openTransmission;
            list.add(data);
        }
        tableHistory.addTableHistory(list);
    }


}
