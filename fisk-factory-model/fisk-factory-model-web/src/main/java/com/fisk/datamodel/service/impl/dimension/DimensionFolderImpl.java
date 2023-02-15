package com.fisk.datamodel.service.impl.dimension;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.accessAndTask.DataTranDTO;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import com.fisk.datamodel.dto.customscript.CustomScriptQueryDTO;
import com.fisk.datamodel.dto.dimension.DimensionDTO;
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
import com.fisk.datamodel.service.impl.CustomScriptImpl;
import com.fisk.datamodel.service.impl.TableHistoryImpl;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class DimensionFolderImpl
        extends ServiceImpl<DimensionFolderMapper, DimensionFolderPO>
        implements IDimensionFolder {

    @Value("${fiData-data-dw-source}")
    private Integer targetDbId;

    @Resource
    UserClient userClient;

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
    SyncModeMapper syncModeMapper;
    @Resource
    CustomScriptImpl customScript;
    private DataSourceTypeEnum dataSourceTypeEnum;

    @Override
    public ResultEnum addDimensionFolder(DimensionFolderDTO dto) {
        //判断共享维度文件夹是否已存在
        if (dto.share) {
            QueryWrapper<DimensionFolderPO> folderPoQueryWrapper = new QueryWrapper<>();
            folderPoQueryWrapper.lambda()
                    .eq(DimensionFolderPO::getDimensionFolderCnName, dto.dimensionFolderCnName)
                    .eq(DimensionFolderPO::getShare,true);
            List<DimensionFolderPO> poList=mapper.selectList(folderPoQueryWrapper);
            if (poList != null && poList.size() > 0) {
                return ResultEnum.DATA_NOTEXISTS;
            }
        }
        //判断同个业务域下是否存在相同维度文件夹
        QueryWrapper<DimensionFolderPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionFolderPO::getBusinessId,dto.businessId)
                .eq(DimensionFolderPO::getDimensionFolderCnName,dto.dimensionFolderCnName);
        DimensionFolderPO po=mapper.selectOne(queryWrapper);
        if (po != null) {
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
            if (CollectionUtils.isEmpty(folderIds)) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.select("id").in("dimension_folder_id",folderIds);
            List<Integer> dimIds=(List) dimensionMapper.selectObjs(queryWrapper);
            if (CollectionUtils.isEmpty(dimIds)) {
                return ResultEnum.SUCCESS;
            }
            //判断维度表是否存在关联
            boolean isExistAssociated=false;
            for (Integer id : dimIds) {
                ResultEnum resultEnum = dimensionImpl.deleteDimension(id);
                if (resultEnum.getCode() == ResultEnum.TABLE_ASSOCIATED.getCode()) {
                    isExistAssociated = true;
                    break;
                }
            }
            if (isExistAssociated) {
                return ResultEnum.TABLE_ASSOCIATED;
            }
            return mapper.deleteBatchIds(ids)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
        } catch (Exception e) {
            log.error("delDimensionFolder ex:", e);
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
    }

    @Override
    public DimensionFolderDTO getDimensionFolder(int id)
    {
        DimensionFolderPO po=mapper.selectById(id);
        if (po == null) {
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
        if (dto.share) {
            QueryWrapper<DimensionFolderPO> folderPoQueryWrapper = new QueryWrapper<>();
            folderPoQueryWrapper.lambda()
                    .eq(DimensionFolderPO::getDimensionFolderCnName, dto.dimensionFolderCnName)
                    .eq(DimensionFolderPO::getShare, true);
            List<DimensionFolderPO> poList = mapper.selectList(folderPoQueryWrapper);
            if (poList != null && poList.size() > 0) {
                if (poList.get(0).id != dto.id) {
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
        if (po != null && po.id != dto.id) {
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
        if (po == null) {
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
        if (dimensionFolderPoList == null || dimensionFolderPoList.size() == 0) {
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
        if (CollectionUtils.isEmpty(list)) {
            return listDtoList;
        }
        for (DimensionFolderDataDTO item : listDtoList) {
            // TODO
            item.dimensionListDTO = DimensionMap.INSTANCES.listPoToListsDto(list.stream().filter(e -> e.dimensionFolderId == item.id).collect(Collectors.toList()));
        }
        //获取业务域下所有维度id集合
        dimensionPoQueryWrapper.select("id");
        List<Integer> ids=(List)dimensionMapper.selectObjs(dimensionPoQueryWrapper);
        if (CollectionUtils.isEmpty(ids)) {
            return listDtoList;
        }
        //根据维度id集合获取字段列表
        QueryWrapper<DimensionAttributePO> attributePoQueryWrapper=new QueryWrapper<>();
        attributePoQueryWrapper.in("dimension_id",ids);
        List<DimensionAttributePO> attributePoList=dimensionAttributeMapper.selectList(attributePoQueryWrapper);
        if (CollectionUtils.isEmpty(attributePoList)) {
            return listDtoList;
        }
        //循环赋值
        for (DimensionFolderDataDTO dimensionFolder : listDtoList) {
            dimensionFolder.dimensionListDTO = DimensionMap.INSTANCES.listPoToListsDto(list.stream()
                    .filter(e -> e.dimensionFolderId == dimensionFolder.id)
                    .collect(Collectors.toList()));
            if (CollectionUtils.isEmpty(dimensionFolder.dimensionListDTO)) {
                continue;
            }
            for (DimensionListDTO item : dimensionFolder.dimensionListDTO) {
                int newId = Integer.parseInt(String.valueOf(item.id));
                item.attributeList = DimensionAttributeMap.INSTANCES.poToDto(attributePoList.stream()
                        .filter(e -> e.getDimensionId() == newId)
                        .sorted(Comparator.comparing(DimensionAttributePO::getCreateTime))
                        .collect(Collectors.toList()));
                //获取维度关联维度表名称和字段名称
                for (DimensionAttributeDataDTO attributeItem : item.attributeList) {
                    if (attributeItem.associateDimensionId != 0) {
                        DimensionPO dimensionPo = dimensionMapper.selectById(attributeItem.associateDimensionId);
                        attributeItem.associateDimensionName = dimensionPo == null ? "" : dimensionPo.dimensionTabName;
                        DimensionAttributePO dimensionAttributePo = dimensionAttributeMapper.selectById(attributeItem.associateDimensionFieldId);
                        attributeItem.associateDimensionFieldName = dimensionAttributePo == null ? "" : dimensionAttributePo.dimensionFieldEnName;
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
        try {
            BusinessAreaPO businessAreaPo = businessAreaMapper.selectById(dto.businessAreaId);
            if (businessAreaPo == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }

            getDwDbType(targetDbId);

            //获取维度文件夹下所有维度
            QueryWrapper<DimensionPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("id", dto.dimensionIds);
            List<DimensionPO> dimensionPoList = dimensionMapper.selectList(queryWrapper);
            if (CollectionUtils.isEmpty(dimensionPoList)) {
                throw new FkException(ResultEnum.PUBLISH_FAILURE, "维度表为空");
            }
            //更改发布状态
            for (DimensionPO item : dimensionPoList) {
                item.isPublish = PublicStatusEnum.PUBLIC_ING.getValue();
                if (dimensionMapper.updateById(item) == 0) {
                    throw new FkException(ResultEnum.PUBLISH_FAILURE);
                }
            }
            //获取维度字段数据
            //获取维度id集合
            List<Integer> dimensionIds = (List) dimensionMapper.selectObjs(queryWrapper.select("id"));
            QueryWrapper<DimensionAttributePO> attributePoQueryWrapper = new QueryWrapper<>();
            attributePoQueryWrapper.in("dimension_id", dimensionIds);
            List<DimensionAttributePO> dimensionAttributePoList = dimensionAttributeMapper
                    .selectList(attributePoQueryWrapper);
            //遍历取值
            ModelPublishDataDTO data = new ModelPublishDataDTO();
            data.businessAreaId = businessAreaPo.getId();
            data.businessAreaName = businessAreaPo.getBusinessName();
            data.userId = userHelper.getLoginUserInfo().id;
            data.openTransmission = dto.openTransmission;
            List<ModelPublishTableDTO> dimensionList = new ArrayList<>();
            //获取表增量配置信息
            QueryWrapper<SyncModePO> syncModePoQueryWrapper=new QueryWrapper<>();
            syncModePoQueryWrapper.lambda().eq(SyncModePO::getTableType, TableHistoryTypeEnum.TABLE_DIMENSION.getValue());
            List<SyncModePO> syncModePoList=syncModeMapper.selectList(syncModePoQueryWrapper);
            //发布历史添加数据
            addTableHistory(dto);

            /*
            // 批量查询数据接入应用id对应的目标源id集合——应用appId修改为dataSourceId后，该段代码暂时不用
            List<Integer> appIds = dimensionPoList.stream().map(DimensionPO::getAppId).collect(Collectors.toList());
            List<AppRegistrationInfoDTO> targetDbIdList = new ArrayList<>();
            try {
                ResultEntity<List<AppRegistrationInfoDTO>> resultEntity = dataAccessClient.getBatchTargetDbIdByAppIds(appIds);
                targetDbIdList = resultEntity.data;
            }catch (Exception e){
                throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
            }
             */

            for (DimensionPO item : dimensionPoList) {
                //拼接数据
                ModelPublishTableDTO pushDto = new ModelPublishTableDTO();
                pushDto.tableId = Integer.parseInt(String.valueOf(item.id));
                pushDto.tableName = convertName(item.dimensionTabName);
                pushDto.createType = CreateTypeEnum.CREATE_DIMENSION.getValue();
                DataTranDTO dtDto = new DataTranDTO();
                dtDto.tableName = pushDto.tableName;
                dtDto.querySql = item.sqlScript;
                ResultEntity<Map<String, String>> converMap = publishTaskClient.converSql(dtDto);
                Map<String, String> data1 = converMap.data;
                pushDto.queryEndTime = data1.get(SystemVariableTypeEnum.END_TIME.getValue());
                pushDto.sqlScript = data1.get(SystemVariableTypeEnum.QUERY_SQL.getValue());
                pushDto.queryStartTime = data1.get(SystemVariableTypeEnum.START_TIME.getValue());

                //获取维度键update语句
                pushDto.factUpdateSql = item.dimensionKeyScript;

                /*
                // 关联数据来源数据库Id——应用appId修改为dataSourceId后，该段代码暂时不用
                AppRegistrationInfoDTO appDto = targetDbIdList.stream().filter(e -> e.getAppId() == item.getAppId()).findFirst().orElse(null);
                if (appDto != null){
                    pushDto.dataSourceDbId = appDto.getTargetDbId();
                }
                 */
                pushDto.setDataSourceDbId(item.dataSourceId);

                // 设置目标dw库id
                pushDto.setTargetDbId(targetDbId);

                // 设置维度表临时表名称
                pushDto.setPrefixTempName(item.getPrefixTempName() + "_");

                //获取自定义脚本
                CustomScriptQueryDTO customScriptDto = new CustomScriptQueryDTO();
                customScriptDto.type = 1;
                customScriptDto.tableId = Integer.parseInt(String.valueOf(item.id));
                customScriptDto.execType = 1;
                String beforeCustomScript = customScript.getBatchScript(customScriptDto);
                if (!StringUtils.isEmpty(beforeCustomScript)) {
                    pushDto.customScript = beforeCustomScript;
                }
                customScriptDto.execType = 2;

                //自定义脚本
                String batchScript = customScript.getBatchScript(customScriptDto);
                if (!StringUtils.isEmpty(batchScript)) {
                    pushDto.customScriptAfter = batchScript;
                }

                //获取维度表同步方式
                Optional<SyncModePO> first = syncModePoList.stream().filter(e -> e.syncTableId == item.id).findFirst();
                if (first.isPresent()) {
                    pushDto.synMode = first.get().syncMode;
                    pushDto.maxRowsPerFlowFile = first.get().maxRowsPerFlowFile;
                    pushDto.fetchSize = first.get().fetchSize;
                } else {
                    pushDto.synMode = dto.syncMode;
                }
                //获取该维度下所有维度字段
                List<ModelPublishFieldDTO> fieldList = new ArrayList<>();
                List<DimensionAttributePO> attributePoList = dimensionAttributePoList.stream().filter(e -> e.dimensionId == item.id).collect(Collectors.toList());
                for (DimensionAttributePO attributePo : attributePoList) {
                    fieldList.add(pushField(attributePo));
                }
                pushDto.fieldList = fieldList;
                dimensionList.add(pushDto);
            }
            data.dimensionList = dimensionList;
            //发送消息
            publishTaskClient.publishBuildAtlasDorisTableTask(data);
        } catch (Exception ex) {
            log.error("batchPublishDimensionFolder ex:", ex);
            throw new FkException(ResultEnum.PUBLISH_FAILURE);
        }
        return ResultEnum.SUCCESS;
    }

    public ModelPublishFieldDTO pushField(DimensionAttributePO attributePo) {
        ModelPublishFieldDTO fieldDTO = new ModelPublishFieldDTO();
        fieldDTO.fieldId = attributePo.id;
        fieldDTO.fieldEnName = convertName(attributePo.dimensionFieldEnName);
        fieldDTO.fieldType = attributePo.dimensionFieldType;
        fieldDTO.fieldLength = attributePo.dimensionFieldLength;
        fieldDTO.attributeType = attributePo.attributeType;
        fieldDTO.isPrimaryKey = attributePo.isPrimaryKey;
        fieldDTO.sourceFieldName = attributePo.sourceFieldName;
        fieldDTO.associateDimensionId = attributePo.associateDimensionId;
        fieldDTO.associateDimensionFieldId = attributePo.associateDimensionFieldId;
        //判断是否关联维度
        if (attributePo.associateDimensionId != 0 && attributePo.associateDimensionFieldId != 0) {
            DimensionPO dimensionPo = dimensionMapper.selectById(attributePo.associateDimensionId);
            fieldDTO.associateDimensionName = dimensionPo == null ? "" : dimensionPo.dimensionTabName;
            fieldDTO.associateDimensionSqlScript = dimensionPo == null ? "" : dimensionPo.sqlScript;
            DimensionAttributePO dimensionAttributePo = dimensionAttributeMapper.selectById(attributePo.associateDimensionFieldId);
            fieldDTO.associateDimensionFieldName = dimensionAttributePo == null ? "" : dimensionAttributePo.dimensionFieldEnName;
        }
        return fieldDTO;
    }

    public void getDwDbType(Integer dbId) {
        ResultEntity<DataSourceDTO> fiDataDataSourceById = userClient.getFiDataDataSourceById(dbId);
        if (fiDataDataSourceById.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        dataSourceTypeEnum = fiDataDataSourceById.data.conType;
    }

    public String convertName(String name) {
        if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
            return name.toLowerCase();
        }
        return name;
    }

    /**
     * 维度表发布历史
     *
     * @param dto
     */
    private void addTableHistory(DimensionFolderPublishQueryDTO dto) {
        List<TableHistoryDTO> list = new ArrayList<>();
        for (Integer id : dto.dimensionIds) {
            TableHistoryDTO data = new TableHistoryDTO();
            data.remark = dto.remark;
            data.tableId = id;
            data.tableType = CreateTypeEnum.CREATE_DIMENSION.getValue();
            data.openTransmission = dto.openTransmission;
            list.add(data);
        }
        tableHistory.addTableHistory(list);
    }

    /**
     * 新增公共域维度
     */
    public void addPublicDimensionFolder() {
        List<DimensionFolderPO> list = this.query().eq("share", true).list();
        if (!CollectionUtils.isEmpty(list)) {
            return;
        }
        DimensionFolderPO po = new DimensionFolderPO();
        po.dimensionFolderCnName = "公共域维度";
        po.dimensionFolderEnName = "Common domain dimension";
        po.share = true;
        if (!this.save(po)) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
    }

    /**
     * 新增业务域系统文件夹
     *
     * @param businessAreaId
     */
    public void addSystemDimensionFolder(long businessAreaId) {
        DimensionFolderPO po = new DimensionFolderPO();
        po.share = false;
        po.dimensionFolderEnName = "Current domain dimension";
        po.dimensionFolderCnName = "当前域维度";
        po.businessId = (int) businessAreaId;
        if (!this.save(po)) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
    }

    @Override
    public DimensionFolderDTO getDimensionFolderByTableName(String tableName) {
        DimensionDTO dimensionByName = dimensionImpl.getDimensionByName(tableName);
        DimensionFolderPO po = mapper.selectById(dimensionByName.dimensionFolderId);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return DimensionFolderMap.INSTANCES.poToDto(po);
    }


}
