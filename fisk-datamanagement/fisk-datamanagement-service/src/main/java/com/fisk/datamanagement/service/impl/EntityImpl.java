package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.datamanagement.dto.entity.*;
import com.fisk.datamanagement.dto.lineage.LineAgeDTO;
import com.fisk.datamanagement.dto.lineage.LineAgeRelationsDTO;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.service.IEntity;
import com.fisk.datamanagement.synchronization.fidata.SynchronizationData;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import com.fisk.datamanagement.vo.ResultErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
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
    private RedisTemplate redisTemplate;
    @Resource
    SynchronizationData synchronizationPgData;

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
    @Value("${atlas.searchQuick}")
    private String searchQuick;
    @Value("${atlas.searchSuggestions}")
    private String searchSuggestions;
    @Value("${spring.metadataentity}")
    private String metaDataEntity;

    @Override
    public List<EntityTreeDTO> getEntityTreeList()
    {
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
     * 获取元数据对象属性结构
     * @return
     */
    public List<EntityTreeDTO> getEntityList()
    {
        List<EntityTreeDTO> list=new ArrayList<>();
        try {
            ResultDataDTO<String> data = atlasClient.Get(searchBasic + "?typeName=rdbms_instance");
            if (data.code != ResultEnum.REQUEST_SUCCESS)
            {
                throw new FkException(data.code);
            }
            JSONObject jsonObj = JSON.parseObject(data.data);
            JSONArray array = jsonObj.getJSONArray("entities");
            for (int i = 0; i < array.size(); i++)
            {
                if (EntityTypeEnum.DELETED.getName().equals(array.getJSONObject(i).getString("status")))
                {
                    continue;
                }
                //获取实例数据
                EntityTreeDTO entityParentDTO=new EntityTreeDTO();
                entityParentDTO.id=array.getJSONObject(i).getString("guid");
                entityParentDTO.label =array.getJSONObject(i).getString("displayText");
                entityParentDTO.type=EntityTypeEnum.RDBMS_INSTANCE.getName();
                entityParentDTO.parentId="-1";
                //查询实例下db/table/column
                ResultDataDTO<String> attribute=atlasClient.Get(entityByGuid+"/"+entityParentDTO.id);
                if (attribute.code != ResultEnum.REQUEST_SUCCESS)
                {
                    throw new FkException(data.code);
                }
                JSONObject jsonObj1 = JSON.parseObject(attribute.data);
                //获取referredEntities
                String referredEntities=jsonObj1.getString("referredEntities");
                JSONObject guidEntityMap = JSON.parseObject(referredEntities);
                Iterator sIterator = guidEntityMap.keySet().iterator();
                List<EntityStagingDTO> stagingDTOList=new ArrayList<>();
                //迭代器获取实例下key值
                while (sIterator.hasNext())
                {
                    String key = sIterator.next().toString();
                    String value = guidEntityMap.getString(key);
                    JSONObject jsonValue = JSON.parseObject(value);
                    //过滤已删除数据
                    if (EntityTypeEnum.DELETED.getName().equals(jsonValue.getString("status")))
                    {
                        continue;
                    }
                    //根据key获取json指定数据
                    String typeName = jsonValue.getString("typeName");
                    EntityStagingDTO childEntityDTO=new EntityStagingDTO();
                    childEntityDTO.guid =jsonValue.getString("guid");
                    String attributes = jsonValue.getString("attributes");
                    JSONObject names = JSON.parseObject(attributes);
                    childEntityDTO.name = names.getString("name");
                    childEntityDTO.type=typeName;
                    EntityTypeEnum typeNameEnum = EntityTypeEnum.getValue(typeName);
                    switch (typeNameEnum)
                    {
                        case RDBMS_DB:
                            childEntityDTO.parent=entityParentDTO.id;
                            break;
                        case RDBMS_TABLE:
                            JSONObject db = JSON.parseObject(names.getString("db"));
                            childEntityDTO.parent=db.getString("guid");
                            break;
                        case RDBMS_COLUMN:
                            JSONObject tables = JSON.parseObject(names.getString("table"));
                            childEntityDTO.parent=tables.getString("guid");
                        default:
                            break;
                    }
                    stagingDTOList.add(childEntityDTO);
                }
                list.add(buildChildTree(entityParentDTO, stagingDTOList));
            }
            list.sort(Comparator.comparing(EntityTreeDTO::getLabel));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error("getEntityTreeList ex:"+e);
            throw new FkException(ResultEnum.SQL_ANALYSIS);
        }
        String jsonString=JSONObject.toJSONString(list);
        redisTemplate.opsForValue().set(metaDataEntity,jsonString);
        return list;
    }

    /**
     * 递归，建立子树形结构
     * @param pNode
     * @return
     */
    public EntityTreeDTO buildChildTree(EntityTreeDTO pNode, List<EntityStagingDTO> poList) {
        List<EntityTreeDTO> list=new ArrayList<>();
        for (EntityStagingDTO item:poList)
        {
            if (item.getParent().equals(pNode.getId()))
            {
                EntityTreeDTO dto=new EntityTreeDTO();
                dto.id=item.guid;
                dto.label =item.name;
                dto.type=item.type;
                dto.parentId=pNode.id;
                list.add(buildChildTree(dto,poList));
            }
        }
        pNode.children =list;
        return pNode;
    }

    @Override
    public ResultEnum addEntity(EntityDTO dto)
    {
        //获取所属人
        dto.entity.attributes.owner=userHelper.getLoginUserInfo().username;
        if (dto.entity.typeName.toLowerCase().equals(EntityTypeEnum.RDBMS_INSTANCE.getName()))
        {
            dto.entity.attributes.comment=dto.entity.attributes.userName+"&"+dto.entity.attributes.password;
        }
        String jsonParameter=JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.Post(entity, jsonParameter);
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
            }
        }
        return result.code==ResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:result.code;
    }

    @Override
    public ResultEnum deleteEntity(String guid)
    {
        ResultDataDTO<String> result = atlasClient.Delete(entityByGuid + "/" + guid);
        updateRedis();
        Boolean exist = redisTemplate.hasKey(metaDataEntity);
        if (exist)
        {
            redisTemplate.delete("metaDataEntityData:"+guid);
        }
        return result.code==ResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:result.code;
    }

    @Override
    public JSONObject getEntity(String guid)
    {
        Boolean exist = redisTemplate.hasKey("metaDataEntityData:"+guid);
        if (exist) {
            String data = redisTemplate.opsForValue().get("metaDataEntityData:"+guid).toString();
            return JSON.parseObject(data);
        }
        ResultDataDTO<String> result = atlasClient.Get(entityByGuid + "/" + guid);
        if (result.code != ResultEnum.REQUEST_SUCCESS)
        {
            throw new FkException(result.code);
        }
        return JSON.parseObject(result.data);
    }

    @Override
    public ResultEnum updateEntity(JSONObject dto)
    {
        String jsonParameter=JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.Post(entity, jsonParameter);
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
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result.code==ResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:result.code;
    }

    @Override
    public EntityInstanceDTO getInstanceDetail(String guid)
    {
        try {
            ResultDataDTO<String> getDetail = atlasClient.Get(entityByGuid + "/" +guid);
            if (getDetail.code !=ResultEnum.REQUEST_SUCCESS)
            {
                throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
            }
            EntityInstanceDTO data=new EntityInstanceDTO();
            JSONObject jsonObj = JSON.parseObject(getDetail.data);
            JSONObject entity= JSON.parseObject(jsonObj.getString("entity"));
            JSONObject attributes=JSON.parseObject(entity.getString("attributes"));
            JSONObject instance=JSON.parseObject(attributes.getString("instance"));
            data.instanceGuid=instance.getString("guid");
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
        /*Thread t1 = new Thread(new Runnable(){
            @Override
            public void run() {
                getEntityList();
            }
        });
        t1.start();*/
        getEntityList();
    }

    @Override
    public JSONObject searchBasicEntity(EntityFilterDTO dto)
    {
        String jsonParameter=JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.Post(searchBasic, jsonParameter);
        if (result.code != ResultEnum.REQUEST_SUCCESS)
        {
            JSONObject msg=JSON.parseObject(result.data);
            throw new FkException(result.code,msg.getString("errorMessage"));
        }
        return JSON.parseObject(result.data);
    }

    @Override
    public List<EntityAuditsDTO> getAuditsList(String guid)
    {
        List<EntityAuditsDTO> data;
        ResultDataDTO<String> result = atlasClient.Get(entity + "/" + guid + "/audit");
        if (result.code != ResultEnum.REQUEST_SUCCESS)
        {
            throw new FkException(result.code);
        }
        data=JSONObject.parseArray(result.data,EntityAuditsDTO.class);
        return data;
    }

    @Override
    public ResultEnum entityAssociatedLabel(EntityAssociatedLabelDTO dto)
    {
        String jsonParameter=JSONArray.toJSON(dto.list).toString();
        ResultDataDTO<String> result = atlasClient.Post(entityByGuid + "/" + dto.guid + "/labels", jsonParameter);
        return result.code==ResultEnum.NO_CONTENT?ResultEnum.SUCCESS:result.code;
    }

    @Override
    public ResultEnum entityAssociatedMetaData(EntityAssociatedMetaDataDTO dto)
    {
        String jsonParameter=JSONArray.toJSON(dto.businessMetaDataAttribute).toString();
        ResultDataDTO<String> result = atlasClient.Post(entityByGuid + "/" + dto.guid + "/businessmetadata?isOverwrite=true", jsonParameter);
        return newResultEnum(result);
    }

    @Override
    public LineAgeDTO getMetaDataKinship(String guid)
    {
        try {
            LineAgeDTO dto=new LineAgeDTO();
            ResultDataDTO<String> result = atlasClient.Get(lineage + "/" + guid);
            if (result.code != ResultEnum.REQUEST_SUCCESS)
            {
                throw new FkException(result.code);
            }
            List<JSONObject> jsonArrayList=new ArrayList<>();
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
            jsonArrayList.add(entityDetailJson);
            String typeName=entityDetailJson.getString("typeName");

            List<LineAgeRelationsDTO> relationsDTOS;
            relationsDTOS=JSONObject.parseArray(jsonObj.getString("relations"), LineAgeRelationsDTO.class);

            if (!CollectionUtils.isNotEmpty(relationsDTOS))
            {
                return dto;
            }
            List<LineAgeRelationsDTO> lineAgeRelationsDTOStream = relationsDTOS.stream()
                    .filter(e -> e.toEntityId.equals(guid))
                    .collect(Collectors.toList());
            List<LineAgeRelationsDTO> relations=new ArrayList<>();
            List<String> ids=new ArrayList<>();
            boolean flat=true;
            while (flat)
            {
                ids.clear();
                if (CollectionUtils.isEmpty(lineAgeRelationsDTOStream))
                {
                    flat=false;
                }
                for (LineAgeRelationsDTO item :lineAgeRelationsDTOStream)
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
                        List<LineAgeRelationsDTO> higherLevelLineAge = relationsDTOS.stream()
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
                lineAgeRelationsDTOStream.clear();
                if (!CollectionUtils.isNotEmpty(ids))
                {
                    flat=false;
                }
                lineAgeRelationsDTOStream=relationsDTOS.stream()
                        .filter(e->ids.contains(e.toEntityId))
                        .collect(Collectors.toList());
                if (!CollectionUtils.isNotEmpty(lineAgeRelationsDTOStream))
                {
                    flat=false;
                }
            }
            dto.guidEntityMap=jsonArrayList;
            dto.relations=relations;
            //获取输出数据
            LineAgeDTO outData=getOutPutData(guid,relationsDTOS,guidEntityMapJson,typeName);
            dto.guidEntityMap.addAll(outData.guidEntityMap);
            dto.relations.addAll(outData.relations);
            return dto;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error("getMetaDataKinship ex:"+e);
            throw new FkException(ResultEnum.SQL_ANALYSIS);
        }
    }

    /**
     * 查询选中实体输出血缘数据
     * @param guid
     * @param relationsDTOS
     * @param guidEntityMapJson
     * @param typeName
     * @return
     */
    public LineAgeDTO getOutPutData(String guid,
                                    List<LineAgeRelationsDTO> relationsDTOS,
                                    JSONObject guidEntityMapJson,
                                    String typeName)
    {
        LineAgeDTO dto=new LineAgeDTO();
        List<LineAgeRelationsDTO> lineAgeRelationsDTOStream = relationsDTOS.stream()
                .filter(e -> e.fromEntityId.equals(guid))
                .collect(Collectors.toList());
        List<JSONObject> jsonArrayList=new ArrayList<>();
        List<LineAgeRelationsDTO> relations=new ArrayList<>();
        List<String> ids=new ArrayList<>();
        boolean flat=true;
        while (flat)
        {
            ids.clear();
            if (CollectionUtils.isEmpty(lineAgeRelationsDTOStream))
            {
                flat=false;
            }
            for (LineAgeRelationsDTO item :lineAgeRelationsDTOStream)
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
                    List<LineAgeRelationsDTO> higherLevelLineAge = relationsDTOS.stream()
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
            lineAgeRelationsDTOStream.clear();
            if (!CollectionUtils.isNotEmpty(ids))
            {
                flat=false;
            }
            lineAgeRelationsDTOStream=relationsDTOS.stream()
                    .filter(e->ids.contains(e.fromEntityId))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isNotEmpty(lineAgeRelationsDTOStream))
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
            ResultDataDTO<String> result = atlasClient.Get(relationship + "/guid/" + guid);
            if (result.code != ResultEnum.REQUEST_SUCCESS)
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
            e.printStackTrace();
            log.error("getRelationShip ex:",e);
        }
        return false;
    }

    @Override
    public JSONObject searchQuick(String query,int limit,int offset)
    {
        ResultDataDTO<String> result = atlasClient.Get(searchQuick + "?query=" + query + "&limit=" + limit + "&offset=" + offset);
        if (result.code != ResultEnum.REQUEST_SUCCESS)
        {
            JSONObject msg=JSON.parseObject(result.data);
            throw new FkException(result.code,msg.getString("errorMessage"));
        }
        return JSON.parseObject(result.data);
    }

    @Override
    public JSONObject searchSuggestions(String prefixString)
    {
        ResultDataDTO<String> result = atlasClient.Get(searchSuggestions + "?prefixString=" + prefixString);
        if (result.code != ResultEnum.REQUEST_SUCCESS)
        {
            JSONObject msg=JSON.parseObject(result.data);
            throw new FkException(result.code,msg.getString("errorMessage"));
        }
        return JSON.parseObject(result.data);
    }

    public void setRedis(String guid)
    {
        ResultDataDTO<String> getDetail = atlasClient.Get(entityByGuid + "/" + guid);
        if (getDetail.code !=ResultEnum.REQUEST_SUCCESS)
        {
            return;
        }
        redisTemplate.opsForValue().set("metaDataEntityData:"+guid,getDetail.data);
    }

    public ResultEnum newResultEnum(ResultDataDTO<String> result)
    {
        String error="";
        if (result.code.getCode() !=ResultEnum.NO_CONTENT.getCode())
        {
            try {
                ResultErrorDTO dto=JSONObject.parseObject(result.data,ResultErrorDTO.class);
                error = dto.errorMessage;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return result.code;
            }
        }
        throw new FkException(ResultEnum.BAD_REQUEST,error);

    }

}
