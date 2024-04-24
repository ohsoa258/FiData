package com.fisk.mdm.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.common.core.enums.datamanage.ClassificationTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.server.metadata.ClassificationInfoDTO;
import com.fisk.common.service.accessAndModel.AccessAndModelAppDTO;
import com.fisk.common.service.accessAndModel.AccessAndModelTableDTO;
import com.fisk.common.service.accessAndModel.AccessAndModelTableTypeEnum;
import com.fisk.common.service.accessAndModel.ServerTypeEnum;
import com.fisk.common.service.dbMetaData.dto.ColumnQueryDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataColumnAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDbAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataTableAttributeDTO;
import com.fisk.dataaccess.dto.taskschedule.ComponentIdDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.model.ModelDTO;
import com.fisk.mdm.dto.model.ModelQueryDTO;
import com.fisk.mdm.dto.model.ModelUpdateDTO;
import com.fisk.mdm.dto.modelVersion.ModelVersionDTO;
import com.fisk.mdm.entity.*;
import com.fisk.mdm.enums.*;
import com.fisk.mdm.map.EntityMap;
import com.fisk.mdm.map.ModelMap;
import com.fisk.mdm.map.ModelVersionMap;
import com.fisk.mdm.mapper.AttributeMapper;
import com.fisk.mdm.mapper.EntityMapper;
import com.fisk.mdm.mapper.ModelMapper;
import com.fisk.mdm.service.*;
import com.fisk.mdm.utils.mdmBEBuild.TableNameGenerateUtils;
import com.fisk.mdm.vo.entity.EntityVO;
import com.fisk.mdm.vo.model.ModelInfoVO;
import com.fisk.mdm.vo.model.ModelVO;
import com.fisk.mdm.vo.viwGroup.ViwGroupVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.relenish.ReplenishUserInfo;
import com.fisk.system.relenish.UserFieldEnum;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.model.TableDTO;
import com.fisk.task.dto.task.BuildDeleteTableApiServiceDTO;
import com.fisk.task.dto.task.BuildDeleteTableServiceDTO;
import com.fisk.task.enums.OlapTableEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fisk.mdm.utils.mdmBEBuild.TableNameGenerateUtils.*;
import static com.fisk.mdm.utils.mdmBEBuild.TableNameGenerateUtils.generateViwTableName;

/**
 * @author ChenYa
 */
@Service
public class ModelServiceImpl extends ServiceImpl<ModelMapper, ModelPO> implements IModelService {

    @Resource
    EventLogService logService;

    @Resource
    EntityMapper entityMapper;

    @Resource
    AttributeService attributeService;

    @Resource
    IModelVersionService iModelVersionService;

    @Resource
    private PublishTaskClient publishTaskClient;

    @Resource
    private UserClient userClient;

    @Resource
    private UserHelper userHelper;

    @Resource
    IModelService modelService;

    @Resource
    EntityService entityService;

    @Resource
    ViwGroupService viwGroupService;

    @Resource
    RedisUtil redisUtil;

    @Resource
    DataManageClient dataManageClient;


    @Autowired
    AccessDataService accessDataService;

    @Value("${open-metadata}")
    private Boolean openMetadata;

