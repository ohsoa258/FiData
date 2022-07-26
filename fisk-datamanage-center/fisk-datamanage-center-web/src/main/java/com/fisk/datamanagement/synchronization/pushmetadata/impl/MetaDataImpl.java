package com.fisk.datamanagement.synchronization.pushmetadata.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.metadata.dto.metadata.*;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.datamanagement.dto.entity.EntityAttributesDTO;
import com.fisk.datamanagement.dto.entity.EntityDTO;
import com.fisk.datamanagement.dto.entity.EntityIdAndTypeDTO;
import com.fisk.datamanagement.dto.entity.EntityTypeDTO;
import com.fisk.datamanagement.entity.MetadataMapAtlasPO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.map.MetaDataMap;
import com.fisk.datamanagement.mapper.MetadataMapAtlasMapper;
import com.fisk.datamanagement.service.impl.EntityImpl;
import com.fisk.datamanagement.synchronization.fidata.SynchronizationKinShip;
import com.fisk.datamanagement.synchronization.pushmetadata.IMetaData;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.task.BuildMetaDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class MetaDataImpl implements IMetaData {

    @Resource
    AtlasClient atlasClient;
    @Resource
    EntityImpl entityImpl;
    @Resource
    MetadataMapAtlasMapper metadataMapAtlasMapper;
    @Resource
    UserHelper userHelper;
    @Resource
    PublishTaskClient client;
    @Resource
    UserClient userClient;
    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    DataModelClient dataModelClient;
    @Resource
    SynchronizationKinShip synchronizationKinShip;

    @Value("${atlas.entity}")
    private String entity;
    @Value("${atlas.entityByGuid}")
    private String entityByGuid;

    @Override
    public ResultEnum metaData(MetaDataAttributeDTO data) {
        try {
            log.info("开始推送元数据实时同步，参数:{}", JSON.toJSONString(data));
            BuildMetaDataDTO dto = new BuildMetaDataDTO();
            dto.userId = (long) 52; //userHelper.getLoginUserInfo().id;
            dto.data = data.instanceList;
            client.metaData(dto);
            return ResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("元数据实时同步失败,失败信息:", e);
            return ResultEnum.SAVE_DATA_ERROR;
        }
    }

    @Override
    public ResultEnum consumeMetaData(List<MetaDataInstanceAttributeDTO> data) {
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
                    //同步表血缘
                    synchronizationTableKinShip(db.name, tableGuid, table.name);
                    List<String> qualifiedNames = new ArrayList<>();
                    for (MetaDataColumnAttributeDTO field : table.columnList) {
                        metaDataField(field, tableGuid);
                        qualifiedNames.add(field.qualifiedName);
                    }
                    //删除
                    deleteMetaData(qualifiedNames, tableGuid);
                }
            }
        }
        //更新Redis
        entityImpl.updateRedis();
        return ResultEnum.SUCCESS;
    }

    public void synchronizationTableKinShip(String dbName, String tableGuid, String tableName) {
        try {
            String dbQualifiedName = whetherSynchronization(dbName);
            if (StringUtils.isEmpty(dbQualifiedName)) {
                return;
            }
            //获取实体详情
            ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + tableGuid);
            if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
                return;
            }
            //获取ods表信息
            ResultEntity<List<DataAccessSourceTableDTO>> odsResult = dataAccessClient.getDataAccessMetaData();
            if (odsResult.code != ResultEnum.SUCCESS.getCode() || CollectionUtils.isEmpty(odsResult.data)) {
                return;
            }
            //获取dw表信息
            ResultEntity<Object> result = dataModelClient.getDataModelTable(1);
            if (result.code != ResultEnum.SUCCESS.getCode()) {
                return;
            }
            //序列化
            List<SourceTableDTO> list = JSON.parseArray(JSON.toJSONString(result.data), SourceTableDTO.class);
            Optional<SourceTableDTO> first = list.stream().filter(e -> tableName.equals(e.tableName)).findFirst();
            if (!first.isPresent()) {
                return;
            }
            List<String> tableList = first.get().fieldList
                    .stream()
                    .map(e -> e.getSourceTable())
                    .distinct()
                    .collect(Collectors.toList());
            tableList.removeAll(Collections.singleton(null));
            List<EntityIdAndTypeDTO> inputTableList = getTableList(tableList, odsResult.data, first.get(), dbQualifiedName);
            //解析数据
            JSONObject jsonObj = JSON.parseObject(getDetail.data);
            JSONObject entityObject = JSON.parseObject(jsonObj.getString("entity"));
            JSONObject relationShip = JSON.parseObject(entityObject.getString("relationshipAttributes"));
            JSONArray relationShipAttribute = JSON.parseArray(relationShip.getString("outputFromProcesses"));
            //条数为0,则添加process
            if (relationShipAttribute.size() == 0) {
                //synchronizationKinShip.addProcess(EntityTypeEnum.RDBMS_TABLE, first.get().sqlScript, inputTableList, tableGuid);
            }
        } catch (Exception e) {
            log.error("同步表血缘失败,表guid" + tableGuid + " ex:", e);
            return;
        }
    }

    @Override
    public ResultEnum deleteMetaData(MetaDataDeleteAttributeDTO dto) {
        for (String qualifiedName : dto.qualifiedNames) {
            QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(MetadataMapAtlasPO::getQualifiedName, qualifiedName);
            MetadataMapAtlasPO po = metadataMapAtlasMapper.selectOne(queryWrapper);
            if (po == null) {
                continue;
            }
            ResultEnum resultEnum = entityImpl.deleteEntity(po.atlasGuid);
            if (resultEnum.getCode() != ResultEnum.SUCCESS.getCode()) {
                continue;
            }
            int flat = metadataMapAtlasMapper.delete(queryWrapper);
            if (flat > 0) {
                delete(po.atlasGuid);
            }
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 循环删除子节点
     *
     * @param atlasGuid
     */
    public void delete(String atlasGuid) {
        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataMapAtlasPO::getParentAtlasGuid, atlasGuid);
        List<MetadataMapAtlasPO> list = metadataMapAtlasMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        String guid = list.get(0).atlasGuid;
        int flat = metadataMapAtlasMapper.delete(queryWrapper);
        if (flat > 0) {
            delete(guid);
        }
    }

    /**
     * 实例新增/修改
     *
     * @param dto
     * @return
     */
    public String metaDataInstance(MetaDataInstanceAttributeDTO dto) {
        String atlasGuid = getMetaDataConfig(dto.qualifiedName);
        //为空,则新增
        if (StringUtils.isEmpty(atlasGuid)) {
            EntityDTO entityDTO = new EntityDTO();
            EntityTypeDTO entityTypeDTO = new EntityTypeDTO();
            entityTypeDTO.typeName = EntityTypeEnum.RDBMS_INSTANCE.getName();
            EntityAttributesDTO attributesDTO = MetaDataMap.INSTANCES.instanceDtoToAttribute(dto);
            entityTypeDTO.attributes = attributesDTO;
            entityDTO.entity = entityTypeDTO;
            return addMetaDataConfig(JSONArray.toJSON(entityDTO).toString(), dto.qualifiedName, EntityTypeEnum.RDBMS_INSTANCE, "");
        }
        //修改
        return updateMetaDataEntity(atlasGuid, EntityTypeEnum.RDBMS_INSTANCE, dto);
    }

    /**
     * 库新增/修改
     *
     * @param dto
     * @param parentEntityGuid
     * @return
     */
    public String metaDataDb(MetaDataDbAttributeDTO dto, String parentEntityGuid) {
        String atlasGuid = getMetaDataConfig(dto.qualifiedName);
        if (StringUtils.isEmpty(atlasGuid)) {
            EntityDTO entityDTO = new EntityDTO();
            EntityTypeDTO entityTypeDTO = new EntityTypeDTO();
            entityTypeDTO.typeName = EntityTypeEnum.RDBMS_DB.getName();
            EntityIdAndTypeDTO parentEntity = new EntityIdAndTypeDTO();
            EntityAttributesDTO attributesDTO = MetaDataMap.INSTANCES.dbDtoToAttribute(dto);
            parentEntity.typeName = EntityTypeEnum.RDBMS_INSTANCE.getName();
            parentEntity.guid = parentEntityGuid;
            attributesDTO.instance = parentEntity;
            entityTypeDTO.attributes = attributesDTO;
            entityDTO.entity = entityTypeDTO;
            return addMetaDataConfig(JSONArray.toJSON(entityDTO).toString(), dto.qualifiedName, EntityTypeEnum.RDBMS_DB, parentEntityGuid);
        }
        return updateMetaDataEntity(atlasGuid, EntityTypeEnum.RDBMS_DB, dto);
    }

    /**
     * 表新增/修改
     *
     * @param dto
     * @param parentEntityGuid
     * @return
     */
    public String metaDataTable(MetaDataTableAttributeDTO dto, String parentEntityGuid) {
        String atlasGuid = getMetaDataConfig(dto.qualifiedName);
        if (StringUtils.isEmpty(atlasGuid)) {
            EntityDTO entityDTO = new EntityDTO();
            EntityTypeDTO entityTypeDTO = new EntityTypeDTO();
            entityTypeDTO.typeName = EntityTypeEnum.RDBMS_TABLE.getName();
            EntityIdAndTypeDTO parentEntity = new EntityIdAndTypeDTO();
            EntityAttributesDTO attributesDTO = MetaDataMap.INSTANCES.tableDtoToAttribute(dto);
            parentEntity.typeName = EntityTypeEnum.RDBMS_DB.getName();
            parentEntity.guid = parentEntityGuid;
            attributesDTO.db = parentEntity;
            entityTypeDTO.attributes = attributesDTO;
            entityDTO.entity = entityTypeDTO;
            return addMetaDataConfig(JSONArray.toJSON(entityDTO).toString(), dto.qualifiedName, EntityTypeEnum.RDBMS_TABLE, parentEntityGuid);
        }
        return updateMetaDataEntity(atlasGuid, EntityTypeEnum.RDBMS_TABLE, dto);
    }

    /**
     * 字段新增/修改
     *
     * @param dto
     * @param parentEntityGuid
     * @return
     */
    public String metaDataField(MetaDataColumnAttributeDTO dto, String parentEntityGuid) {
        String atlasGuid = getMetaDataConfig(dto.qualifiedName);
        if (StringUtils.isEmpty(atlasGuid)) {
            EntityDTO entityDTO = new EntityDTO();
            EntityTypeDTO entityTypeDTO = new EntityTypeDTO();
            entityTypeDTO.typeName = EntityTypeEnum.RDBMS_COLUMN.getName();
            EntityIdAndTypeDTO parentEntity = new EntityIdAndTypeDTO();
            EntityAttributesDTO attributesDTO = MetaDataMap.INSTANCES.fieldDtoToAttribute(dto);
            parentEntity.typeName = EntityTypeEnum.RDBMS_TABLE.getName();
            parentEntity.guid = parentEntityGuid;
            attributesDTO.table = parentEntity;
            entityTypeDTO.attributes = attributesDTO;
            entityDTO.entity = entityTypeDTO;
            return addMetaDataConfig(JSONArray.toJSON(entityDTO).toString(), dto.qualifiedName, EntityTypeEnum.RDBMS_COLUMN, parentEntityGuid);
        }
        return updateMetaDataEntity(atlasGuid, EntityTypeEnum.RDBMS_COLUMN, dto);
    }

    /**
     * 更新元数据实体
     *
     * @param atlasGuid
     * @param entityTypeEnum
     * @param dto
     * @return
     */
    public String updateMetaDataEntity(String atlasGuid,
                                       EntityTypeEnum entityTypeEnum,
                                       MetaDataBaseAttributeDTO dto) {
        ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + atlasGuid);
        if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return "";
        }
        //解析数据
        JSONObject jsonObj = JSON.parseObject(getDetail.data);
        JSONObject entityObject = JSON.parseObject(jsonObj.getString("entity"));
        JSONObject attribute = JSON.parseObject(entityObject.getString("attributes"));
        switch (entityTypeEnum) {
            case RDBMS_INSTANCE:
                MetaDataInstanceAttributeDTO data = (MetaDataInstanceAttributeDTO) dto;
                //修改数据
                attribute.put("hostname", data.hostname);
                attribute.put("name", data.name);
                attribute.put("port", data.port);
                attribute.put("platform", data.platform);
                attribute.put("protocol", data.protocol);
                attribute.put("comment", data.comment);
                attribute.put("contact_info", data.contact_info);
                attribute.put("description", data.description);
                break;
            case RDBMS_DB:
            case RDBMS_TABLE:
                attribute.put("name", dto.name);
                attribute.put("comment", dto.comment);
                attribute.put("contact_info", dto.contact_info);
                attribute.put("description", dto.description);
                break;
            case RDBMS_COLUMN:
                MetaDataColumnAttributeDTO field = (MetaDataColumnAttributeDTO) dto;
                attribute.put("name", field.name);
                attribute.put("comment", field.comment);
                attribute.put("contact_info", field.contact_info);
                attribute.put("description", field.description);
                attribute.put("data_type", field.dataType);
                break;
            default:
        }
        entityObject.put("attributes", attribute);
        jsonObj.put("entity", entityObject);
        String jsonParameter = JSONArray.toJSON(jsonObj).toString();
        ResultDataDTO<String> result = atlasClient.post(entity, jsonParameter);
        if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return "";
        }
        return atlasGuid;
    }

    /**
     * 删除元数据实体
     *
     * @param qualifiedNameList
     * @param parentEntityGuid
     */
    public void deleteMetaData(List<String> qualifiedNameList, String parentEntityGuid) {
        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .notIn("qualified_name", qualifiedNameList)
                .select("atlas_guid")
                .lambda()
                .eq(MetadataMapAtlasPO::getParentAtlasGuid, parentEntityGuid);
        List<String> guidList = (List) metadataMapAtlasMapper.selectObjs(queryWrapper);
        for (String guid : guidList) {
            entityImpl.deleteEntity(guid);
        }
        metadataMapAtlasMapper.delete(queryWrapper);
    }

    /**
     * 新增元数据、元数据配置
     *
     * @param jsonStr
     * @param qualifiedName
     * @param entityTypeEnum
     * @return
     */
    private String addMetaDataConfig(String jsonStr,
                                     String qualifiedName,
                                     EntityTypeEnum entityTypeEnum,
                                     String parentGuid) {
        try {
            log.info("向atlas添加元数据实体.......参数为:", jsonStr);
            //调用atlas添加实例
            ResultDataDTO<String> result = atlasClient.post(entity, jsonStr);
            if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR, atlasClient.newResultEnum(result).getMsg());
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
            //配置表添加数据
            MetadataMapAtlasPO metadataMapAtlasPo = new MetadataMapAtlasPO();
            metadataMapAtlasPo.qualifiedName = qualifiedName;
            metadataMapAtlasPo.atlasGuid = jsonArray.getJSONObject(0).getString("guid");
            metadataMapAtlasPo.type = entityTypeEnum.getValue();
            metadataMapAtlasPo.parentAtlasGuid = parentGuid;
            return metadataMapAtlasMapper.insert(metadataMapAtlasPo) > 0 ? jsonArray.getJSONObject(0).getString("guid") : "";
        } catch (Exception e) {
            log.error("addMetaDataConfig ex:", e);
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, e.getMessage());
        }
    }

    /**
     * 元数据是否已存在
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


    /**
     * 根据数据库名,判断是否可以血缘同步
     *
     * @param dbName
     * @return
     */
    public String whetherSynchronization(String dbName) {
        //获取所有数据源
        ResultEntity<List<DataSourceDTO>> result = userClient.getAllFiDataDataSource();
        if (result.code != ResultEnum.SUCCESS.getCode()) {
            return null;
        }
        //根据数据库筛选
        Optional<DataSourceDTO> first = result.data.stream().filter(e -> dbName.equals(e.conDbname)).findFirst();
        if (!first.isPresent()) {
            return null;
        }
        //暂不支持同步ods血缘
        if (first.get().id == DataSourceConfigEnum.DMP_ODS.getValue()) {
            return null;
        }
        //dw
        else if (first.get().id == DataSourceConfigEnum.DMP_DW.getValue()) {
            ResultEntity<DataSourceDTO> resultDataSource = userClient.getFiDataDataSourceById(DataSourceConfigEnum.DMP_ODS.getValue());
            if (resultDataSource.code != ResultEnum.SUCCESS.getCode() && resultDataSource.data == null) {
                return null;
            }
            return resultDataSource.data.conIp + "_" + resultDataSource.data.conDbname;
        }

        return null;
    }

    /**
     * @param tableNameList
     * @param dtoList
     * @param associateDto
     * @param dbQualifiedName
     * @return
     */
    public List<EntityIdAndTypeDTO> getTableList(List<String> tableNameList,
                                                 List<DataAccessSourceTableDTO> dtoList,
                                                 SourceTableDTO associateDto,
                                                 String dbQualifiedName) {
        List<EntityIdAndTypeDTO> list = new ArrayList<>();

        List<String> tableQualifiedNameList = dtoList.stream()
                .filter(e -> tableNameList.contains(e.tableName.toLowerCase()))
                .map(e -> dbQualifiedName + "_" + e.getId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tableQualifiedNameList)) {
            return list;
        }
        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("qualified_name", tableQualifiedNameList);
        List<MetadataMapAtlasPO> poList = metadataMapAtlasMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            return list;
        }
        for (MetadataMapAtlasPO item : poList) {
            EntityIdAndTypeDTO dto = new EntityIdAndTypeDTO();
            dto.guid = item.atlasGuid;
            dto.typeName = EntityTypeEnum.RDBMS_TABLE.getName();
            list.add(dto);
        }
        List<Integer> associateIdList = associateDto.fieldList.stream().filter(e -> e.associatedDim == true).map(e -> e.getAssociatedDimId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(associateIdList)) {
            return list;
        }
        //关联维度
        List<Integer> collect = associateIdList.stream().distinct().collect(Collectors.toList());
        for (Integer id : collect) {
        }
        return list;
    }


}
