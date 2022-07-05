package com.fisk.datamanagement.synchronization.pushmetadata.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.metadata.dto.metadata.MetaDataColumnAttributeDTO;
import com.fisk.common.server.metadata.dto.metadata.MetaDataDbAttributeDTO;
import com.fisk.common.server.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.common.server.metadata.dto.metadata.MetaDataTableAttributeDTO;
import com.fisk.datamanagement.dto.entity.EntityAttributesDTO;
import com.fisk.datamanagement.dto.entity.EntityDTO;
import com.fisk.datamanagement.dto.entity.EntityIdAndTypeDTO;
import com.fisk.datamanagement.dto.entity.EntityTypeDTO;
import com.fisk.datamanagement.entity.MetadataMapAtlasPO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.mapper.MetadataMapAtlasMapper;
import com.fisk.datamanagement.synchronization.pushmetadata.IMetaData;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class MetaDataImpl implements IMetaData {

    @Resource
    AtlasClient atlasClient;
    @Resource
    MetadataMapAtlasMapper metadataMapAtlasMapper;

    @Value("${atlas.entity}")
    private String entity;
    @Value("${atlas.entityByGuid}")
    private String entityByGuid;

    @Override
    public ResultEnum metaData(List<MetaDataInstanceAttributeDTO> data) {
        for (MetaDataInstanceAttributeDTO instance : data) {
            String instanceGuid = metaDataInstance(instance);
            if (StringUtils.isEmpty(instanceGuid)) {
                continue;
            }
            for (MetaDataDbAttributeDTO db : instance.dbList) {
                String dbGuid = metaDataDb(db, instanceGuid);
                if (StringUtils.isEmpty(dbGuid)) {
                    continue;
                }
                for (MetaDataTableAttributeDTO table : db.tableList) {
                    String tableGuid = metaDataTable(table, dbGuid);
                    if (StringUtils.isEmpty(tableGuid)) {
                        continue;
                    }
                    for (MetaDataColumnAttributeDTO field : table.columnList) {
                        metaDataField(field, tableGuid);
                    }
                }
            }
        }
        return ResultEnum.SUCCESS;
    }

    public String metaDataInstance(MetaDataInstanceAttributeDTO dto) {
        String atlasGuid = getMetaDataConfig(dto.qualifiedName);
        //为空,则新增
        if (StringUtils.isEmpty(atlasGuid)) {
            EntityDTO entityDTO = new EntityDTO();
            EntityTypeDTO entityTypeDTO = new EntityTypeDTO();
            entityTypeDTO.typeName = EntityTypeEnum.RDBMS_INSTANCE.getName();
            EntityAttributesDTO attributesDTO = new EntityAttributesDTO();
            attributesDTO.name = dto.name;
            attributesDTO.qualifiedName = dto.qualifiedName;
            attributesDTO.hostname = dto.hostname;
            attributesDTO.port = dto.port;
            attributesDTO.contact_info = dto.contact_info;
            attributesDTO.description = dto.description;
            attributesDTO.protocol = dto.protocol;
            attributesDTO.owner = "";
            attributesDTO.rdbms_type = dto.rdbms_type;
            entityTypeDTO.attributes = attributesDTO;
            entityDTO.entity = entityTypeDTO;
            return addMetaDataConfig(JSONArray.toJSON(entityDTO).toString(), dto.qualifiedName, EntityTypeEnum.RDBMS_INSTANCE);
        }
        ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + atlasGuid);
        if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return "";
        }
        //解析数据
        JSONObject jsonObj = JSON.parseObject(getDetail.data);
        JSONObject entityObject = JSON.parseObject(jsonObj.getString("entity"));
        JSONObject attribute = JSON.parseObject(entityObject.getString("attributes"));
        //修改数据
        attribute.put("hostname", dto.hostname);
        attribute.put("name", dto.name);
        attribute.put("port", dto.port);
        attribute.put("platform", dto.platform);
        attribute.put("protocol", dto.protocol);
        attribute.put("comment", dto.comment);
        attribute.put("contact_info", dto.contact_info);
        attribute.put("description", dto.description);
        entityObject.put("attributes", attribute);
        jsonObj.put("entity", entityObject);
        String jsonParameter = JSONArray.toJSON(jsonObj).toString();
        ResultDataDTO<String> result = atlasClient.post(entity, jsonParameter);
        if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return "";
        }
        return atlasGuid;
    }

    public String metaDataDb(MetaDataDbAttributeDTO dto, String parentEntityGuid) {
        String atlasGuid = getMetaDataConfig(dto.qualifiedName);
        if (StringUtils.isEmpty(atlasGuid)) {
            EntityDTO entityDTO = new EntityDTO();
            EntityTypeDTO entityTypeDTO = new EntityTypeDTO();
            entityTypeDTO.typeName = EntityTypeEnum.RDBMS_DB.getName();
            EntityIdAndTypeDTO parentEntity = new EntityIdAndTypeDTO();
            EntityAttributesDTO attributesDTO = new EntityAttributesDTO();
            attributesDTO.qualifiedName = dto.qualifiedName;
            attributesDTO.name = dto.name;
            attributesDTO.description = dto.description;
            attributesDTO.comment = dto.comment;
            parentEntity.typeName = EntityTypeEnum.RDBMS_INSTANCE.getName();
            parentEntity.guid = parentEntityGuid;
            attributesDTO.instance = parentEntity;
            return addMetaDataConfig(JSONArray.toJSON(entityDTO).toString(), dto.qualifiedName, EntityTypeEnum.RDBMS_DB);
        }
        ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + atlasGuid);
        if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return "";
        }
        //解析数据
        JSONObject jsonObj = JSON.parseObject(getDetail.data);
        JSONObject entityObject = JSON.parseObject(jsonObj.getString("entity"));
        JSONObject attribute = JSON.parseObject(entityObject.getString("attributes"));
        attribute.put("name", dto.name);
        attribute.put("comment", dto.comment);
        attribute.put("contact_info", dto.contact_info);
        attribute.put("description", dto.description);
        entityObject.put("attributes", attribute);
        jsonObj.put("entity", entityObject);
        String jsonParameter = JSONArray.toJSON(jsonObj).toString();
        ResultDataDTO<String> result = atlasClient.post(entity, jsonParameter);
        if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return "";
        }
        return atlasGuid;
    }

    public String metaDataTable(MetaDataTableAttributeDTO dto, String parentEntityGuid) {
        String atlasGuid = getMetaDataConfig(dto.qualifiedName);
        if (StringUtils.isEmpty(atlasGuid)) {
            EntityDTO entityDTO = new EntityDTO();
            EntityTypeDTO entityTypeDTO = new EntityTypeDTO();
            entityTypeDTO.typeName = EntityTypeEnum.RDBMS_TABLE.getName();
            EntityIdAndTypeDTO parentEntity = new EntityIdAndTypeDTO();
            EntityAttributesDTO attributesDTO = new EntityAttributesDTO();
            attributesDTO.qualifiedName = dto.qualifiedName;
            attributesDTO.name = dto.name;
            attributesDTO.description = dto.description;
            attributesDTO.comment = dto.comment;
            parentEntity.typeName = EntityTypeEnum.RDBMS_DB.getName();
            parentEntity.guid = parentEntityGuid;
            attributesDTO.db = parentEntity;
            return addMetaDataConfig(JSONArray.toJSON(entityDTO).toString(), dto.qualifiedName, EntityTypeEnum.RDBMS_TABLE);
        }
        ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + atlasGuid);
        if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return "";
        }
        //解析数据
        JSONObject jsonObj = JSON.parseObject(getDetail.data);
        JSONObject entityObject = JSON.parseObject(jsonObj.getString("entity"));
        JSONObject attribute = JSON.parseObject(entityObject.getString("attributes"));
        attribute.put("name", dto.name);
        attribute.put("comment", dto.comment);
        attribute.put("contact_info", dto.contact_info);
        attribute.put("description", dto.description);
        entityObject.put("attributes", attribute);
        jsonObj.put("entity", entityObject);
        String jsonParameter = JSONArray.toJSON(jsonObj).toString();
        ResultDataDTO<String> result = atlasClient.post(entity, jsonParameter);
        if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return "";
        }
        return atlasGuid;
    }

    public String metaDataField(MetaDataColumnAttributeDTO dto, String parentEntityGuid) {
        String atlasGuid = getMetaDataConfig(dto.qualifiedName);
        if (StringUtils.isEmpty(atlasGuid)) {
            EntityDTO entityDTO = new EntityDTO();
            EntityTypeDTO entityTypeDTO = new EntityTypeDTO();
            entityTypeDTO.typeName = EntityTypeEnum.RDBMS_COLUMN.getName();
            EntityIdAndTypeDTO parentEntity = new EntityIdAndTypeDTO();
            EntityAttributesDTO attributesDTO = new EntityAttributesDTO();
            attributesDTO.name = dto.name;
            attributesDTO.comment = dto.comment;
            attributesDTO.qualifiedName = dto.qualifiedName;
            attributesDTO.data_type = dto.dataType;
            attributesDTO.contact_info = dto.contact_info;
            parentEntity.typeName = EntityTypeEnum.RDBMS_TABLE.getName();
            parentEntity.guid = parentEntityGuid;
            attributesDTO.table = parentEntity;
            return addMetaDataConfig(JSONArray.toJSON(entityDTO).toString(), dto.qualifiedName, EntityTypeEnum.RDBMS_COLUMN);
        }
        ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + atlasGuid);
        if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return "";
        }
        //解析数据
        JSONObject jsonObj = JSON.parseObject(getDetail.data);
        JSONObject entityObject = JSON.parseObject(jsonObj.getString("entity"));
        JSONObject attribute = JSON.parseObject(entityObject.getString("attributes"));
        attribute.put("name", dto.name);
        attribute.put("comment", dto.comment);
        attribute.put("contact_info", dto.contact_info);
        attribute.put("description", dto.description);
        attribute.put("data_type", dto.dataType);
        entityObject.put("attributes", attribute);
        jsonObj.put("entity", entityObject);
        String jsonParameter = JSONArray.toJSON(jsonObj).toString();
        ResultDataDTO<String> result = atlasClient.post(entity, jsonParameter);
        if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return "";
        }
        return atlasGuid;
    }

    private String addMetaDataConfig(String jsonStr, String qualifiedName, EntityTypeEnum entityTypeEnum) {
        try {
            //调用atlas添加实例
            ResultDataDTO<String> result = atlasClient.post(entity, jsonStr);
            if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
                return "";
            }
            JSONObject jsonObj = JSON.parseObject(result.data);
            JSONObject mutatedEntities = JSON.parseObject(jsonObj.getString("mutatedEntities"));
            String strMutatedEntities = mutatedEntities.toString();
            JSONArray jsonArray;
            if (strMutatedEntities.indexOf("CREATE") > -1) {
                jsonArray = mutatedEntities.getJSONArray("CREATE");
            } else {
                jsonArray = mutatedEntities.getJSONArray("UPDATE");
            }
            MetadataMapAtlasPO metadataMapAtlasPo = new MetadataMapAtlasPO();
            metadataMapAtlasPo.qualifiedName = qualifiedName;
            metadataMapAtlasPo.atlasGuid = jsonArray.getJSONObject(0).getString("guid");
            metadataMapAtlasPo.type = entityTypeEnum.getValue();
            return metadataMapAtlasMapper.insert(metadataMapAtlasPo) > 0 ? jsonArray.getJSONObject(0).getString("guid") : "";
        } catch (Exception e) {
            log.error("addMetaDataConfig ex:", e);
            return "";
        }
    }

    /**
     * 实体是否已存在
     *
     * @param qualifiedName
     * @return
     */
    public String getMetaDataConfig(String qualifiedName) {
        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataMapAtlasPO::getQualifiedName, qualifiedName);
        MetadataMapAtlasPO po = metadataMapAtlasMapper.selectOne(queryWrapper);
        return po == null ? "" : po.atlasGuid;
    }


}