    /**
     * 通过id查询
     *
     * @param id id
     * @return ModelVO
     */
    @Override
    public ResultEntity<ModelVO> getById(Integer id) {
        ModelVO modelVO = ModelMap.INSTANCES.poToVo(baseMapper.selectById(id));
        if (Objects.isNull(modelVO)) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, modelVO);
    }

    /**
     * 添加模型
     *
     * @param modelDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addData(ModelDTO modelDTO) {

        //判断名称是否存在
        QueryWrapper<ModelPO> wrapper = new QueryWrapper<>();
        wrapper.eq("name", modelDTO.name);
        if (baseMapper.selectOne(wrapper) != null) {
            return ResultEnum.NAME_EXISTS;
        }

        //添加数据
        ModelPO modelPO = ModelMap.INSTANCES.dtoToPo(modelDTO);
        if (baseMapper.insert(modelPO) <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        //创建成功后创建默认版本
        ModelVersionDTO modelVersionDTO = new ModelVersionDTO();
        modelVersionDTO.setModelId((int) modelPO.getId());
        modelVersionDTO.setName("VERSION1");
        modelVersionDTO.setDesc("默认版本");
        modelVersionDTO.setStatus(ModelVersionStatusEnum.OPEN.getValue());
        modelVersionDTO.setType(ModelVersionTypeEnum.SYSTEM_CREAT.getValue());
        ModelVersionPO modelVersionPO = ModelVersionMap.INSTANCES.dtoToPo(modelVersionDTO);
        if (!iModelVersionService.save(modelVersionPO)) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        //回填current_version_id字段、attribute_log_name字段
        modelPO.setCurrentVersionId((int) modelVersionPO.getId());
        modelPO.setAttributeLogName("tb_attribute_log_" + modelPO.getId());
        if (baseMapper.updateById(modelPO) <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        //发布创建属性日志表任务
        com.fisk.task.dto.model.ModelDTO dto = new com.fisk.task.dto.model.ModelDTO();
        dto.setAttributeLogName(modelPO.attributeLogName);
        dto.setUserId(userHelper.getLoginUserInfo().getId());
        if (publishTaskClient.pushModelByName(dto).getCode() != ResultEnum.SUCCESS.getCode()) {
            return ResultEnum.CREATE_ATTRIBUTE_LOG_TABLE_ERROR;
        }


        // 记录日志
        String desc = "新增一个模型,id:" + modelPO.getId();
        logService.saveEventLog((int) modelPO.getId(), ObjectTypeEnum.MODEL, EventTypeEnum.SAVE, desc);

        //同步数据资产业务分类
        syncMetadataClassification(modelDTO.getDisplayName(),modelDTO.getDesc(),false);
        //创建成功
        return ResultEnum.SUCCESS;
    }

    /**
     * 编辑
     *
     * @param modelUpdateDTO
     * @return
     */
    @Override
    public ResultEnum editData(ModelUpdateDTO modelUpdateDTO) {
        ModelPO modelPO = baseMapper.selectById(modelUpdateDTO.getId());

        //判断数据是否存在
        if (modelPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        //判断修改后的名称是否存在
        QueryWrapper<ModelPO> wrapper = new QueryWrapper<>();
        wrapper.eq("name", modelUpdateDTO.getName())
                .ne("id", modelUpdateDTO.getId());
        if (baseMapper.selectOne(wrapper) != null) {
            return ResultEnum.NAME_EXISTS;
        }

        //把DTO转化到查询出来的PO上
        modelPO = ModelMap.INSTANCES.updateDtoToPo(modelUpdateDTO);


        //修改数据
        if (baseMapper.updateById(modelPO) <= 0) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 记录日志
        String desc = "修改一个模型,id:" + modelUpdateDTO.getId();
        logService.saveEventLog((int) modelPO.getId(), ObjectTypeEnum.MODEL, EventTypeEnum.UPDATE, desc);

        //同步数据资产业务分类
        syncMetadataClassification(modelUpdateDTO.getDisplayName(),modelUpdateDTO.getDesc(),false);

        //添加成功
        return ResultEnum.SUCCESS;
    }

    /**
     * 同步数据资产业务分类
     * @param modelDisplayName
     * @param modelDes
     * @param delete
     */
    public void  syncMetadataClassification(String modelDisplayName,String modelDes,boolean delete){
        if(openMetadata){
            ClassificationInfoDTO classificationInfoDTO=new ClassificationInfoDTO();
            classificationInfoDTO.setName(modelDisplayName);
            classificationInfoDTO.setDescription(modelDes);
            classificationInfoDTO.setSourceType(ClassificationTypeEnum.MASTER_DATA);
            classificationInfoDTO.setDelete(delete);
            dataManageClient.appSynchronousClassification(classificationInfoDTO);

        }
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @Override
    public ResultEnum deleteDataById(Integer id) {
        //判断数据是否存在
        ModelPO modelPO = baseMapper.selectById(id);

        if (modelPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        //删除数据
        if (baseMapper.deleteById(id) <= 0) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        LambdaQueryWrapper<AccessDataPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessDataPO::getModelId,id);
        List<AccessDataPO> tableApiServicePOS = accessDataService.list(queryWrapper);
        List<Long> tableApiIdList = tableApiServicePOS.stream().map(i->i.getEntityId().longValue()).collect(Collectors.toList());
        BuildDeleteTableServiceDTO buildDeleteTableApiServiceDTO = new BuildDeleteTableServiceDTO();
        buildDeleteTableApiServiceDTO.ids = tableApiIdList;
        buildDeleteTableApiServiceDTO.appId = String.valueOf(id);
        buildDeleteTableApiServiceDTO.olapTableEnum = OlapTableEnum.MDM_DATA_ACCESS;
        buildDeleteTableApiServiceDTO.userId = userHelper.getLoginUserInfo().id;
        buildDeleteTableApiServiceDTO.delBusiness = true;
        publishTaskClient.publishDeleteAccessMdmNifiFlowTask(buildDeleteTableApiServiceDTO);
        LambdaQueryWrapper<EntityPO> entityQueryWrapper = new LambdaQueryWrapper<>();
        entityQueryWrapper.eq(EntityPO::getModelId,modelPO.getId());
        List<EntityPO> entityPOS = entityMapper.selectList(entityQueryWrapper);
        List<Integer> ids = entityPOS.stream().map(i->(int)i.getId()).collect(Collectors.toList());
        // 删除实体下的属性
        if (CollectionUtils.isNotEmpty(ids)){
            this.deleteAttrByEntityIds(ids);
        }
        for (EntityPO entityPO : entityPOS) {
            TableDTO tableDTO = new TableDTO();
            tableDTO.setLogTableName(generateLogTableName(modelPO.getName(),entityPO.getName()));
            tableDTO.setStgTableName(generateStgTableName(modelPO.getName(),entityPO.getName()));
            tableDTO.setMdmTableName(generateMdmTableName(modelPO.getName(),entityPO.getName()));
            tableDTO.setViwTableName(generateViwTableName(modelPO.getName(),entityPO.getName()));
            publishTaskClient.deleteBackendTable(tableDTO);
        }

        // 记录日志
        String desc = "删除一个模型,id:" + id;
        logService.saveEventLog(id, ObjectTypeEnum.MODEL, EventTypeEnum.DELETE, desc);

        //同步数据资产业务分类
        syncMetadataClassification(modelPO.getDisplayName(),modelPO.getDesc(),true);

        //删除成功
        return ResultEnum.SUCCESS;
    }

    /**
     * 删除实体下的属性
     *
     * @param entityIds
     */
    public void deleteAttrByEntityIds(List<Integer> entityIds) {
        QueryWrapper<AttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .in(AttributePO::getEntityId, entityIds);
        List<AttributePO> list = attributeService.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            List<Long> ids = list.stream().filter(Objects::nonNull).map(e -> {
                return e.getId();
            }).collect(Collectors.toList());
            attributeService.removeByIds(ids);
        }
    }

    /**
     * 分页查询
     *
     * @param query
     * @return
     */
    @Override
    public Page<ModelVO> getAll(ModelQueryDTO query) {

        Page<ModelVO> all = baseMapper.getAll(query.page, query);

        //获取创建人、修改人名称
        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            ReplenishUserInfo.replenishUserName(all.getRecords(), userClient, UserFieldEnum.USER_ACCOUNT);
        }

        return all;
    }

    /**
     * 通过模型id获取实体
     *
     * @param modelId 模型id
     * @return {@link ModelInfoVO}
     */
    @Override
    public ModelInfoVO getEntityById(Integer modelId, String name) {
        //判断模型是否存在
        ModelPO modelPo = baseMapper.selectById(modelId);
        if (modelPo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        //查询实体
        ModelInfoVO modelInfoVO = ModelMap.INSTANCES.poToInfoVO(modelPo);
        QueryWrapper<EntityPO> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(EntityPO::getModelId, modelId);

        // 追加模糊搜索条件
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(name)) {
            wrapper.lambda().and(wq -> wq
                    .like(EntityPO::getName, name)
                    .or()
                    .like(EntityPO::getDisplayName, name)
                    .or()
                    .like(EntityPO::getDesc, name));
        }

        List<EntityPO> entityPos = entityMapper.selectList(wrapper);

        //判断是否存在实体
        if (!CollectionUtils.isNotEmpty(entityPos)) {
            return modelInfoVO;
        }

        List<EntityVO> entityVos = EntityMap.INSTANCES.poToVoList(entityPos);

        //获取创建人、修改人名称
        ReplenishUserInfo.replenishUserName(entityVos, userClient, UserFieldEnum.USER_ACCOUNT);

        //最终返回
        modelInfoVO.setEntityVOList(entityVos);
        return modelInfoVO;
    }

    @Override
    public List<FiDataMetaDataDTO> getDataStructure(FiDataMetaDataReqDTO reqDto) {
        boolean flag = redisUtil.hasKey(RedisKeyBuild.buildFiDataStructureKey(reqDto.dataSourceId));
        if (!flag) {
            // 将数据接入结构存入redis
            modelService.setDataStructure(reqDto);
        }
        List<FiDataMetaDataDTO> list = null;
        String dataAccessStructure = redisUtil.get(RedisKeyBuild.buildFiDataStructureKey(reqDto.dataSourceId)).toString();
        if (StringUtils.isNotBlank(dataAccessStructure)) {
            list = JSONObject.parseArray(dataAccessStructure, FiDataMetaDataDTO.class);
        }

        return list;
    }

    @Override
    public boolean setDataStructure(FiDataMetaDataReqDTO reqDto) {
        List<FiDataMetaDataDTO> list = new ArrayList<>();
        FiDataMetaDataDTO dto = new FiDataMetaDataDTO();
        dto.setDataSourceId(Integer.parseInt(StringUtils.isBlank(reqDto.dataSourceId) ? String.valueOf(0) : reqDto.dataSourceId));

        // 第一层id
        String uuid = reqDto.dataSourceId;
        List<FiDataMetaDataTreeDTO> dataTreeList = new ArrayList<>();
        FiDataMetaDataTreeDTO dataTree = new FiDataMetaDataTreeDTO();
        dataTree.setId(uuid);
        dataTree.setParentId("-10");
        dataTree.setLabel(reqDto.getDataSourceName());
        dataTree.setLabelAlias(reqDto.getDataSourceName());
        dataTree.setLevelType(LevelTypeEnum.DATABASE);
        // 获取模型数据
        dataTree.setChildren(this.getModelData(uuid));
        dataTreeList.add(dataTree);

        dto.setChildren(dataTreeList);
        list.add(dto);

        // 放入redis缓存
        if (!org.springframework.util.CollectionUtils.isEmpty(list)) {
            redisUtil.set(RedisKeyBuild.buildFiDataStructureKey(reqDto.dataSourceId), JSON.toJSONString(list));
        }
        return true;
    }

    @Override
    public List<TableNameDTO> getTableDataStructure(FiDataMetaDataReqDTO reqDto) {
        List<TableNameDTO> tableNames = new ArrayList<>();
        //获取实体表名
        List<ModelPO> modelPoList = baseMapper.selectList(null);
        modelPoList = modelPoList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        List<Long> modelIds = modelPoList.stream().map(BasePO::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(modelIds)){
            LambdaQueryWrapper<EntityPO> entityQueryWrapper = new LambdaQueryWrapper<>();
            entityQueryWrapper.in(EntityPO::getModelId,modelIds);
            List<EntityPO> entityPOList = entityMapper.selectList(entityQueryWrapper);
            if (CollectionUtils.isNotEmpty(entityPOList)){
                for (EntityPO entityPO : entityPOList) {
                    TableNameDTO tableName = new TableNameDTO();
                    tableName.setTableId(String.valueOf(entityPO.getId()));
                    tableName.setTableName(entityPO.getTableName());
                    tableName.setTableBusinessTypeEnum(TableBusinessTypeEnum.ENTITY_TABLR);
                    tableNames.add(tableName);
                }
            }
        }
        return tableNames;
    }

    @Override
    public List<TableColumnDTO> getFieldDataStructure(ColumnQueryDTO reqDto) {
        List<AttributeInfoDTO> attributeList = entityService.getAttributeById(Integer.valueOf(reqDto.getTableId()), null).getAttributeList();
        return attributeList.stream().map(i->{
            TableColumnDTO tableColumnDTO = new TableColumnDTO();
            tableColumnDTO.setFieldId(String.valueOf(i.getId()));
            if (i.getDataTypeLength() == null){
                tableColumnDTO.setFieldLength(0);
            }else {
                tableColumnDTO.setFieldLength(i.getDataTypeLength());
            }
            tableColumnDTO.setFieldName(i.getColumnName());
            tableColumnDTO.setFieldDes(i.getDesc());
            tableColumnDTO.setFieldPrecision(i.getDataTypeDecimalLength());
            return tableColumnDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 获取模型名称和实体名称
     * @param dto
     * @return
     */
    @Override
    public ResultEntity<ComponentIdDTO> getModelNameAndEntityName(DataAccessIdsDTO dto) {
        ModelPO modelPO = modelService.query().eq("id", dto.appId).one();

        ComponentIdDTO componentIdDTO = new ComponentIdDTO();
        componentIdDTO.appName = modelPO == null ? "" : modelPO.name;
        if (dto.tableId != null) {
            ChannelDataEnum type = ChannelDataEnum.getName(dto.flag);
            switch (type) {
                // 主数据
                case MDM_TABLE_TASK:
                    QueryWrapper<EntityPO> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("id",dto.tableId);
                    EntityPO entityPO = entityMapper.selectOne(queryWrapper);
                    componentIdDTO.tableName = entityPO == null ? "" : entityPO.getName();
                    break;
                default:
                    break;
            }
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, componentIdDTO);
    }

    /**
     * 获取主数据业务分类
     * @return
     */
    @Override
    public List<AppBusinessInfoDTO> getMasterDataModel() {
        List<ModelPO> modelPOS = baseMapper.selectList(null);
        List<AppBusinessInfoDTO> appBusinessInfoDTOList=  modelPOS.stream().map(e->{
             AppBusinessInfoDTO appBusinessInfoDTO=new AppBusinessInfoDTO();
             appBusinessInfoDTO.setName(e.getDisplayName());
             appBusinessInfoDTO.setAppDes(e.getDesc());
             appBusinessInfoDTO.setSourceType(ClassificationTypeEnum.MASTER_DATA.getValue());
             return appBusinessInfoDTO;
        }).collect(Collectors.toList());
        return appBusinessInfoDTOList;
    }

    @Override
    public List<AccessAndModelAppDTO> getAllModelAndEntitys() {
        List<ModelPO> modelPOList = modelService.list();
        List<AccessAndModelAppDTO> list = new ArrayList<>();
        //查询组装数据
        for (ModelPO modelPO : modelPOList) {
            AccessAndModelAppDTO accessAndModelAppDTO = new AccessAndModelAppDTO();
            accessAndModelAppDTO.setAppId((int)modelPO.id);
            accessAndModelAppDTO.setAppName(modelPO.name);
            accessAndModelAppDTO.setServerType(ServerTypeEnum.MDM.getValue());
            LambdaQueryWrapper<EntityPO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(EntityPO::getModelId,modelPO.getId());
            List<EntityPO> entityPOS = entityMapper.selectList(queryWrapper);
            List<AccessAndModelTableDTO> accessAndModelTableDTOS = entityPOS.stream().map(i -> {
                AccessAndModelTableDTO accessAndModelTableDTO = new AccessAndModelTableDTO();
                accessAndModelTableDTO.setTblId((int) i.getId());
                accessAndModelTableDTO.setTableName(i.getTableName());
                accessAndModelTableDTO.setDisplayTableName(i.getDisplayName());
                accessAndModelTableDTO.setTableType(OlapTableEnum.MDM_DATA_ACCESS.getValue());
                return accessAndModelTableDTO;
            }).collect(Collectors.toList());
            accessAndModelAppDTO.setTables(accessAndModelTableDTOS);
            list.add(accessAndModelAppDTO);
        }
        return list;
    }


    /**
     * 获取模型、实体、属性 数据结构Tree
     *
     * @param id
     * @return
     */
    public List<FiDataMetaDataTreeDTO> getModelData(String id) {
        // 视图和自定义视图缺少视图字段
        List<ModelPO> modelPoList = baseMapper.selectList(null);
        List<FiDataMetaDataTreeDTO> modelDataList = modelPoList.stream().filter(Objects::nonNull)
                .map(e -> {
                    // 模型层级
                    String modelUuid = String.valueOf(e.getId());
                    FiDataMetaDataTreeDTO dto = new FiDataMetaDataTreeDTO();
                    dto.setId(modelUuid);
                    dto.setParentId(id);
                    dto.setLabel(e.getName());
                    dto.setLabelAlias(e.getDisplayName());
                    dto.setLevelType(LevelTypeEnum.FOLDER);
                    dto.setSourceType(1);
                    dto.setSourceId(Integer.parseInt(id));

                    List<FiDataMetaDataTreeDTO> dataList = new ArrayList<>();

                    List<EntityVO> entityVoList = modelService.getEntityById((int) e.getId(), null).getEntityVOList();

                    if (CollectionUtils.isNotEmpty(entityVoList)) {
                        // 获取模型下的实体
                        List<FiDataMetaDataTreeDTO> entityDataList = entityVoList.stream().filter(Objects::nonNull)
                                .map(item -> {

                                    // 实体层级
                                    String entityUuid = String.valueOf(item.getId());
                                    String entityName = item.getName();
                                    String displayName = item.getDisplayName();
                                    FiDataMetaDataTreeDTO entityDto = new FiDataMetaDataTreeDTO();
                                    entityDto.setId(entityUuid);
                                    entityDto.setParentId(modelUuid);
                                    entityDto.setLabel(entityName);
                                    entityDto.setLabelAlias(displayName);
                                    entityDto.setLevelType(LevelTypeEnum.TABLE);
                                    entityDto.setSourceType(1);
                                    entityDto.setSourceId(Integer.parseInt(id));
                                    entityDto.setLabelBusinessType(TableBusinessTypeEnum.NONE.getValue());

                                    // 获取实体下的属性
                                    List<AttributeInfoDTO> attributeList = entityService.getAttributeById(item.getId(), null).getAttributeList();
                                    List<FiDataMetaDataTreeDTO> attributeDataList = attributeList.stream().filter(Objects::nonNull)
                                            .map(iter -> {

                                                // 属性层级
                                                String attributeUuid = String.valueOf(iter.getId());
                                                FiDataMetaDataTreeDTO attributeDto = new FiDataMetaDataTreeDTO();
                                                attributeDto.setId(attributeUuid);
                                                attributeDto.setParentId(entityUuid);
                                                attributeDto.setParentName(entityName);
                                                attributeDto.setParentNameAlias(displayName);
                                                attributeDto.setLabel(iter.getName());
                                                attributeDto.setLabelAlias(iter.getDisplayName());
                                                attributeDto.setLevelType(LevelTypeEnum.FIELD);
                                                String syncStatus = iter.getSyncStatus();
                                                if (syncStatus.equals(AttributeSyncStatusEnum.SUCCESS.getName())) {
                                                    attributeDto.setPublishState("1");
                                                } else {
                                                    attributeDto.setPublishState("0");
                                                }
                                                // 字段信息
                                                Integer dataTypeLength = iter.getDataTypeLength();
                                                if (dataTypeLength != null) {
                                                    attributeDto.setLabelLength(dataTypeLength.toString());
                                                }
                                                attributeDto.setLabelType(iter.getDataType());
                                                attributeDto.setLabelDesc(iter.getDesc());
                                                attributeDto.setSourceType(1);
                                                attributeDto.setSourceId(Integer.parseInt(id));
                                                attributeDto.setLabelBusinessType(TableBusinessTypeEnum.NONE.getValue());
                                                return attributeDto;
                                            }).collect(Collectors.toList());

                                    entityDto.setChildren(attributeDataList);
                                    return entityDto;
                                }).collect(Collectors.toList());
                        dataList.addAll(entityDataList);

                        // 获取模型下实体的视图
                        List<FiDataMetaDataTreeDTO> viwDataList = entityVoList.stream().filter(Objects::nonNull)
                                .map(item -> {

                                    // 视图层级
                                    String viwName = TableNameGenerateUtils.generateViwTableName(e.getName(),item.getName());
                                    String entityUuid = String.valueOf(item.getId());
                                    FiDataMetaDataTreeDTO entityDto = new FiDataMetaDataTreeDTO();
                                    entityDto.setId(entityUuid);
                                    entityDto.setParentId(modelUuid);
                                    entityDto.setLabel(viwName);
                                    entityDto.setLabelAlias(viwName);
                                    entityDto.setLevelType(LevelTypeEnum.VIEW);
                                    entityDto.setSourceType(1);
                                    entityDto.setSourceId(Integer.parseInt(id));
                                    entityDto.setLabelBusinessType(TableBusinessTypeEnum.NONE.getValue());
                                    return entityDto;
                                }).collect(Collectors.toList());
                        dataList.addAll(viwDataList);

                        // 获取模型下自定义视图组的视图
                        List<FiDataMetaDataTreeDTO> viwGroupDataList = new ArrayList<>();
                        entityVoList.stream().filter(Objects::nonNull)
                                .forEach(item -> {

                                    // 自定义视图组
                                    List<ViwGroupVO> viwGroupList = viwGroupService.getDataByEntityId(item.getId(), null);
                                    if (viwGroupList == null) {
                                        return;
                                    }

                                    viwGroupList.stream().filter(Objects::nonNull)
                                            .forEach(iter -> {

                                                // 自定义视图
                                                String entityUuid = String.valueOf(iter.getId());
                                                FiDataMetaDataTreeDTO entityDto = new FiDataMetaDataTreeDTO();
                                                entityDto.setId(entityUuid);
                                                entityDto.setParentId(modelUuid);
                                                entityDto.setLabel(iter.getName());
                                                entityDto.setLabelAlias(iter.getDetails());
                                                entityDto.setLevelType(LevelTypeEnum.VIEW);
                                                entityDto.setSourceType(1);
                                                entityDto.setSourceId(Integer.parseInt(id));
                                                entityDto.setLabelBusinessType(TableBusinessTypeEnum.NONE.getValue());
                                                viwGroupDataList.add(entityDto);
                                            });
                                });
                        dataList.addAll(viwGroupDataList);
                    }

                    dto.setChildren(dataList);
                    return dto;
                }).collect(Collectors.toList());

        return modelDataList;
    }
}
