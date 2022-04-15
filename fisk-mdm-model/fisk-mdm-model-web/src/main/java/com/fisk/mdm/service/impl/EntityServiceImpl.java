package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.dto.entity.EntityDTO;
import com.fisk.mdm.dto.entity.EntityPageDTO;
import com.fisk.mdm.dto.entity.UpdateEntityDTO;
import com.fisk.mdm.dto.eventlog.EventLogDTO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.enums.*;
import com.fisk.mdm.map.EntityMap;
import com.fisk.mdm.mapper.EntityMapper;
import com.fisk.mdm.service.AttributeService;
import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.service.EventLogService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
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


    @Override
    public EntityDTO getDataById(Integer id) {
        EntityPO entityPo = entityMapper.selectById(id);
        return entityPo == null ? null : EntityMap.INSTANCES.poToDto(entityPo);
    }

    @Override
    public Page<EntityDTO> listData(EntityPageDTO dto) {

        // page转换
        Page<EntityPO> poPage = EntityMap.INSTANCES.dtoToPoPage(dto.getPage());

        QueryWrapper<EntityPO> query = new QueryWrapper<>();
        query.lambda()
                .orderByDesc(EntityPO::getCreateTime);

        String name = dto.getName();
        if (StringUtils.isNotBlank(name)) {
            query.lambda()
                    .like(EntityPO::getName, name);
            return EntityMap.INSTANCES.poToDtoPage(entityMapper.selectPage(poPage, query));
        }

        return EntityMap.INSTANCES.poToDtoPage(entityMapper.selectPage(poPage, query));
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
        ResultEnum resultEnum = logService.saveEventLog(id,ObjectTypeEnum.ENTITY,EventTypeEnum.DELETE,desc);
        if (resultEnum == ResultEnum.SAVE_DATA_ERROR){
            return ResultEnum.SAVE_DATA_ERROR;
        }

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
        ResultEnum resultEnum = logService.saveEventLog(id,ObjectTypeEnum.ENTITY,EventTypeEnum.DELETE,desc);
        if (resultEnum == ResultEnum.SAVE_DATA_ERROR){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        return ResultEnum.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum saveEntity(EntityDTO dto) {
        QueryWrapper<EntityPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
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
        attributeCode.setDisplayName("字典编码");
        attributeCode.setDataType(DataTypeEnum.TEXT);
        attributeCode.setDataTypeLength(50);
        attributePoList.add(attributeCode);

        AttributePO attributePoName = new AttributePO();
        attributePoName.setEntityId(entityId);
        attributePoName.setName(MdmTypeEnum.NAME.getName());
        attributePoName.setDisplayName("字典名称");
        attributePoName.setDataType(DataTypeEnum.TEXT);
        attributePoName.setDataTypeLength(50);
        attributePoList.add(attributePoName);

        boolean saveBatch = attributeService.saveBatch(attributePoList);
        if (saveBatch == false){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        String desc = "创建了一个实体,id:" + entityId;

        // 记录日志
        ResultEnum resultEnum = logService.saveEventLog((int)entityPo.getId(),ObjectTypeEnum.ENTITY,
                EventTypeEnum.SAVE,desc);
        if (resultEnum == ResultEnum.SAVE_DATA_ERROR){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        return ResultEnum.SUCCESS;
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
}
