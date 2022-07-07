package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.attributeGroup.AttributeGroupDTO;
import com.fisk.mdm.dto.entity.EntityDTO;
import com.fisk.mdm.dto.entity.EntityPageDTO;
import com.fisk.mdm.dto.entity.UpdateEntityDTO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.enums.*;
import com.fisk.mdm.map.AttributeMap;
import com.fisk.mdm.map.EntityMap;
import com.fisk.mdm.mapper.AttributeMapper;
import com.fisk.mdm.mapper.EntityMapper;
import com.fisk.mdm.service.AttributeGroupService;
import com.fisk.mdm.service.AttributeService;
import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.service.EventLogService;
import com.fisk.mdm.vo.entity.EntityDropDownVO;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import com.fisk.mdm.vo.entity.EntityVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
import com.fisk.system.relenish.ReplenishUserInfo;
import com.fisk.system.relenish.UserFieldEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author WangYan
 * @date 2022/4/2 17:49
 */
@Service
public class EntityServiceImpl implements EntityService {

    @Resource
    EntityMapper entityMapper;
    @Resource
    AttributeService attributeService;
    @Resource
    EventLogService logService;
    @Resource
    UserClient userClient;
    @Resource
    AttributeMapper attributeMapper;
    @Resource
    AttributeGroupService groupService;

    @Override
    public EntityVO getDataById(Integer id) {
        EntityPO entityPo = entityMapper.selectById(id);
        return entityPo == null ? null : EntityMap.INSTANCES.poToVo(entityPo);
    }

    @Override
    public Page<EntityVO> listData(EntityPageDTO dto) {

        // page转换
        Page<EntityPO> poPage = EntityMap.INSTANCES.voToPoPage(dto.getPage());

        QueryWrapper<EntityPO> query = new QueryWrapper<>();
        query.lambda()
                .orderByDesc(EntityPO::getCreateTime);

        String name = dto.getName();
        if (StringUtils.isNotBlank(name)) {
            query.lambda()
                    .like(EntityPO::getName, name)
                    .or()
                    .like(EntityPO::getDisplayName,name)
                    .or()
                    .like(EntityPO::getDesc,name);
            Page<EntityPO> entityPoPage = entityMapper.selectPage(poPage, query);

            // 查创建人信息
            Page<EntityPO> queryCreateUser = this.queryCreateUser(entityPoPage);
            return EntityMap.INSTANCES.poToVoPage(this.queryUpdateUser(queryCreateUser));
        }

        Page<EntityPO> entityPoPage = entityMapper.selectPage(poPage, query);

        // 查创建人信息
        Page<EntityPO> queryCreateUser = this.queryCreateUser(entityPoPage);
        return EntityMap.INSTANCES.poToVoPage(this.queryUpdateUser(queryCreateUser));
    }

    /**
     * 查询创建人信息
     * @param entityPoPage
     * @return
     */
    public Page<EntityPO> queryCreateUser(Page<EntityPO> entityPoPage){
        List<EntityPO> records = entityPoPage.getRecords();
        if (CollectionUtils.isNotEmpty(records)){
            List<Long> userIds = records.stream().filter(e -> StringUtils.isNotBlank(e.getCreateUser())).map(e -> Long.parseLong(e.getCreateUser())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(userIds)){
                // 查询用户名
                List<UserDTO> dtoList = userClient.getUserListByIds(userIds).getData();

                List<EntityPO> entityPoList = new ArrayList<>();
                for (EntityPO record : records) {
                    List<EntityPO> collect = dtoList.stream().filter(item -> item.getId().toString().equals(record.getCreateUser())).map(item -> {
                        record.setCreateUser(item.getUserAccount());
                        return record;
                    }).collect(Collectors.toList());

                    entityPoList.addAll(collect);
                }

                return entityPoPage.setRecords(entityPoList);
            }
        }

        return null;
    }

    /**
     * 查询更新人信息
     * @param entityPoPage
     * @return
     */
    public Page<EntityPO> queryUpdateUser(Page<EntityPO> entityPoPage){
        if (entityPoPage != null){
            List<EntityPO> records = entityPoPage.getRecords();
            if (CollectionUtils.isNotEmpty(records)){

                List<EntityPO> pagePoList = new ArrayList<>();

                // 没有更新人
                List<EntityPO> notUpdateUserList = records.stream().filter(e -> StringUtils.isBlank(e.getUpdateUser())).collect(Collectors.toList());
                pagePoList.addAll(notUpdateUserList);

                List<Long> userIds = records.stream().filter(e -> StringUtils.isNotBlank(e.getUpdateUser())).map(e -> Long.parseLong(e.getUpdateUser())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(userIds)){
                    // 查询用户名
                    List<UserDTO> dtoList = userClient.getUserListByIds(userIds).getData();

                    List<EntityPO> entityPoList = new ArrayList<>();
                    for (EntityPO record : records) {
                        List<EntityPO> collect = dtoList.stream().filter(item -> item.getId().toString().equals(record.getUpdateUser())).map(item -> {
                            record.setUpdateUser(item.getUserAccount());
                            return record;
                        }).collect(Collectors.toList());

                        entityPoList.addAll(collect);
                    }

                    pagePoList.addAll(entityPoList);
                }

                return entityPoPage.setRecords(pagePoList);
            }
        }

        return entityPoPage;
    }

