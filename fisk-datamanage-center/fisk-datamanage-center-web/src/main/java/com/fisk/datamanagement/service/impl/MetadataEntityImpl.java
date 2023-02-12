package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.metadata.dto.metadata.MetaDataBaseAttributeDTO;
import com.fisk.datamanagement.dto.entity.EntityTreeDTO;
import com.fisk.datamanagement.entity.MetadataEntityPO;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.mapper.MetadataEntityMapper;
import com.fisk.datamanagement.service.IMetadataEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class MetadataEntityImpl
        extends ServiceImpl<MetadataEntityMapper, MetadataEntityPO>
        implements IMetadataEntity {

    private static final String stg = "stg";
    @Resource
    MetadataEntityTypeImpl metadataEntityType;
    @Resource
    MetadataAttributeImpl metadataAttribute;
    @Resource
    MetadataEntityMapper metadataEntityMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer addMetadataEntity(MetaDataBaseAttributeDTO dto, String rdbmsType, String parentEntityId) {
        MetadataEntityPO po = new MetadataEntityPO();
        po.name = dto.name;
        po.description = dto.description;
        po.displayName = dto.displayName;
        po.owner = dto.owner;
        po.typeId = metadataEntityType.getTypeId(rdbmsType);
        po.qualifiedName = dto.qualifiedName;
        //父级
        po.parentId = Integer.parseInt(parentEntityId);

        boolean save = this.save(po);
        if (!save) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        metadataAttribute.addMetadataAttribute(dto, (int) po.id);

        return (int) po.id;
    }

    @Override
    public Integer updateMetadataEntity(MetaDataBaseAttributeDTO dto, Integer entityId, String rdbmsType) {
        MetadataEntityPO po = this.query().eq("id", entityId).one();
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        po.owner = dto.owner;
        po.displayName = dto.displayName;
        po.name = dto.name;
        po.description = dto.description;

        boolean flat = this.updateById(po);
        if (!flat) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        metadataAttribute.operationMetadataAttribute(dto, entityId);

        return (int) po.id;
    }

    public List<EntityTreeDTO> getMetadataEntityTree() {
        List<EntityTreeDTO> list = new ArrayList<>();

        List<MetadataEntityPO> poList = this.query().ne("description", stg).list();
        if (CollectionUtils.isEmpty(poList)) {
            return list;
        }

        List<MetadataEntityPO> parentList = poList.stream().filter(e -> e.parentId == -1).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(parentList)) {
            return list;
        }

        for (MetadataEntityPO item : parentList) {
            EntityTreeDTO dto = new EntityTreeDTO();
            dto.id = String.valueOf(item.id);
            dto.label = item.displayName;
            dto.type = EntityTypeEnum.RDBMS_INSTANCE.getName();
            dto.parentId = "-1";
            dto.displayName = item.displayName;
            list.add(buildChildTree(dto, poList));
        }

        return list;
    }

    public EntityTreeDTO buildChildTree(EntityTreeDTO pNode, List<MetadataEntityPO> poList) {
        List<EntityTreeDTO> list = new ArrayList<>();
        for (MetadataEntityPO item : poList) {
            if (item.getParentId().toString().equals(pNode.getId().toString())) {
                EntityTreeDTO dto = new EntityTreeDTO();
                dto.id = String.valueOf(item.id);
                dto.label = item.name;
                dto.type = EntityTypeEnum.getValue(item.typeId).getName();
                dto.parentId = pNode.id;
                dto.displayName = item.displayName;
                list.add(buildChildTree(dto, poList));
            }
        }
        pNode.children = list;
        return pNode;
    }

    /**
     * 根据限定名获取id
     *
     * @param qualifiedName
     * @return
     */
    public Integer getMetadataEntity(String qualifiedName) {
        MetadataEntityPO one = this.query().eq("qualified_name", qualifiedName).one();

        return one == null ? null : (int) one.id;
    }

    /**
     * 删除元数据
     *
     * @param qualifiedNameList
     * @param parentEntityId
     * @return
     */
    public ResultEnum delMetadataEntity(List<String> qualifiedNameList, String parentEntityId) {
        QueryWrapper<MetadataEntityPO> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .notIn("qualified_name", qualifiedNameList)
                .select("id")
                .lambda()
                .eq(MetadataEntityPO::getParentId, Integer.parseInt(parentEntityId));
        List<String> guidList = (List) metadataEntityMapper.selectObjs(queryWrapper);
        if (CollectionUtils.isEmpty(guidList)) {
            return ResultEnum.SUCCESS;
        }
        int delete = metadataEntityMapper.delete(queryWrapper);
        if (delete == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEnum.SUCCESS;
    }

    public JSONObject getMetadataEntityDetails(String entityId) {

        MetadataEntityPO one = this.query().eq("id", entityId).one();
        if (one == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        Map infoMap = metadataAttribute.setMedataAttribute(Integer.parseInt(entityId));
        infoMap.put("name", one.name);
        infoMap.put("description", one.description);
        infoMap.put("owner", one.owner);
        infoMap.put("qualifiedName", one.qualifiedName);
        infoMap.put("displayName", one.displayName);

        tea(one, infoMap);

        Map map2 = new HashMap();
        map2.put("attributes", infoMap);

        Map map = new HashMap();
        map.put("entity", map2);

        return JSONObject.parseObject(JSONObject.toJSONString(map));

    }

    public Map tea(MetadataEntityPO po, Map map) {

        Map attributeMap = new HashMap();

        EntityTypeEnum value = EntityTypeEnum.getValue(po.typeId);
        switch (value) {
            case RDBMS_INSTANCE:
                map.put("databases", aa((int) po.id, value, "parent_id"));
                break;
            case RDBMS_DB:
                map.put("instance", aa((int) po.id, value, "id"));
                map.put("tables", aa((int) po.id, value, "parent_id"));
                break;
            case RDBMS_TABLE:
                map.put("db", aa((int) po.id, value, "id"));
                map.put("columns", aa((int) po.id, value, "parent_id"));
                break;
            case RDBMS_COLUMN:
                map.put("table", aa((int) po.id, value, "id"));
                break;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);

        }

        return attributeMap;

    }

    public List<Map> aa(Integer entityId, EntityTypeEnum entityTypeEnum, String fileName) {

        List<MetadataEntityPO> list = this.query().ne("description", stg).eq(fileName, entityId).list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<Map> mapList = new ArrayList<>();
        for (MetadataEntityPO item : list) {

            Map infoMap = new HashMap();
            infoMap.put("guid", item.id);
            infoMap.put("typeName", entityTypeEnum.getName());
            mapList.add(infoMap);
        }

        return mapList;

    }


}
