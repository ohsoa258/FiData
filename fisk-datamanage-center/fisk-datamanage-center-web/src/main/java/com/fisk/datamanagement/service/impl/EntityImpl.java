package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.datamanagement.dto.entity.*;
import com.fisk.datamanagement.dto.lineage.LineAgeDTO;
import com.fisk.datamanagement.dto.lineage.LineAgeRelationsDTO;
import com.fisk.datamanagement.dto.metadatalabelmap.MetadataLabelMapParameter;
import com.fisk.datamanagement.dto.metamap.MetaMapDTO;
import com.fisk.datamanagement.dto.metamap.MetaMapTblDTO;
import com.fisk.datamanagement.dto.search.SearchBusinessGlossaryEntityDTO;
import com.fisk.datamanagement.entity.MetadataEntityPO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.mapper.MetadataEntityMapper;
import com.fisk.datamanagement.service.IEntity;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import com.fisk.datamodel.client.DataModelClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class EntityImpl implements IEntity {

    @Resource
    AtlasClient atlasClient;
    @Resource
    UserHelper userHelper;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RedisUtil redisUtil;

    @Resource
    MetadataLabelMapImpl metadataLabelMap;

    @Value("${atlas.entityByGuid}")
    private String entityByGuid;
    @Value("${atlas.entity}")
    private String entity;
    @Value("${atlas.relationship}")
    private String relationship;
    @Value("${spring.metadataentity}")
    private String metaDataEntity;

    @Value("${redisKeyFroAdHocQuery}")
    private String redisKeyForAdHocQuery;

    @Value("${redisKeyForTerm}")
    private String redisKeyForTerm;

    @Resource
    MetadataEntityImpl metadataEntity;

    @Resource
    private DataAccessClient dataAccessClient;

    @Resource
    private DataModelClient dataModelClient;

    @Resource
    MetadataEntityMapper metadataEntityMapper;

    @Override
    public List<EntityTreeDTO> getEntityTreeList() {
        List<EntityTreeDTO> list;
        Boolean exist = redisTemplate.hasKey(metaDataEntity);
        if (exist) {
            String treeList = redisTemplate.opsForValue().get(metaDataEntity).toString();
            list = JSONObject.parseArray(treeList, EntityTreeDTO.class);
            return list;
        }
        list = getEntityList();
        return list;
    }

    /**
     * 获取元数据表节点下的字段  只有表节点才能使用此接口!
     *
     * @param entityId
     * @return
     */
    @Override
    public List<EntityTreeDTO> getEntityListOfTable(Integer entityId) {
        List<MetadataEntityPO> poList = metadataEntityMapper.getColumnMetadataEntities(entityId);
        List<EntityTreeDTO> dtos = new ArrayList<>();
        for (MetadataEntityPO po : poList) {
            EntityTreeDTO dto = new EntityTreeDTO();
            dto.setId(String.valueOf(po.getId()));
            dto.setLabel(po.getName());
            String typeName = EntityTypeEnum.getValue(po.typeId).getName();
            dto.setType(typeName);
            dto.setParentId(String.valueOf(po.getParentId()));
            dto.setDisplayName(po.getDisplayName());
            dto.setQualifiedName(po.getQualifiedName());
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * 为即席查询获取元数据对象树形列表（ods dw mdm）
     *
     * @return
     */
    @Override
    public List<EntityTreeDTO> getEntityListForAdHocQuery() {
        List<EntityTreeDTO> list;
        boolean exist = redisUtil.hasKey(redisKeyForAdHocQuery);
        if (exist) {
            String treeList = Objects.requireNonNull(redisUtil.get(redisKeyForAdHocQuery)).toString();
            list = JSONObject.parseArray(treeList, EntityTreeDTO.class);
            return list;
        }
        List<EntityTreeDTO> metadataEntityTree = metadataEntity.getTreeForAdHocQuery();
        String jsonString = JSONObject.toJSONString(metadataEntityTree);
        redisUtil.set(redisKeyForAdHocQuery, jsonString);
        return metadataEntityTree;
    }

    /**
     * 为业务术语获取元数据对象树形列表
     *
     * @return
     */
    @Override
    public List<EntityTreeDTO> getEntityListForBusinessTerm() {
        List<EntityTreeDTO> list;
        boolean exist = redisUtil.hasKey(redisKeyForTerm);
        if (exist) {
            String treeList = Objects.requireNonNull(redisUtil.get(redisKeyForTerm)).toString();
            list = JSONObject.parseArray(treeList, EntityTreeDTO.class);
            return list;
        }
        List<EntityTreeDTO> metadataEntityTree = metadataEntity.getTreeForBusinessTerm();
        String jsonString = JSONObject.toJSONString(metadataEntityTree);
        redisUtil.set(redisKeyForTerm, jsonString);
        return metadataEntityTree;
    }

    /**
     * 为业务术语获取指定表元数据节点下的字段
     *
     * @param entityId
     * @return
     */
    @Override
    public List<EntityTreeDTO> getEntityColumnsForBusinessTermByEntityId(Integer entityId) {
        return metadataEntity.getEntityColumnsForBusinessTermByEntityId(entityId);
    }

    @Override
    public void refreshEntityTreeList() {
        Boolean exist = redisTemplate.hasKey(metaDataEntity);
        List<EntityTreeDTO> metadataEntityTree = metadataEntity.getMetadataEntityTree();
        String jsonString = JSONObject.toJSONString(metadataEntityTree);
        if (exist) {
            redisTemplate.delete(metaDataEntity);
        }
        redisTemplate.opsForValue().set(metaDataEntity, jsonString);
    }

    /**
     * 刷新即席查询元数据对象树形列表（ods dw mdm olap）
     */
    @Override
    public void refreshEntityTreeForAdHocQuery() {
        boolean exist = redisUtil.hasKey(redisKeyForAdHocQuery);
        List<EntityTreeDTO> metadataEntityTree = metadataEntity.getTreeForAdHocQuery();
        String jsonString = JSONObject.toJSONString(metadataEntityTree);
        if (exist) {
            redisUtil.del(redisKeyForAdHocQuery);
        }
        redisUtil.set(redisKeyForAdHocQuery, jsonString);
    }

    @Override
    public ResultEntity<Object> refreshEntityTreeForTerm() {
        boolean exist = redisUtil.hasKey(redisKeyForTerm);
        List<EntityTreeDTO> metadataEntityTree = metadataEntity.getTreeForBusinessTerm();
        String jsonString = JSONObject.toJSONString(metadataEntityTree);
        if (exist) {
            redisUtil.del(redisKeyForTerm);
        }
        redisUtil.set(redisKeyForTerm, jsonString);
        return ResultEntityBuild.build(ResultEnum.SUCCESS);
    }

    /**
     * 获取元数据对象属性结构
     *
     * @return
     */
    public List<EntityTreeDTO> getEntityList() {
        List<EntityTreeDTO> metadataEntityTree = metadataEntity.getMetadataEntityTree();
        String jsonString = JSONObject.toJSONString(metadataEntityTree);
        redisTemplate.opsForValue().set(metadataEntity, jsonString);
        return metadataEntityTree;
    }

    @Override
    public ResultEnum addEntity(EntityDTO dto) {
        //获取所属人
        dto.entity.attributes.owner = userHelper.getLoginUserInfo().username;
        if (dto.entity.typeName.toLowerCase().equals(EntityTypeEnum.RDBMS_INSTANCE.getName())) {
            dto.entity.attributes.qualifiedName += ":" + dto.entity.attributes.port;
        }
        String jsonParameter = JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.post(entity, jsonParameter);
        updateRedis();
        //添加redis
        if (dto.entity.typeName.toLowerCase().equals(EntityTypeEnum.RDBMS_INSTANCE.getName())
                || dto.entity.typeName.toLowerCase().equals(EntityTypeEnum.RDBMS_DB.getName())) {
            try {
                //解析数据
                JSONObject jsonObj = JSON.parseObject(result.data);
                JSONObject mutatedEntities = JSON.parseObject(jsonObj.getString("mutatedEntities"));
                JSONArray jsonArray = mutatedEntities.getJSONArray("CREATE");
                setRedis(jsonArray.getJSONObject(0).getString("guid"));
                if (dto.entity.typeName.toLowerCase().equals(EntityTypeEnum.RDBMS_DB.getName())) {
                    JSONArray updateAttribute = mutatedEntities.getJSONArray("UPDATE");
                    setRedis(updateAttribute.getJSONObject(0).getString("guid"));
                }
            } catch (Exception e) {
                log.error("parsing data failure:" + e);
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum deleteEntity(String guid) {
        ResultDataDTO<String> result = atlasClient.delete(entityByGuid + "/" + guid);
        updateRedis();
        Boolean exist = redisTemplate.hasKey(metaDataEntity);
        if (exist) {
            redisTemplate.delete("metaDataEntityData:" + guid);
        }
        return atlasClient.newResultEnum(result);
    }

    @Override
    public JSONObject getEntity(String guid) {
        return metadataEntity.getMetadataEntityDetails(guid);
    }

    @Override
    public ResultEnum updateEntity(JSONObject dto) {
        String jsonParameter = JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.post(entity, jsonParameter);
        updateRedis();
        try {
            JSONObject jsonObj = JSON.parseObject(jsonParameter);
            JSONObject entityObject = JSON.parseObject(jsonObj.getString("entity"));
            String guid = entityObject.getString("guid");
            Boolean exist = redisTemplate.hasKey("metaDataEntityData:" + guid);
            if (exist) {
                setRedis(guid);
            }
        } catch (Exception e) {
            log.error("updateEntity ex:", e);
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return atlasClient.newResultEnum(result);
    }

    @Override
    public EntityInstanceDTO getInstanceDetail(String guid) {
        try {
            String[] s = guid.split("_");
            EntityInstanceDTO data = new EntityInstanceDTO();
            data.instanceGuid = s[0];
            return data;
        } catch (Exception e) {
            throw new FkException(ResultEnum.SQL_ANALYSIS);
        }
    }

    /**
     * 根据guid和应用名称获取entity详情
     *
     * @param guid
     * @param appName
     * @return
     */
    @Override
    public JSONObject getEntityV2(String guid, String appName) {
        return metadataEntity.getMetadataEntityDetailsV2(guid, appName);
    }

    /**
     * 根据类型获取获取元数据地图 0数据湖 1数仓
     *
     * @param type 0数据湖 1数仓
     * @return
     */
    @Override
    public List<MetaMapDTO> getMetaMapByType(Integer type) {
        //0数据湖 1数仓
        if (type == 0) {
            return dataAccessClient.accessGetMetaMap();
        } else if (type == 1) {
            return dataModelClient.modelGetMetaMap();
        } else {
            return null;
        }
    }

    /**
     * 元数据地图根据应用id或业务过程id获取表 0数据湖 1数仓
     *
     * @param type  0数据湖 1数仓
     * @param appId 应用id/业务过程id
     * @return
     */
    @Override
    public List<MetaMapTblDTO> getMetaMapTableDetailByType(Integer type, Integer appId, Integer businessType) {
        //0数据湖 1数仓
        if (type == 0) {
            return dataAccessClient.accessGetMetaMapTableDetail(appId);
        } else if (type == 1) {
            return dataModelClient.modelGetMetaMapTableDetail(appId, businessType);
        } else {
            return null;
        }
    }

    /**
     * entity操作后,更新Redis数据
     */
    public void updateRedis() {
        getEntityList();
    }

    @Override
    public SearchBusinessGlossaryEntityDTO searchBasicEntity(EntityFilterDTO dto) {
        return metadataEntity.searchBasicEntity(dto);
    }

    @Override
    public List<EntityAuditsDTO> getAuditsList(String guid) {
        List<EntityAuditsDTO> data;
        ResultDataDTO<String> result = atlasClient.get(entity + "/" + guid + "/audit");
        if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
            throw new FkException(ResultEnum.BAD_REQUEST);
        }
        data = JSONObject.parseArray(result.data, EntityAuditsDTO.class);
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum entityAssociatedLabel(MetadataLabelMapParameter dto) {
        return metadataLabelMap.operationMetadataLabelMap(dto);
    }

    @Override
    public ResultEnum entityAssociatedMetaData(EntityAssociatedMetaDataDTO dto) {
        String jsonParameter = JSONArray.toJSON(dto.businessMetaDataAttribute).toString();
        ResultDataDTO<String> result = atlasClient.post(entityByGuid + "/" + dto.guid + "/businessmetadata?isOverwrite=true", jsonParameter);
        Boolean exist = redisTemplate.hasKey("metaDataEntityData:" + dto.guid);
        if (exist) {
            setRedis(dto.guid);
        }
        return atlasClient.newResultEnum(result);
    }

    @Override
    public LineAgeDTO getMetaDataKinship(String guid) {
        return metadataEntity.getMetaDataKinship(guid);
    }

    public LineAgeDTO getInPutData(String guid,
                                   List<LineAgeRelationsDTO> relationsDtoList,
                                   JSONObject guidEntityMapJson,
                                   String typeName) {
        LineAgeDTO dto = new LineAgeDTO();
        List<LineAgeRelationsDTO> lineAgeRelationsDtoListStream = relationsDtoList.stream()
                .filter(e -> e.toEntityId.equals(guid))
                .collect(Collectors.toList());
        List<JSONObject> jsonArrayList = new ArrayList<>();
        List<LineAgeRelationsDTO> relations = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        boolean flat = true;
        while (flat) {
            ids.clear();
            if (CollectionUtils.isEmpty(lineAgeRelationsDtoListStream)) {
                flat = false;
            }
            for (LineAgeRelationsDTO item : lineAgeRelationsDtoListStream) {
                String jsonObj1String = guidEntityMapJson.getString(item.fromEntityId);
                JSONObject jsonObj2 = JSON.parseObject(jsonObj1String);
                if (!jsonObj2.getString("typeName").equals(typeName)
                        && !"process".equals(jsonObj2.getString("typeName").toLowerCase())) {
                    continue;
                }
                boolean isExist = true;
                //判断process上一级typeName是否一致
                if ("process".equals(jsonObj2.getString("typeName").toLowerCase())) {
                    List<LineAgeRelationsDTO> higherLevelLineAge = relationsDtoList.stream()
                            .filter(e -> e.toEntityId.equals(item.fromEntityId))
                            .collect(Collectors.toList());
                    if (!CollectionUtils.isNotEmpty(higherLevelLineAge)) {
                        continue;
                    }
                    for (LineAgeRelationsDTO lineAge : higherLevelLineAge) {
                        String higher = guidEntityMapJson.getString(lineAge.fromEntityId);
                        JSONObject higherJson = JSON.parseObject(higher);
                        if (!higherJson.getString("typeName").equals(typeName)) {
                            isExist = false;
                        }
                    }
                }
                if (!isExist) {
                    continue;
                }
                //判断relation是否删除
                if (!getRelationShip(item.relationshipId)) {
                    continue;
                }
                String entityDetail1 = guidEntityMapJson.getString(item.fromEntityId);
                JSONObject entityDetailJson1 = JSON.parseObject(entityDetail1);
                //判断实体是否删除
                if (EntityTypeEnum.DELETED.getName().equals(entityDetailJson1.getString("status"))) {
                    continue;
                }
                relations.add(item);
                jsonArrayList.add(entityDetailJson1);
                ids.add(item.fromEntityId);
            }
            lineAgeRelationsDtoListStream.clear();
            if (!CollectionUtils.isNotEmpty(ids)) {
                flat = false;
            }
            lineAgeRelationsDtoListStream = relationsDtoList.stream()
                    .filter(e -> ids.contains(e.toEntityId))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isNotEmpty(lineAgeRelationsDtoListStream)) {
                flat = false;
            }
        }
        dto.guidEntityMap = jsonArrayList;
        dto.relations = relations;
        return dto;
    }

    /**
     * 查询选中实体输出血缘数据
     *
     * @param guid
     * @param relationsDtoList
     * @param guidEntityMapJson
     * @param typeName
     * @return
     */
    public LineAgeDTO getOutPutData(String guid,
                                    List<LineAgeRelationsDTO> relationsDtoList,
                                    JSONObject guidEntityMapJson,
                                    String typeName) {
        LineAgeDTO dto = new LineAgeDTO();
        List<LineAgeRelationsDTO> lineAgeRelationsDtoStream = relationsDtoList.stream()
                .filter(e -> e.fromEntityId.equals(guid))
                .collect(Collectors.toList());
        List<JSONObject> jsonArrayList = new ArrayList<>();
        List<LineAgeRelationsDTO> relations = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        boolean flat = true;
        while (flat) {
            ids.clear();
            if (CollectionUtils.isEmpty(lineAgeRelationsDtoStream)) {
                flat = false;
            }
            for (LineAgeRelationsDTO item : lineAgeRelationsDtoStream) {
                String jsonObj1String = guidEntityMapJson.getString(item.toEntityId);
                JSONObject jsonObj2 = JSON.parseObject(jsonObj1String);
                if (!jsonObj2.getString("typeName").equals(typeName)
                        && !"process".equals(jsonObj2.getString("typeName").toLowerCase())) {
                    continue;
                }
                boolean isExist = true;
                //判断process上一级typeName是否一致
                if ("process".equals(jsonObj2.getString("typeName").toLowerCase())) {
                    List<LineAgeRelationsDTO> higherLevelLineAge = relationsDtoList.stream()
                            .filter(e -> e.fromEntityId.equals(item.toEntityId))
                            .collect(Collectors.toList());
                    if (!CollectionUtils.isNotEmpty(higherLevelLineAge)) {
                        continue;
                    }
                    for (LineAgeRelationsDTO lineAge : higherLevelLineAge) {
                        String higher = guidEntityMapJson.getString(lineAge.toEntityId);
                        JSONObject higherJson = JSON.parseObject(higher);
                        if (!higherJson.getString("typeName").equals(typeName)) {
                            isExist = false;
                        }
                    }
                }
                if (!isExist) {
                    continue;
                }
                //判断relation是否删除
                if (!getRelationShip(item.relationshipId)) {
                    continue;
                }
                String entityDetail1 = guidEntityMapJson.getString(item.toEntityId);
                JSONObject entityDetailJson1 = JSON.parseObject(entityDetail1);
                //判断实体是否删除
                if (EntityTypeEnum.DELETED.getName().equals(entityDetailJson1.getString("status"))) {
                    continue;
                }
                relations.add(item);
                jsonArrayList.add(entityDetailJson1);
                ids.add(item.toEntityId);
            }
            lineAgeRelationsDtoStream.clear();
            if (!CollectionUtils.isNotEmpty(ids)) {
                flat = false;
            }
            lineAgeRelationsDtoStream = relationsDtoList.stream()
                    .filter(e -> ids.contains(e.fromEntityId))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isNotEmpty(lineAgeRelationsDtoStream)) {
                flat = false;
            }
        }
        dto.guidEntityMap = jsonArrayList;
        dto.relations = relations;
        return dto;
    }

    /**
     * 判断血缘关系是否删除
     *
     * @param guid
     * @return
     */
    public boolean getRelationShip(String guid) {
        try {
            ResultDataDTO<String> result = atlasClient.get(relationship + "/guid/" + guid);
            if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
                return false;
            }
            //解析数据
            JSONObject jsonObj = JSON.parseObject(result.data);
            //判断是否存在血缘关系
            String relationshipStr = jsonObj.getString("relationship");
            JSONObject relationshipJson = JSON.parseObject(relationshipStr);
            if (!relationshipJson.getString("status").equals(EntityTypeEnum.DELETED.getName())) {
                return true;
            }
        } catch (Exception e) {
            log.error("getRelationShip ex:", e);
        }
        return false;
    }

    public void setRedis(String guid) {
        ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + guid);
        if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return;
        }
        redisTemplate.opsForValue().set("metaDataEntityData:" + guid, getDetail.data);
    }

}
