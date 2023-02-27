package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.datamanagement.dto.entity.*;
import com.fisk.datamanagement.dto.lineage.LineAgeDTO;
import com.fisk.datamanagement.dto.lineage.LineAgeRelationsDTO;
import com.fisk.datamanagement.dto.search.SearchBusinessGlossaryEntityDTO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.mapper.*;
import com.fisk.datamanagement.service.IEntity;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.system.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
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
    DataAccessClient dataAccessClient;
    @Resource
    DataModelClient dataModelClient;
    @Resource
    UserClient userClient;
    @Resource
    BusinessMetadataConfigImpl businessMetadataConfigImpl;
    @Resource
    MetadataEntityMapper metadataEntityMapper;
    @Resource
    MetaDataGlossaryMapMapper metaDataGlossaryMapMapper;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    GlossaryMapper glossaryMapper;
    @Resource
    MetadataEntityTypeMapper metadataEntityTypeMapper;
    @Resource
    MetadataAttributeMapper metadataAttributeMapper;
    @Resource
    MetaDataClassificationMapMapper metaDataClassificationMapMapper;
    @Resource
    BusinessClassificationMapper businessClassificationMapper;

    @Value("${atlas.searchBasic}")
    private String searchBasic;
    @Value("${atlas.entityByGuid}")
    private String entityByGuid;
    @Value("${atlas.entity}")
    private String entity;
    @Value("${atlas.lineage}")
    private String lineage;
    @Value("${atlas.relationship}")
    private String relationship;
    @Value("${spring.metadataentity}")
    private String metaDataEntity;

    @Resource
    MetadataEntityImpl metadataEntity;

    @Override
    public List<EntityTreeDTO> getEntityTreeList() {
        List<EntityTreeDTO> list;
        /*Boolean exist = redisTemplate.hasKey(metaDataEntity);
        if (exist) {
            String treeList = redisTemplate.opsForValue().get(metaDataEntity).toString();
            list = JSONObject.parseArray(treeList, EntityTreeDTO.class);
            return list;
        }*/
        list = getEntityList();
        return list;
    }

    /**
     * 获取元数据对象属性结构
     *
     * @return
     */
    public List<EntityTreeDTO> getEntityList() {

        List<EntityTreeDTO> metadataEntityTree = metadataEntity.getMetadataEntityTree();
        /*String jsonString = JSONObject.toJSONString(metadataEntityTree);
        redisTemplate.opsForValue().set(metaDataEntity, jsonString);*/
        return metadataEntityTree;
        /*//try {
        ResultDataDTO<String> data = atlasClient.get(searchBasic + "?typeName=rdbms_instance");
        if (data.code != AtlasResultEnum.REQUEST_SUCCESS) {
            throw new FkException(ResultEnum.BAD_REQUEST);
        }
        JSONObject jsonObj = JSON.parseObject(data.data);
        JSONArray array = jsonObj.getJSONArray("entities");
        //获取接入应用列表
        ResultEntity<List<AppBusinessInfoDTO>> appList = dataAccessClient.getAppList();
        //获取建模业务域列表
        ResultEntity<List<AppBusinessInfoDTO>> businessAreaList = dataModelClient.getBusinessAreaList();
        //获取数据源列表
        ResultEntity<List<DataSourceDTO>> allFiDataDataSource = userClient.getAllFiDataDataSource();
        if (appList.code != ResultEnum.SUCCESS.getCode()
                || businessAreaList.code != ResultEnum.SUCCESS.getCode()
                || allFiDataDataSource.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        for (int i = 0; i < array.size(); i++) {
            try {
                if (EntityTypeEnum.DELETED.getName().equals(array.getJSONObject(i).getString("status"))) {
                    continue;
                }
                //判断是否stg临时表
                String attributes1 = array.getJSONObject(i).getString("attributes");
                JSONObject jsonObject = JSONObject.parseObject(attributes1);
                if ("stg".equals(jsonObject.getString("description"))) {
                    continue;
                }

                //获取实例数据
                EntityTreeDTO entityParentDTO = new EntityTreeDTO();
                entityParentDTO.id = array.getJSONObject(i).getString("guid");
                entityParentDTO.label = array.getJSONObject(i).getString("displayText");
                entityParentDTO.type = EntityTypeEnum.RDBMS_INSTANCE.getName();
                entityParentDTO.parentId = "-1";
                entityParentDTO.displayName = array.getJSONObject(i).getString("displayText");
                //查询实例下db/table/column
                ResultDataDTO<String> attribute = atlasClient.get(entityByGuid + "/" + entityParentDTO.id);
                if (attribute.code != AtlasResultEnum.REQUEST_SUCCESS) {
                    throw new FkException(ResultEnum.BAD_REQUEST);
                }
                JSONObject jsonObj1 = JSON.parseObject(attribute.data);
                //获取referredEntities
                String referredEntities = jsonObj1.getString("referredEntities");
                JSONObject guidEntityMap = JSON.parseObject(referredEntities);
                Iterator sIterator = guidEntityMap.keySet().iterator();
                List<EntityStagingDTO> stagingDTOList = new ArrayList<>();
                //迭代器获取实例下key值
                while (sIterator.hasNext()) {
                    try {
                        String key = sIterator.next().toString();
                        String value = guidEntityMap.getString(key);
                        JSONObject jsonValue = JSON.parseObject(value);
                        //过滤已删除数据
                        if (EntityTypeEnum.DELETED.getName().equals(jsonValue.getString("status"))) {
                            continue;
                        }
                        //根据key获取json指定数据
                        String typeName = jsonValue.getString("typeName");
                        EntityStagingDTO childEntityDTO = new EntityStagingDTO();
                        childEntityDTO.guid = jsonValue.getString("guid");
                        String attributes = jsonValue.getString("attributes");
                        JSONObject names = JSON.parseObject(attributes);

                        if ("stg".equals(names.getString("description"))) {
                            continue;
                        }

                        childEntityDTO.name = names.getString("name");
                        childEntityDTO.type = typeName;
                        childEntityDTO.displayName = names.getString("displayName");
                        EntityTypeEnum typeNameEnum = EntityTypeEnum.getValue(typeName);
                        switch (typeNameEnum) {
                            case RDBMS_DB:
                                childEntityDTO.parent = entityParentDTO.id;
                                break;
                            case RDBMS_TABLE:
                                //实体为表时，需要获取所属应用或业务域
                                String relationshipAttributes = jsonValue.getString("relationshipAttributes");
                                JSONObject dbInfo = JSONObject.parseObject(relationshipAttributes);
                                JSONObject dbObject = JSONObject.parseObject(dbInfo.getString("db"));
                                Optional<DataSourceDTO> sourceData = allFiDataDataSource.data.stream().filter(e -> dbObject.getString("displayText").equals(e.conDbname)).findFirst();
                                if (!sourceData.isPresent()) {
                                    continue;
                                }
                                Optional<AppBusinessInfoDTO> first = null;
                                switch (sourceData.get().sourceBusinessType) {
                                    case ODS:
                                        first = appList.data.stream().filter(e -> e.id == Integer.parseInt(names.getString("comment"))).findFirst();
                                        break;
                                    case DW:
                                        first = businessAreaList.data.stream().filter(e -> e.id == Integer.parseInt(names.getString("comment"))).findFirst();
                                        break;
                                    default:
                                        continue;
                                }
                                if (!first.isPresent()) {
                                    continue;
                                }
                                EntityStagingDTO dbStag = new EntityStagingDTO();
                                dbStag.guid = dbObject.getString("guid") + "_" + first.get().name;
                                dbStag.parent = dbObject.getString("guid");
                                dbStag.name = first.get().name;
                                dbStag.displayName = first.get().name;
                                //库名
                                dbStag.type = dbObject.getString("displayText");
                                stagingDTOList.add(dbStag);

                                childEntityDTO.parent = dbStag.guid;
                                break;
                            case RDBMS_COLUMN:
                                JSONObject tables = JSON.parseObject(names.getString("table"));
                                childEntityDTO.parent = tables.getString("guid");
                            default:
                                break;
                        }
                        stagingDTOList.add(childEntityDTO);
                    } catch (Exception e) {
                        log.error("getEntityTreeList while ex:" + e);
                        continue;
                    }
                }
                List<EntityStagingDTO> collect = stagingDTOList.stream().distinct().collect(Collectors.toList());
                list.add(buildChildTree(entityParentDTO, collect));
            } catch (Exception e) {
                log.error("getEntityTreeList ex:" + e);
                continue;
            }
        }
        list.sort(Comparator.comparing(EntityTreeDTO::getLabel));
        String jsonString = JSONObject.toJSONString(list);
        redisTemplate.opsForValue().set(metaDataEntity, jsonString);
        return list;*/
    }


    /**
     * 递归，建立子树形结构
     * @param pNode
     * @return
     */
    public EntityTreeDTO buildChildTree(EntityTreeDTO pNode, List<EntityStagingDTO> poList) {
        List<EntityTreeDTO> list = new ArrayList<>();
        for (EntityStagingDTO item : poList) {
            if (item.getParent().equals(pNode.getId())) {
                EntityTreeDTO dto = new EntityTreeDTO();
                dto.id = item.guid;
                dto.label = item.name;
                dto.type = item.type;
                dto.parentId = pNode.id;
                dto.displayName = item.displayName;
                list.add(buildChildTree(dto, poList));
            }
        }
        pNode.children = list;
        return pNode;
    }

    @Override
    public ResultEnum addEntity(EntityDTO dto) {
        //获取所属人
        dto.entity.attributes.owner = userHelper.getLoginUserInfo().username;
        if (dto.entity.typeName.toLowerCase().equals(EntityTypeEnum.RDBMS_INSTANCE.getName())) {
            dto.entity.attributes.qualifiedName += ":" + dto.entity.attributes.port;
        }
        String jsonParameter=JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.post(entity, jsonParameter);
        updateRedis();
        //添加redis
        if (dto.entity.typeName.toLowerCase().equals(EntityTypeEnum.RDBMS_INSTANCE.getName())
        || dto.entity.typeName.toLowerCase().equals(EntityTypeEnum.RDBMS_DB.getName()))
        {
            try {
                //解析数据
                JSONObject jsonObj = JSON.parseObject(result.data);
                JSONObject mutatedEntities= JSON.parseObject(jsonObj.getString("mutatedEntities"));
                JSONArray jsonArray=mutatedEntities.getJSONArray("CREATE");
                setRedis(jsonArray.getJSONObject(0).getString("guid"));
                if (dto.entity.typeName.toLowerCase().equals(EntityTypeEnum.RDBMS_DB.getName()))
                {
                    JSONArray updateAttribute=mutatedEntities.getJSONArray("UPDATE");
                    setRedis(updateAttribute.getJSONObject(0).getString("guid"));
                }
            }
            catch (Exception e)
            {
                log.error("parsing data failure:"+e);
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum deleteEntity(String guid)
    {
        ResultDataDTO<String> result = atlasClient.delete(entityByGuid + "/" + guid);
        updateRedis();
        Boolean exist = redisTemplate.hasKey(metaDataEntity);
        if (exist)
        {
            redisTemplate.delete("metaDataEntityData:"+guid);
        }
        return atlasClient.newResultEnum(result);
    }

    @Override
    public JSONObject getEntity(String guid) {
        return metadataEntity.getMetadataEntityDetails(guid);
        /*Boolean exist = redisTemplate.hasKey("metaDataEntityData:" + guid);
        if (exist) {
            String data = redisTemplate.opsForValue().get("metaDataEntityData:" + guid).toString();
            return JSON.parseObject(data);
        }
        ResultDataDTO<String> result = atlasClient.get(entityByGuid + "/" + guid);
        if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
            throw new FkException(ResultEnum.BAD_REQUEST);
        }
        JSONObject data = JSON.parseObject(result.data);
        List<BusinessMetadataConfigDTO> businessMetadataConfigList = businessMetadataConfigImpl.getBusinessMetadataConfigList();
        Map<String, String> keyMap = new HashMap<>();
        for (BusinessMetadataConfigDTO item : businessMetadataConfigList) {
            keyMap.put(item.attributeName, item.attributeCnName);
        }
        return CommonMethods.changeJsonObj(data, keyMap);*/
    }

    @Override
    public ResultEnum updateEntity(JSONObject dto)
    {
        String jsonParameter=JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.post(entity, jsonParameter);
        updateRedis();
        try {
            JSONObject jsonObj = JSON.parseObject(jsonParameter);
            JSONObject entityObject= JSON.parseObject(jsonObj.getString("entity"));
            String guid = entityObject.getString("guid");
            Boolean exist = redisTemplate.hasKey("metaDataEntityData:"+guid);
            if (exist)
            {
                setRedis(guid);
            }
        }
        catch (Exception e) {
            log.error("updateEntity ex:", e);
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return atlasClient.newResultEnum(result);
    }

    @Override
    public EntityInstanceDTO getInstanceDetail(String guid)
    {
        try {
            String[] s = guid.split("_");
            EntityInstanceDTO data = new EntityInstanceDTO();
            /*ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + s[0]);
            if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
                throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
            }
            EntityInstanceDTO data = new EntityInstanceDTO();
            JSONObject jsonObj = JSON.parseObject(getDetail.data);
            JSONObject entity = JSON.parseObject(jsonObj.getString("entity"));
            JSONObject attributes = JSON.parseObject(entity.getString("attributes"));
            JSONObject instance = JSON.parseObject(attributes.getString("instance"));
            data.instanceGuid = instance.getString("guid");*/
            data.instanceGuid = s[0];
            return data;
        }
        catch (Exception e)
        {
            throw new FkException(ResultEnum.SQL_ANALYSIS);
        }
    }

    /**
     * entity操作后,更新Redis数据
     */
    public void updateRedis(){
        getEntityList();
    }

    @Override
    public SearchBusinessGlossaryEntityDTO searchBasicEntity(EntityFilterDTO dto)
    {

        return metadataEntity.searchBasicEntity(dto);
        /*SearchBusinessGlossaryEntityDTO searchDto = new SearchBusinessGlossaryEntityDTO();
        searchDto.approximateCount = "-1";
        searchDto.queryType = "BASIC";
        searchDto.searchParameters.excludeDeletedEntities = true;
        searchDto.searchParameters.includeClassificationAttributes = true;
        searchDto.searchParameters.includeSubClassifications = true;
        searchDto.searchParameters.includeSubTypes = true;
        searchDto.searchParameters.limit = dto.limit;
        searchDto.searchParameters.offset = dto.offset;
        searchDto.searchParameters.termName = dto.termName;

        // 查询术语id
        QueryWrapper<GlossaryPO> gQw = new QueryWrapper<>();
        gQw.eq("name", dto.termName.split("@")[0]);
        GlossaryPO glossaryPO = glossaryMapper.selectOne(gQw);
        if (glossaryPO == null){
            return searchDto;
        }

        // 查询术语对应的实体集合
        QueryWrapper<MetaDataGlossaryMapPO> mgQw = new QueryWrapper<>();
        mgQw.eq("glossary_id", glossaryPO.id);
        List<MetaDataGlossaryMapPO> metaDataGlossaryMapPOList = metaDataGlossaryMapMapper.selectList(mgQw);
        if (CollectionUtils.isEmpty(metaDataGlossaryMapPOList)){
            return searchDto;
        }
        List<Integer> entityIdList = metaDataGlossaryMapPOList.stream().map(MetaDataGlossaryMapPO::getMetaDataEntityId).collect(Collectors.toList());
        List<MetadataEntityPO> metadataEntityPOList = metadataEntityMapper.selectBatchIds(entityIdList);
        if (CollectionUtils.isEmpty(metadataEntityPOList)){
            return searchDto;
        }

        List<EntitiesDTO> entitiesDTOList = new ArrayList<>();
        for (MetadataEntityPO item : metadataEntityPOList){
            EntitiesDTO eDto = new EntitiesDTO();
            eDto.displayText = item.displayName;
            eDto.typeName = metadataEntityTypeMapper.selectNameById(item.typeId);
            eDto.guid = String.valueOf(item.id);
            eDto.status = "ACTIVE";
            eDto.attributes.name = item.name;
            eDto.attributes.description = item.description;
            eDto.attributes.qualifiedName = item.qualifiedName;
            eDto.attributes.owner = item.owner;

            GlossaryDTO term = new GlossaryDTO();
            term.termGuid = String.valueOf(glossaryPO.id);
            term.displayText = glossaryPO.name;
            eDto.meanings.add(term);

            eDto.meaningNames.add(glossaryPO.name);
            // 查询对应业务分类
            MetadataClassificationMapPO mdcMapPo = metaDataClassificationMapMapper.selectById(item.id);
            if (mdcMapPo != null){
                BusinessClassificationPO businessClassificationPO = businessClassificationMapper.selectById(mdcMapPo.businessClassificationId);
                ClassificationDTO cDto = new ClassificationDTO();
                cDto.typeName = businessClassificationPO.name;
                cDto.entityGuid = String.valueOf(businessClassificationPO.id);
                eDto.classifications.add(cDto);
            }

            entitiesDTOList.add(eDto);
        }

        searchDto.entities = entitiesDTOList;
        return searchDto;*/
    }

    @Override
    public List<EntityAuditsDTO> getAuditsList(String guid)
    {
        List<EntityAuditsDTO> data;
        ResultDataDTO<String> result = atlasClient.get(entity + "/" + guid + "/audit");
        if (result.code != AtlasResultEnum.REQUEST_SUCCESS)
        {
            throw new FkException(ResultEnum.BAD_REQUEST);
        }
        data=JSONObject.parseArray(result.data,EntityAuditsDTO.class);
        return data;
    }

    @Override
    public ResultEnum entityAssociatedLabel(EntityAssociatedLabelDTO dto)
    {
        String jsonParameter=JSONArray.toJSON(dto.list).toString();
        ResultDataDTO<String> result = atlasClient.post(entityByGuid + "/" + dto.guid + "/labels", jsonParameter);
        Boolean exist = redisTemplate.hasKey("metaDataEntityData:"+dto.guid);
        if (exist)
        {
            setRedis(dto.guid);
        }
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum entityAssociatedMetaData(EntityAssociatedMetaDataDTO dto)
    {
        String jsonParameter=JSONArray.toJSON(dto.businessMetaDataAttribute).toString();
        ResultDataDTO<String> result = atlasClient.post(entityByGuid + "/" + dto.guid + "/businessmetadata?isOverwrite=true", jsonParameter);
        Boolean exist = redisTemplate.hasKey("metaDataEntityData:"+dto.guid);
        if (exist)
        {
            setRedis(dto.guid);
        }
        return atlasClient.newResultEnum(result);
    }

    @Override
    public LineAgeDTO getMetaDataKinship(String guid)
    {


        return metadataEntity.getMetaDataKinship(guid);
        /*try {
            ResultDataDTO<String> result = atlasClient.get(lineage + "/" + guid + "?depth=9");
            if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
                throw new FkException(ResultEnum.BAD_REQUEST);
            }
            LineAgeDTO dto=new LineAgeDTO();
            dto.relations=new ArrayList<>();
            dto.guidEntityMap=new ArrayList<>();
            //解析数据
            JSONObject jsonObj = JSON.parseObject(result.data);
            //判断是否存在血缘关系
            JSONArray dataArray=jsonObj.getJSONArray("relations");
            if (dataArray.size()==0)
            {
                dto.guidEntityMap=new ArrayList<>();
                dto.relations=new ArrayList<>();
                return dto;
            }
            //获取血缘关联实体列表
            JSONObject guidEntityMapJson = JSON.parseObject(jsonObj.getString("guidEntityMap"));
            String entityDetail = guidEntityMapJson.getString(guid);
            JSONObject entityDetailJson = JSON.parseObject(entityDetail);
            dto.guidEntityMap.add(entityDetailJson);
            String typeName=entityDetailJson.getString("typeName");

            List<LineAgeRelationsDTO> relationsDtoList;
            relationsDtoList=JSONObject.parseArray(jsonObj.getString("relations"), LineAgeRelationsDTO.class);

            if (!CollectionUtils.isNotEmpty(relationsDtoList))
            {
                return dto;
            }
            //获取输出数据
            LineAgeDTO inData=getInPutData(guid,relationsDtoList,guidEntityMapJson,typeName);
            if (!CollectionUtils.isEmpty(inData.relations))
            {
                dto.guidEntityMap.addAll(inData.guidEntityMap);
                dto.relations.addAll(inData.relations);
            }
            //获取输出数据
            LineAgeDTO outData=getOutPutData(guid,relationsDtoList,guidEntityMapJson,typeName);
            if (!CollectionUtils.isEmpty(outData.relations))
            {
                dto.guidEntityMap.addAll(outData.guidEntityMap);
                dto.relations.addAll(outData.relations);
            }
            return dto;
        }
        catch (Exception e)
        {
            log.error("getMetaDataKinship ex:"+e);
            throw new FkException(ResultEnum.SQL_ANALYSIS);
        }*/
    }

    public LineAgeDTO getInPutData(String guid,
                                   List<LineAgeRelationsDTO> relationsDtoList,
                                   JSONObject guidEntityMapJson,
                                   String typeName)
    {
        LineAgeDTO dto=new LineAgeDTO();
        List<LineAgeRelationsDTO> lineAgeRelationsDtoListStream = relationsDtoList.stream()
                .filter(e -> e.toEntityId.equals(guid))
                .collect(Collectors.toList());
        List<JSONObject> jsonArrayList=new ArrayList<>();
        List<LineAgeRelationsDTO> relations=new ArrayList<>();
        List<String> ids=new ArrayList<>();
        boolean flat=true;
        while (flat)
        {
            ids.clear();
            if (CollectionUtils.isEmpty(lineAgeRelationsDtoListStream))
            {
                flat=false;
            }
            for (LineAgeRelationsDTO item :lineAgeRelationsDtoListStream)
            {
                String  jsonObj1String = guidEntityMapJson.getString(item.fromEntityId);
                JSONObject jsonObj2 = JSON.parseObject(jsonObj1String);
                if (!jsonObj2.getString("typeName").equals(typeName)
                        && !"process".equals(jsonObj2.getString("typeName").toLowerCase()))
                {
                    continue;
                }
                boolean isExist=true;
                //判断process上一级typeName是否一致
                if ("process".equals(jsonObj2.getString("typeName").toLowerCase()))
                {
                    List<LineAgeRelationsDTO> higherLevelLineAge = relationsDtoList.stream()
                            .filter(e -> e.toEntityId.equals(item.fromEntityId))
                            .collect(Collectors.toList());
                    if (!CollectionUtils.isNotEmpty(higherLevelLineAge))
                    {
                        continue;
                    }
                    for (LineAgeRelationsDTO lineAge:higherLevelLineAge)
                    {
                        String  higher = guidEntityMapJson.getString(lineAge.fromEntityId);
                        JSONObject higherJson = JSON.parseObject(higher);
                        if (!higherJson.getString("typeName").equals(typeName))
                        {
                            isExist=false;
                        }
                    }
                }
                if (!isExist)
                {
                    continue;
                }
                //判断relation是否删除
                if (!getRelationShip(item.relationshipId)) {
                    continue;
                }
                String entityDetail1 = guidEntityMapJson.getString(item.fromEntityId);
                JSONObject entityDetailJson1 = JSON.parseObject(entityDetail1);
                //判断实体是否删除
                if (EntityTypeEnum.DELETED.getName().equals(entityDetailJson1.getString("status")))
                {
                    continue;
                }
                relations.add(item);
                jsonArrayList.add(entityDetailJson1);
                ids.add(item.fromEntityId);
            }
            lineAgeRelationsDtoListStream.clear();
            if (!CollectionUtils.isNotEmpty(ids))
            {
                flat=false;
            }
            lineAgeRelationsDtoListStream=relationsDtoList.stream()
                    .filter(e->ids.contains(e.toEntityId))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isNotEmpty(lineAgeRelationsDtoListStream))
            {
                flat=false;
            }
        }
        dto.guidEntityMap=jsonArrayList;
        dto.relations=relations;
        return dto;
    }

    /**
     * 查询选中实体输出血缘数据
     * @param guid
     * @param relationsDtoList
     * @param guidEntityMapJson
     * @param typeName
     * @return
     */
    public LineAgeDTO getOutPutData(String guid,
                                    List<LineAgeRelationsDTO> relationsDtoList,
                                    JSONObject guidEntityMapJson,
                                    String typeName)
    {
        LineAgeDTO dto=new LineAgeDTO();
        List<LineAgeRelationsDTO> lineAgeRelationsDtoStream = relationsDtoList.stream()
                .filter(e -> e.fromEntityId.equals(guid))
                .collect(Collectors.toList());
        List<JSONObject> jsonArrayList=new ArrayList<>();
        List<LineAgeRelationsDTO> relations=new ArrayList<>();
        List<String> ids=new ArrayList<>();
        boolean flat=true;
        while (flat)
        {
            ids.clear();
            if (CollectionUtils.isEmpty(lineAgeRelationsDtoStream))
            {
                flat=false;
            }
            for (LineAgeRelationsDTO item :lineAgeRelationsDtoStream)
            {
                String  jsonObj1String = guidEntityMapJson.getString(item.toEntityId);
                JSONObject jsonObj2 = JSON.parseObject(jsonObj1String);
                if (!jsonObj2.getString("typeName").equals(typeName)
                        && !"process".equals(jsonObj2.getString("typeName").toLowerCase()))
                {
                    continue;
                }
                boolean isExist=true;
                //判断process上一级typeName是否一致
                if ("process".equals(jsonObj2.getString("typeName").toLowerCase()))
                {
                    List<LineAgeRelationsDTO> higherLevelLineAge = relationsDtoList.stream()
                            .filter(e -> e.fromEntityId.equals(item.toEntityId))
                            .collect(Collectors.toList());
                    if (!CollectionUtils.isNotEmpty(higherLevelLineAge))
                    {
                        continue;
                    }
                    for (LineAgeRelationsDTO lineAge:higherLevelLineAge)
                    {
                        String  higher = guidEntityMapJson.getString(lineAge.toEntityId);
                        JSONObject higherJson = JSON.parseObject(higher);
                        if (!higherJson.getString("typeName").equals(typeName))
                        {
                            isExist=false;
                        }
                    }
                }
                if (!isExist)
                {
                    continue;
                }
                //判断relation是否删除
                if (!getRelationShip(item.relationshipId)) {
                    continue;
                }
                String entityDetail1 = guidEntityMapJson.getString(item.toEntityId);
                JSONObject entityDetailJson1 = JSON.parseObject(entityDetail1);
                //判断实体是否删除
                if (EntityTypeEnum.DELETED.getName().equals(entityDetailJson1.getString("status")))
                {
                    continue;
                }
                relations.add(item);
                jsonArrayList.add(entityDetailJson1);
                ids.add(item.toEntityId);
            }
            lineAgeRelationsDtoStream.clear();
            if (!CollectionUtils.isNotEmpty(ids))
            {
                flat=false;
            }
            lineAgeRelationsDtoStream=relationsDtoList.stream()
                    .filter(e->ids.contains(e.fromEntityId))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isNotEmpty(lineAgeRelationsDtoStream))
            {
                flat=false;
            }
        }
        dto.guidEntityMap=jsonArrayList;
        dto.relations=relations;
        return dto;
    }

    /**
     * 判断血缘关系是否删除
     * @param guid
     * @return
     */
    public boolean getRelationShip(String guid)
    {
        try {
            ResultDataDTO<String> result = atlasClient.get(relationship + "/guid/" + guid);
            if (result.code != AtlasResultEnum.REQUEST_SUCCESS)
            {
                return false;
            }
            //解析数据
            JSONObject jsonObj = JSON.parseObject(result.data);
            //判断是否存在血缘关系
            String relationshipStr=jsonObj.getString("relationship");
            JSONObject relationshipJson=JSON.parseObject(relationshipStr);
            if (!relationshipJson.getString("status").equals(EntityTypeEnum.DELETED.getName()))
            {
                return true;
            }
        }
        catch (Exception e)
        {
            log.error("getRelationShip ex:",e);
        }
        return false;
    }

    public void setRedis(String guid)
    {
        ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + guid);
        if (getDetail.code !=AtlasResultEnum.REQUEST_SUCCESS)
        {
            return;
        }
        redisTemplate.opsForValue().set("metaDataEntityData:"+guid,getDetail.data);
    }

}
