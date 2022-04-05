package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.entity.EntityDTO;
import com.fisk.mdm.dto.entity.UpdateEntityDTO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.enums.MdmTypeEnum;
import com.fisk.mdm.map.EntityMap;
import com.fisk.mdm.mapper.EntityMapper;
import com.fisk.mdm.service.AttributeService;
import com.fisk.mdm.service.EntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
        boolean entity = this.isExistEntity(dto.getId());
        if (entity == false){
            return ResultEnum.DATA_NOTEXISTS;
        }

        int res = entityMapper.updateById(EntityMap.INSTANCES.updateDtoToPo(dto));
        return res <= 0 ? ResultEnum.SAVE_DATA_ERROR : ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteData(Integer id) {
        boolean entity = this.isExistEntity(id);
        if (entity == false){
            return ResultEnum.DATA_NOTEXISTS;
        }

        int res = entityMapper.deleteById(id);
        return res <= 0 ? ResultEnum.SAVE_DATA_ERROR : ResultEnum.SUCCESS;
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
        List<AttributePO> attributePoList = new ArrayList<>();
        AttributePO attributePo = new AttributePO();
        attributePo.setEntityId((int)entityPo.getId());
        attributePo.setName(MdmTypeEnum.CODE.getName());
        attributePo.setDisplayName("字典编码");
        attributePoList.add(attributePo);

        AttributePO attributePo1 = new AttributePO();
        attributePo1.setEntityId((int)entityPo.getId());
        attributePo1.setName(MdmTypeEnum.NAME.getName());
        attributePo1.setDisplayName("字典名称");
        attributePoList.add(attributePo1);

        boolean saveBatch = attributeService.saveBatch(attributePoList);
        if (saveBatch == false){
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
}
