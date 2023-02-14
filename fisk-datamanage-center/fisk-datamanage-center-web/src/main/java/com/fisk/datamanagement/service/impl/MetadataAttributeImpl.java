package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.ObjectInfoUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.metadataattribute.MetadataAttributeDTO;
import com.fisk.datamanagement.entity.MetadataAttributePO;
import com.fisk.datamanagement.map.MetadataAttributeMap;
import com.fisk.datamanagement.mapper.MetadataAttributeMapper;
import com.fisk.datamanagement.service.IMetadataAttribute;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author JianWenYang
 */
@Service
public class MetadataAttributeImpl
        extends ServiceImpl<MetadataAttributeMapper, MetadataAttributePO>
        implements IMetadataAttribute {

    @Resource
    MetadataAttributeMapper mapper;

    /**
     * 技术属性
     */
    private Integer technologyAttribute = 0;
    /**
     * 自定义属性
     */
    private Integer customAttribute = 1;

    /**
     * 操作元数据属性
     *
     * @param object
     * @param entityId
     * @return
     */
    public ResultEnum operationMetadataAttribute(Object object, Integer entityId) {
        delMetadataAttribute(entityId, technologyAttribute);

        addMetadataAttribute(object, entityId);

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum addMetadataAttribute(Object object, Integer entityId) {

        String[] fieldNames = ObjectInfoUtils.getFiledName(object);

        Map<String, Object> map = new HashMap<>();
        for (String item : fieldNames) {
            if (StringUtils.isEmpty(item)) {
                continue;
            }
            Object value = ObjectInfoUtils.getFieldValueByName(item, object);
            map.put(item, value);
        }

        List<MetadataAttributePO> dataList = new ArrayList<>();

        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            MetadataAttributePO po = new MetadataAttributePO();
            po.metadataEntityId = entityId;
            String key = iterator.next();
            po.name = key;
            po.value = map.get(key).toString();
            po.groupType = 0;

            dataList.add(po);
        }

        boolean flat = this.saveBatch(dataList);
        if (!flat) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum delMetadataAttribute(Integer entityId, Integer group) {
        QueryWrapper<MetadataAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(MetadataAttributePO::getMetadataEntityId, entityId)
                .eq(MetadataAttributePO::getGroupType, group);

        List<MetadataAttributePO> metadataAttributePOS = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(metadataAttributePOS)) {
            return ResultEnum.SUCCESS;
        }

        boolean remove = this.remove(queryWrapper);
        if (!remove) {
            throw new FkException(ResultEnum.DATA_SUBMIT_ERROR);
        }

        return ResultEnum.SUCCESS;

    }

    public Map setMedataAttribute(Integer metadataEntityId, Integer group) {
        Map map = new HashMap();

        List<MetadataAttributePO> list = this.query()
                .select("name", "value")
                .eq("metadata_entity_id", metadataEntityId)
                .eq("group_type", group)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return map;
        }

        for (MetadataAttributePO item : list) {
            map.put(item.name, item.value);
        }

        return map;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum metadataCustomAttribute(List<MetadataAttributeDTO> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return ResultEnum.SUCCESS;
        }

        delMetadataAttribute(dtoList.get(0).metadataEntityId, customAttribute);

        dtoList.stream().map(e -> {
            e.groupType = customAttribute;
            return e;
        });

        List<MetadataAttributePO> metadataAttributePOS = MetadataAttributeMap.INSTANCES.dtoListToPoList(dtoList);
        boolean flat = this.saveBatch(metadataAttributePOS);
        if (!flat) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEnum.SUCCESS;
    }

}