    @Override
    public ResultEnum updateData(UpdateEntityDTO dto) {
        @NotNull Integer id = dto.getId();
        boolean entity = this.isExistEntity(id);
        if (entity == false){
            return ResultEnum.DATA_NOTEXISTS;
        }

        int res = entityMapper.updateById(EntityMap.INSTANCES.updateDtoToPo(dto));
        if (res <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        String desc = "修改一个实体,id:" + id;

        // 记录日志
        logService.saveEventLog(id,ObjectTypeEnum.ENTITY,EventTypeEnum.DELETE,desc);

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteData(Integer id) {
        boolean entity = this.isExistEntity(id);
        if (entity == false){
            return ResultEnum.DATA_NOTEXISTS;
        }

        int res = entityMapper.deleteById(id);
        if (res <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 删除实体下的属性
        this.deleteAttrByEntityId(id);

        String desc = "删除了一个实体,id:" + id;

        // 记录日志
        logService.saveEventLog(id,ObjectTypeEnum.ENTITY,EventTypeEnum.DELETE,desc);

        return ResultEnum.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum saveEntity(EntityDTO dto) {
        QueryWrapper<EntityPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(EntityPO::getModelId,dto.getModelId())
                .eq(EntityPO::getName,dto.getName())
                .last("limit 1");

        EntityPO po = entityMapper.selectOne(queryWrapper);
        if (po != null){
            return ResultEnum.DATA_EXISTS;
        }

        // 保存实体信息
        EntityPO entityPo = EntityMap.INSTANCES.DtoToPo(dto);
        entityPo.setStatus(MdmStatusTypeEnum.NOT_CREATED);
        int insert = entityMapper.insert(entityPo);
        if (insert <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 保存属性信息
        int entityId = (int)entityPo.getId();
        List<AttributePO> attributePoList = new ArrayList<>();
        AttributePO attributeCode = new AttributePO();
        attributeCode.setEntityId(entityId);
        attributeCode.setName(MdmTypeEnum.CODE.getName());
        attributeCode.setDisplayName("编码");
        attributeCode.setDataType(DataTypeEnum.TEXT);
        attributeCode.setDataTypeLength(50);
        attributeCode.setEnableAttributeLog(0);
        attributeCode.setEnableReadonly(0);
        attributeCode.setSortWieght(1);
        attributeCode.setEnableRequired(1);
        attributeCode.setDisplayWidth(120);
        attributeCode.setStatus(AttributeStatusEnum.INSERT);
        attributeCode.setSyncStatus(AttributeSyncStatusEnum.NOT_PUBLISH);
        attributePoList.add(attributeCode);

        AttributePO attributePoName = new AttributePO();
        attributePoName.setEntityId(entityId);
        attributePoName.setName(MdmTypeEnum.NAME.getName());
        attributePoName.setDisplayName("名称");
        attributePoName.setDataType(DataTypeEnum.TEXT);
        attributePoName.setDataTypeLength(50);
        attributePoName.setEnableAttributeLog(0);
        attributePoName.setEnableReadonly(0);
        attributePoName.setSortWieght(2);
        attributePoName.setEnableRequired(1);
        attributePoName.setDisplayWidth(120);
        attributePoName.setStatus(AttributeStatusEnum.INSERT);
        attributePoName.setSyncStatus(AttributeSyncStatusEnum.NOT_PUBLISH);
        attributePoList.add(attributePoName);

        boolean saveBatch = attributeService.saveBatch(attributePoList);
        if (saveBatch == false){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        String desc = "创建了一个实体,id:" + entityId;

        // 记录日志
        logService.saveEventLog((int)entityPo.getId(),ObjectTypeEnum.ENTITY,
                EventTypeEnum.SAVE,desc);

        return ResultEnum.SUCCESS;
    }

    @Override
    public EntityInfoVO getAttributeById(Integer id,String name) {
        EntityPO entityPo = entityMapper.selectById(id);
        if (entityPo == null){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // 实体信息
        EntityInfoVO entityInfoVo = AttributeMap.INSTANCES.poToEntityVo(entityPo);

        QueryWrapper<AttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AttributePO::getEntityId,id)
                .like(AttributePO::getName, name)
                .or()
                .like(AttributePO::getDisplayName,name)
                .or()
                .like(AttributePO::getDesc,name)
                .orderByAsc(AttributePO::getSortWieght);
        List<AttributePO> attributePoList = attributeMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(attributePoList)){
            List<AttributeInfoDTO> dtoList = AttributeMap.INSTANCES.poToDtoList(attributePoList).stream().filter(Objects::nonNull)
                    .map(e -> {
                        e.setModelId(entityPo.getModelId());
                        return e;
                    }).collect(Collectors.toList());

            // 获取属性组
            dtoList.stream().filter(e -> e.getId() != null).forEach(e -> {
                List<AttributeGroupDTO> attributeGroupList = groupService.getDataByAttributeId(e.getId());
                e.setAttributeGroupList(attributeGroupList);
            });

            // 获取创建人、修改人
            ReplenishUserInfo.replenishUserName(dtoList, userClient, UserFieldEnum.USER_ACCOUNT);

            //若属性类型为域字段，需返回关联实体名称
            for (AttributeInfoDTO attributeInfoDTO:dtoList){
                //判断类型是否为域字段，并且域字段id是否为空
                if( DataTypeEnum.DOMAIN.getName().equals(attributeInfoDTO.getDataType()) &&
                        !Objects.isNull(attributeInfoDTO.getDomainId())){
                    //查询所关联实体的code属性
                    AttributePO codeAttribute = attributeMapper.selectById(attributeInfoDTO.getDomainId());
                    if(codeAttribute != null){
                        //根据code属性的entity_id查询到实体
                        EntityPO domainEntityPo = entityMapper.selectById(codeAttribute.getEntityId());
                        if (domainEntityPo != null) {
                            //为”关联实体名“赋值
                            attributeInfoDTO.setDomainEntityId((int) domainEntityPo.getId());
                            //为”关联实体名“赋值
                            attributeInfoDTO.setDomainName(domainEntityPo.getName());
                        }
                    }
                }
            }
            dtoList.stream().map(e -> {
                e.setDataTypeEnDisplay(DataTypeEnum.getValue(e.getDataType()).name());
                return e;
            }).collect(Collectors.toList());
            entityInfoVo.setAttributeList(dtoList);
            return entityInfoVo;
        }

        return entityInfoVo;
    }

    /**
     * 获取可关联（同模型下 除本身外 创建后台表成功）的实体
     *
     * @return {@link List}<{@link EntityVO}>
     */
    @Override
    public ResultEntity<List<EntityVO>> getCreateSuccessEntity(Integer modelId,Integer entityId) {
        QueryWrapper<EntityPO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(EntityPO::getStatus,MdmStatusTypeEnum.CREATED_SUCCESSFULLY)
                .ne(EntityPO::getId,entityId)
                .eq(EntityPO::getModelId,modelId);
        List<EntityPO> entityPoS = entityMapper.selectList(wrapper);
        return entityPoS.size() == 0 ? ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS) :
                ResultEntityBuild.build(ResultEnum.SUCCESS,EntityMap.INSTANCES.poToVoList(entityPoS));
    }

    /**
     * 判断实体数据是否存在
     * @param id
     * @return
     */
    public boolean isExistEntity(Integer id){
        EntityPO entityPo = entityMapper.selectById(id);
        if (entityPo == null){
            return false;
        }

        return true;
    }

    /**
     * 删除实体下的属性
     * @param entityId
     */
    public void deleteAttrByEntityId(Integer entityId){
        QueryWrapper<AttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AttributePO::getEntityId,entityId);
        List<AttributePO> list = attributeService.list(queryWrapper);
        if(CollectionUtils.isNotEmpty(list)){
            List<Long> ids = list.stream().filter(Objects::nonNull).map(e -> {
                return e.getId();
            }).collect(Collectors.toList());
            boolean res = attributeService.removeByIds(ids);
            if (res == false){
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }
    }

    /**
     * 获取实体下拉列表
     * @param modelId
     * @return
     */
    public List<EntityDropDownVO> getEntityDropDown(int modelId) {
        QueryWrapper<EntityPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time")
                .lambda()
                .eq(EntityPO::getModelId, modelId)
                .eq(EntityPO::getStatus, MdmStatusTypeEnum.CREATED_SUCCESSFULLY);
        List<EntityPO> list = entityMapper.selectList(queryWrapper);
        return EntityMap.INSTANCES.poListToDropDownVoList(list);
    }

    /**
     * 实体是否开启成员日志
     *
     * @param entityId
     * @return
     */
    public boolean getEnableMemberLog(Integer entityId) {
        EntityPO entityPo = entityMapper.selectById(entityId);
        if (entityPo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return entityPo.getEnableMemberLog() == 0 ? false : true;
    }

}
