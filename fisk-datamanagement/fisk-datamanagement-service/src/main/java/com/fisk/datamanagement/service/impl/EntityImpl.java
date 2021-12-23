package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.dto.entity.*;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.service.IEntity;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class EntityImpl implements IEntity {

    @Resource
    AtlasClient atlasClient;

    @Value("${atlas.searchBasic}")
    private String searchBasic;
    @Value("${atlas.entityByGuid}")
    private String entityByGuid;
    @Value("${atlas.entity}")
    private String entity;

    @Override
    public List<EntityTreeDTO> getEntityTreeList()
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
                if ("DELETED".equals(array.getJSONObject(i).getString("status")))
                {
                    continue;
                }
                //获取实例数据
                EntityTreeDTO entityParentDTO=new EntityTreeDTO();
                entityParentDTO.id=array.getJSONObject(i).getString("guid");
                entityParentDTO.name=array.getJSONObject(i).getString("displayText");
                entityParentDTO.type=EntityTypeEnum.RDBMS_INSTANCE.getName();
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
                    if ("DELETED".equals(jsonValue.getString("status")))
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error("getEntityTreeList ex:"+e);
            throw new FkException(ResultEnum.SQL_ANALYSIS);
        }
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
                dto.name=item.name;
                dto.type=item.type;
                list.add(buildChildTree(dto,poList));
            }
        }
        pNode.list=list;
        return pNode;
    }

    @Override
    public ResultEnum addEntity(EntityDTO dto)
    {
        String jsonParameter=JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.Post(entity, jsonParameter);
        return result.code==ResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:result.code;
    }

    @Override
    public ResultEnum deleteEntity(String guid)
    {
        ResultDataDTO<String> result = atlasClient.Delete(entityByGuid + "/" + guid);
        return result.code==ResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:result.code;
    }

    @Override
    public EntityDetailDTO getEntity(String guid)
    {
        EntityDetailDTO dto=new EntityDetailDTO();
        ResultDataDTO<String> result = atlasClient.Get(entityByGuid + "/" + guid);
        if (result.code != ResultEnum.REQUEST_SUCCESS)
        {
            throw new FkException(result.code);
        }
        dto.entityDetailJson=result.data;
        return dto;
    }

}
