package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.dto.entity.EntityDTO;
import com.fisk.mdm.dto.entity.UpdateEntityDTO;
import com.fisk.mdm.dto.eventlog.EventLogDTO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.enums.DataTypeEnum;
import com.fisk.mdm.enums.EventTypeEnum;
import com.fisk.mdm.enums.MdmTypeEnum;
import com.fisk.mdm.enums.ObjectTypeEnum;
import com.fisk.mdm.map.EntityMap;
import com.fisk.mdm.mapper.EntityMapper;
import com.fisk.mdm.service.AttributeService;
import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.service.EventLogService;
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


    @Override
    public EntityDTO getDataById(Integer id) {
        EntityPO entityPo = entityMapper.selectById(id);
        return entityPo == null ? null : EntityMap.INSTANCES.poToDto(entityPo);
    }

    @Override
    public Page<EntityDTO> listData(Page<EntityPO> page,String name) {
        QueryWrapper<EntityPO> query = new QueryWrapper<>();
        query.lambda()
                .orderByDesc(EntityPO::getCreateTime);

        if (StringUtils.isNotBlank(name)) {
            query.lambda()
                    .like(EntityPO::getName, name);
            return EntityMap.INSTANCES.poToDtoPage(entityMapper.selectPage(page, query));
        }

        return EntityMap.INSTANCES.poToDtoPage(entityMapper.selectPage(page, query));
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

        EventLogDTO eventLog = new EventLogDTO();
        eventLog.setObjectId(id);
        eventLog.setObjectType(ObjectTypeEnum.ENTITY);
        eventLog.setEventType(EventTypeEnum.DELETE);
        eventLog.setDesc("修改一个实体,id:" + id);

        // 记录日志
        ResultEnum resultEnum = logService.saveEventLog(eventLog);
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

        EventLogDTO eventLog = new EventLogDTO();
        eventLog.setObjectId(id);
        eventLog.setObjectType(ObjectTypeEnum.ENTITY);
        eventLog.setEventType(EventTypeEnum.DELETE);
        eventLog.setDesc("删除了一个实体,id:" + id);

        // 记录日志
        ResultEnum resultEnum = logService.saveEventLog(eventLog);
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
        int insert = entityMapper.insert(entityPo);
        if (insert <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 保存属性信息
        int entityId = (int)entityPo.getId();
        List<AttributePO> attributePoList = new ArrayList<>();
        AttributePO attributePo = new AttributePO();
        attributePo.setEntityId(entityId);
        attributePo.setName(MdmTypeEnum.CODE.getName());
        attributePo.setDisplayName("字典编码");
        attributePo.setDataType(DataTypeEnum.TEXT);
        attributePo.setDataTypeLength(50);
        attributePoList.add(attributePo);

        AttributePO attributePo1 = new AttributePO();
        attributePo1.setEntityId(entityId);
        attributePo1.setName(MdmTypeEnum.NAME.getName());
        attributePo1.setDisplayName("字典名称");
        attributePo1.setDataType(DataTypeEnum.TEXT);
        attributePo1.setDataTypeLength(50);
        attributePoList.add(attributePo1);

        boolean saveBatch = attributeService.saveBatch(attributePoList);
        if (saveBatch == false){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        EventLogDTO eventLog = new EventLogDTO();
        eventLog.setObjectId((int)entityPo.getId());
        eventLog.setObjectType(ObjectTypeEnum.ENTITY);
        eventLog.setEventType(EventTypeEnum.SAVE);
        eventLog.setDesc("创建了一个实体,id:" + entityId);

        // 记录日志
        ResultEnum resultEnum = logService.saveEventLog(eventLog);
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
