package com.fisk.mdm.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.model.ModelDTO;
import com.fisk.mdm.dto.model.ModelQueryDTO;
import com.fisk.mdm.dto.model.ModelUpdateDTO;
import com.fisk.mdm.dto.modelVersion.ModelVersionDTO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.entity.ModelPO;
import com.fisk.mdm.entity.ModelVersionPO;
import com.fisk.mdm.enums.*;
import com.fisk.mdm.map.EntityMap;
import com.fisk.mdm.map.ModelMap;
import com.fisk.mdm.map.ModelVersionMap;
import com.fisk.mdm.mapper.EntityMapper;
import com.fisk.mdm.mapper.ModelMapper;
import com.fisk.mdm.service.*;
import com.fisk.mdm.utils.mdmBEBuild.TableNameGenerateUtils;
import com.fisk.mdm.vo.entity.EntityVO;
import com.fisk.mdm.vo.model.ModelInfoVO;
import com.fisk.mdm.vo.model.ModelVO;
import com.fisk.mdm.vo.viwGroup.ViwGroupVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.relenish.ReplenishUserInfo;
import com.fisk.system.relenish.UserFieldEnum;
import com.fisk.task.client.PublishTaskClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

        //添加成功
        return ResultEnum.SUCCESS;
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
        if (baseMapper.selectById(id) == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        //删除数据
        if (baseMapper.deleteById(id) <= 0) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 记录日志
        String desc = "删除一个模型,id:" + id;
        logService.saveEventLog(id, ObjectTypeEnum.MODEL, EventTypeEnum.DELETE, desc);

        //删除成功
        return ResultEnum.SUCCESS;
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
                                    String viwName = TableNameGenerateUtils.generateViwTableName((int) e.getId(), item.getId());
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
