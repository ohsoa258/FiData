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
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceFieldDTO;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.datamanagement.dto.entity.EntityAttributesDTO;
import com.fisk.datamanagement.dto.entity.EntityDTO;
import com.fisk.datamanagement.dto.entity.EntityIdAndTypeDTO;
import com.fisk.datamanagement.dto.entity.EntityTypeDTO;
import com.fisk.datamanagement.dto.process.*;
import com.fisk.datamanagement.dto.relationship.RelationshipDTO;
import com.fisk.datamanagement.entity.MetadataMapAtlasPO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.map.MetaDataMap;
import com.fisk.datamanagement.mapper.MetadataMapAtlasMapper;
import com.fisk.datamanagement.service.impl.EntityImpl;
import com.fisk.datamanagement.synchronization.pushmetadata.IMetaData;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.tableconfig.SourceFieldDTO;
import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;
import com.fisk.datamodel.enums.DataModelTableTypeEnum;
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

    @Value("${atlas.entity}")
    private String entity;
    @Value("${atlas.entityByGuid}")
    private String entityByGuid;
    @Value("${atlas.relationship}")
    private String relationship;

    @Override
    public ResultEnum metaData(MetaDataAttributeDTO data) {
        try {
            log.info("开始推送元数据实时同步，参数:{}", JSON.toJSONString(data));
            BuildMetaDataDTO dto = new BuildMetaDataDTO();
            dto.userId = data.userId == 0 ? userHelper.getLoginUserInfo().id : data.userId;
            dto.data = data.instanceList;
            client.metaData(dto);
            log.info("推送前，meta数据:", JSON.toJSONString(dto));
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
                    List<String> qualifiedNames = new ArrayList<>();
                    for (MetaDataColumnAttributeDTO field : table.columnList) {
                        metaDataField(field, tableGuid);
                        qualifiedNames.add(field.qualifiedName);
                    }
                    //删除
                    deleteMetaData(qualifiedNames, tableGuid);
                    //同步血缘
                    synchronizationTableKinShip(db.name, tableGuid, table.name, table.columnList);
                }
            }
        }
        //更新Redis
        entityImpl.updateRedis();
        return ResultEnum.SUCCESS;
    }

    /**
     * 同步表血缘
     *
     * @param dbName
     * @param tableGuid
     * @param tableName
     */
    public void synchronizationTableKinShip(String dbName,
                                            String tableGuid,
                                            String tableName,
                                            List<MetaDataColumnAttributeDTO> columnList) {
        try {
            String dbQualifiedName = whetherSynchronization(dbName, false);
            if (StringUtils.isEmpty(dbQualifiedName)) {
                return;
            }
            //获取实体详情
            ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + tableGuid);
            if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
                return;
            }

            //获取dw表信息
            ResultEntity<Object> result;

            List<EntityIdAndTypeDTO> inputTableList = new ArrayList<>();

            Optional<SourceTableDTO> first = null;

            List<SourceTableDTO> list;

            ResultEntity<List<DataAccessSourceTableDTO>> odsResult = new ResultEntity<>();

            DataSourceDTO dataSourceInfo = getDataSourceInfo(dbName);
            if (dataSourceInfo == null) {
                return;
            }
            if (dataSourceInfo.id == DataSourceConfigEnum.DMP_DW.getValue()) {
                //获取ods表信息
                odsResult = dataAccessClient.getDataAccessMetaData();
                if (odsResult.code != ResultEnum.SUCCESS.getCode() || CollectionUtils.isEmpty(odsResult.data)) {
                    return;
                }
                result = dataModelClient.getDataModelTable(1);
                if (result.code != ResultEnum.SUCCESS.getCode()) {
                    return;
                }
                //序列化
                list = JSON.parseArray(JSON.toJSONString(result.data), SourceTableDTO.class);
                first = list.stream().filter(e -> tableName.equals(e.tableName)).findFirst();
                if (!first.isPresent()) {
                    return;
                }
                List<String> tableList = first.get().fieldList
                        .stream()
                        .map(e -> e.getSourceTable())
                        .distinct()
                        .collect(Collectors.toList());
                tableList.removeAll(Collections.singleton(null));
                //获取输入参数
                inputTableList = getTableList(tableList, odsResult.data, dbQualifiedName);
                String newDbQualifiedName = whetherSynchronization(dbName, true);
                //获取关联维度
                inputTableList.addAll(associateInputTableList(first.get(), newDbQualifiedName, DataModelTableTypeEnum.DW_DIMENSION));
                if (CollectionUtils.isEmpty(inputTableList)) {
                    return;
                }
            } else if (dataSourceInfo.id == DataSourceConfigEnum.DMP_OLAP.getValue()) {
                result = dataModelClient.getDataModelTable(2);
                if (result.code != ResultEnum.SUCCESS.getCode()) {
                    return;
                }
                //序列化
                list = JSON.parseArray(JSON.toJSONString(result.data), SourceTableDTO.class);
                first = list.stream().filter(e -> tableName.equals(e.tableName)).findFirst();
                if (!first.isPresent()) {
                    return;
                }
                String newDbQualifiedName = whetherSynchronization(dbName, true);
                inputTableList = getDorisTableList(first.get(), dbQualifiedName, newDbQualifiedName);
                //获取关联维度
                inputTableList.addAll(associateInputTableList(first.get(), newDbQualifiedName, DataModelTableTypeEnum.DORIS_DIMENSION));
                if (CollectionUtils.isEmpty(inputTableList)) {
                    return;
                }
            }
            //解析数据
            JSONObject jsonObj = JSON.parseObject(getDetail.data);
            JSONObject entityObject = JSON.parseObject(jsonObj.getString("entity"));
            JSONObject relationShip = JSON.parseObject(entityObject.getString("relationshipAttributes"));
            JSONArray relationShipAttribute = JSON.parseArray(relationShip.getString("outputFromProcesses"));
            //条数为0,则添加process
            if (relationShipAttribute.size() == 0) {
                addProcess(EntityTypeEnum.RDBMS_TABLE, first.get().sqlScript, inputTableList, tableGuid);
            } else {
                for (int i = 0; i < relationShipAttribute.size(); i++) {
                    updateProcess(
                            relationShipAttribute.getJSONObject(i).getString("guid"),
                            inputTableList,
                            EntityTypeEnum.RDBMS_TABLE,
                            first.get().sqlScript,
                            tableGuid);
                }
            }
            //同步字段血缘
            synchronizationColumnKinShip(odsResult.data, first.get(), columnList, dataSourceInfo.id, dbQualifiedName);
        } catch (Exception e) {
            log.error("同步表血缘失败,表guid" + tableGuid + " ex:", e);
            return;
        }
    }

    /**
     * 同步字段血缘
     *
     * @param odsData
     * @param dto
     * @param columnList
     * @param dataSourceId
     * @param dbQualifiedName
     */
    public void synchronizationColumnKinShip(List<DataAccessSourceTableDTO> odsData,
                                             SourceTableDTO dto,
                                             List<MetaDataColumnAttributeDTO> columnList,
                                             int dataSourceId,
                                             String dbQualifiedName) {
        if (CollectionUtils.isEmpty(columnList)) {
            return;
        }

        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("qualified_name", columnList.stream().map(e -> e.qualifiedName).collect(Collectors.toList()));
        List<MetadataMapAtlasPO> poList = metadataMapAtlasMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            return;
        }

        List<EntityIdAndTypeDTO> inputList = new ArrayList<>();
        for (MetadataMapAtlasPO item : poList) {
            inputList.clear();
            //获取实体详情
            ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + item.atlasGuid);
            if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
                continue;
            }

            Optional<MetaDataColumnAttributeDTO> first = columnList.stream().filter(e -> e.qualifiedName.equals(item.qualifiedName)).findFirst();

            Optional<SourceFieldDTO> first1 = dto.fieldList.stream().filter(e -> e.fieldName.equals(first.get().name)).findFirst();
            if (!first1.isPresent()) {
                continue;
            }

            EntityIdAndTypeDTO inputDto = new EntityIdAndTypeDTO();
            inputDto.typeName = EntityTypeEnum.RDBMS_TABLE.getName();

            String fieldName = null;
            if (dataSourceId == DataSourceConfigEnum.DMP_DW.getValue()) {
                Optional<DataAccessSourceTableDTO> first2 = odsData.stream().filter(e -> e.tableName.equals(first1.get().sourceTable)).findFirst();
                if (!first2.isPresent()) {
                    continue;
                }

                Optional<DataAccessSourceFieldDTO> first3 = first2.get().list.stream().filter(e -> e.fieldName.toLowerCase().equals(first1.get().sourceField)).findFirst();
                if (!first3.isPresent()) {
                    continue;
                }

                String columnQualifiedName = dbQualifiedName + "_" + first2.get().id + "_" + first3.get().id;
                QueryWrapper<MetadataMapAtlasPO> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.lambda().eq(MetadataMapAtlasPO::getQualifiedName, columnQualifiedName);
                MetadataMapAtlasPO po = metadataMapAtlasMapper.selectOne(queryWrapper1);
                if (po == null) {
                    continue;
                }
                inputDto.guid = po.atlasGuid;
                inputList.add(inputDto);
                fieldName = first3.get().fieldName;
            } else if (dataSourceId == DataSourceConfigEnum.DMP_OLAP.getValue()) {
                //业务限定
                if (first1.get().attributeType == 0) {
                    String qualifiedName = dbQualifiedName + "_" + DataModelTableTypeEnum.DW_FACT.getValue() + "_" + dto.id + "_" + first1.get().id;
                    QueryWrapper<MetadataMapAtlasPO> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.lambda().eq(MetadataMapAtlasPO::getQualifiedName, qualifiedName);
                    MetadataMapAtlasPO po = metadataMapAtlasMapper.selectOne(queryWrapper1);
                    if (po == null) {
                        continue;
                    }
                    inputDto.guid = po.atlasGuid;
                    inputList.add(inputDto);
                    fieldName = "doris_" + first1.get().fieldName;
                }
                //关联维度
                else if (first1.get().attributeType == 1) {
                    ResultEntity<Object> dataModelTable = dataModelClient.getDataModelTable(1);
                    if (dataModelTable.code != ResultEnum.SUCCESS.getCode()) {
                        continue;
                    }
                    List<SourceTableDTO> result = JSON.parseArray(JSON.toJSONString(dataModelTable.data), SourceTableDTO.class);
                    Optional<SourceTableDTO> first2 = result.stream().filter(e -> dto.tableName.equals(e.tableName)).findFirst();
                    if (!first2.isPresent()) {
                        continue;
                    }

                    Optional<SourceFieldDTO> first3 = first2.get().fieldList.stream().filter(e -> e.fieldName.equals(first1.get().fieldName) && e.associatedDim == true).findFirst();
                    if (!first3.isPresent()) {
                        continue;
                    }

                    String qualifiedName = dbQualifiedName + "_" + DataModelTableTypeEnum.DW_FACT.getValue() + "_" + dto.id + "_" + first3.get().id;
                    QueryWrapper<MetadataMapAtlasPO> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.lambda().eq(MetadataMapAtlasPO::getQualifiedName, qualifiedName);
                    MetadataMapAtlasPO po = metadataMapAtlasMapper.selectOne(queryWrapper1);
                    if (po == null) {
                        continue;
                    }
                    inputDto.guid = po.atlasGuid;
                    inputList.add(inputDto);
                    fieldName = "doris_" + first1.get().fieldName;
                }
                //原子指标
                else {
                    String qualifiedName = dbQualifiedName + "_" + DataModelTableTypeEnum.DW_FACT.getValue() + "_" + dto.id + "_" + first1.get().sourceTable;
                    QueryWrapper<MetadataMapAtlasPO> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.lambda().eq(MetadataMapAtlasPO::getQualifiedName, qualifiedName);
                    MetadataMapAtlasPO po = metadataMapAtlasMapper.selectOne(queryWrapper1);
                    if (po == null) {
                        continue;
                    }
                    inputDto.guid = po.atlasGuid;
                    inputList.add(inputDto);
                    fieldName = first1.get().calculationLogic + "(" + first1.get().sourceField + ")";
                }
            }

            //解析数据
            JSONObject jsonObj = JSON.parseObject(getDetail.data);
            JSONObject entityObject = JSON.parseObject(jsonObj.getString("entity"));
            JSONObject relationShip = JSON.parseObject(entityObject.getString("relationshipAttributes"));
            JSONArray relationShipAttribute = JSON.parseArray(relationShip.getString("outputFromProcesses"));
            //条数为0,则添加process
            if (relationShipAttribute.size() == 0) {
                addProcess(EntityTypeEnum.RDBMS_COLUMN, fieldName, inputList, item.atlasGuid);
            } else {
                for (int i = 0; i < relationShipAttribute.size(); i++) {
                    updateProcess(
                            relationShipAttribute.getJSONObject(i).getString("guid"),
                            inputList,
                            EntityTypeEnum.RDBMS_TABLE,
                            fieldName,
                            item.atlasGuid);
                }
            }
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
     * @param isSkip
     * @param dbName
     * @return
     */
    public String whetherSynchronization(String dbName, boolean isSkip) {
        DataSourceDTO dataSourceInfo = getDataSourceInfo(dbName);
        if (dataSourceInfo == null) {
            return null;
        }
        if (isSkip) {
            return dataSourceInfo.conIp + "_" + dataSourceInfo.conDbname;
        }
        int dataSourceId = 0;
        //暂不支持同步ods血缘
        if (dataSourceInfo.id == DataSourceConfigEnum.DMP_ODS.getValue()) {
            return null;
        }
        //dw
        else if (dataSourceInfo.id == DataSourceConfigEnum.DMP_DW.getValue()) {
            dataSourceId = DataSourceConfigEnum.DMP_ODS.getValue();
        }
        //olap
        else if (dataSourceInfo.id == DataSourceConfigEnum.DMP_OLAP.getValue()) {
            dataSourceId = DataSourceConfigEnum.DMP_DW.getValue();
        }
        ResultEntity<DataSourceDTO> resultDataSource = userClient.getFiDataDataSourceById(dataSourceId);
        if (resultDataSource.code != ResultEnum.SUCCESS.getCode() && resultDataSource.data == null) {
            return null;
        }
        return resultDataSource.data.conIp + "_" + resultDataSource.data.conDbname;
    }

    /**
     * 根据表名,获取数据源配置信息
     *
     * @param dbName
     * @return
     */
    public DataSourceDTO getDataSourceInfo(String dbName) {
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
        return first.get();
    }

    /**
     * 获取ods与dw表血缘输入参数
     *
     * @param tableNameList
     * @param dtoList
     * @param dbQualifiedName
     * @return
     */
    public List<EntityIdAndTypeDTO> getTableList(List<String> tableNameList,
                                                 List<DataAccessSourceTableDTO> dtoList,
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
        return list;
    }

    /**
     * 获取dw与doris表血缘输入参数
     *
     * @param dto
     * @param dbQualifiedName
     * @param newDbQualifiedName
     * @return
     */
    public List<EntityIdAndTypeDTO> getDorisTableList(SourceTableDTO dto, String dbQualifiedName, String newDbQualifiedName) {
        List<EntityIdAndTypeDTO> list = new ArrayList<>();
        //获取dw事实表限定名
        String factQualifiedName = dbQualifiedName + "_" + DataModelTableTypeEnum.DW_FACT.getValue() + "_" + dto.id;
        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataMapAtlasPO::getQualifiedName, factQualifiedName);
        MetadataMapAtlasPO po = metadataMapAtlasMapper.selectOne(queryWrapper);
        if (po == null) {
            return list;
        }
        EntityIdAndTypeDTO data = new EntityIdAndTypeDTO();
        data.guid = po.atlasGuid;
        data.typeName = EntityTypeEnum.RDBMS_TABLE.getName();
        list.add(data);

        //获取关联维度
        List<Integer> associateIdList = dto.fieldList.stream().filter(e -> e.attributeType == 1)
                .map(e -> e.getAssociatedDimId())
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(associateIdList)) {
            return list;
        }
        for (Integer id : associateIdList) {

            String associateQualifiedName = newDbQualifiedName + "_" + DataModelTableTypeEnum.DORIS_DIMENSION.getValue() + "_" + id;
            QueryWrapper<MetadataMapAtlasPO> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.lambda().eq(MetadataMapAtlasPO::getQualifiedName, associateQualifiedName);
            MetadataMapAtlasPO po1 = metadataMapAtlasMapper.selectOne(queryWrapper1);
            if (po1 == null) {
                continue;
            }
            //获取实体详情
            ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + po1.atlasGuid);
            if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
                continue;
            }
            JSONObject jsonObj = JSON.parseObject(getDetail.data);
            JSONObject entityObject = JSON.parseObject(jsonObj.getString("entity"));
            JSONObject relationShip = JSON.parseObject(entityObject.getString("relationshipAttributes"));
            JSONArray relationShipAttribute = JSON.parseArray(relationShip.getString("outputFromProcesses"));
            //条数不为0,则不添加process
            if (relationShipAttribute.size() != 0) {
                continue;
            }
            String associateQualifiedName1 = dbQualifiedName + "_" + DataModelTableTypeEnum.DW_DIMENSION.getValue() + "_" + id;
            QueryWrapper<MetadataMapAtlasPO> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.lambda().eq(MetadataMapAtlasPO::getQualifiedName, associateQualifiedName1);
            MetadataMapAtlasPO po2 = metadataMapAtlasMapper.selectOne(queryWrapper2);
            if (po2 == null) {
                continue;
            }
            List<EntityIdAndTypeDTO> inputTableList = new ArrayList<>();
            EntityIdAndTypeDTO entityIdAndTypeDTO = new EntityIdAndTypeDTO();
            entityIdAndTypeDTO.guid = po2.atlasGuid;
            entityIdAndTypeDTO.typeName = EntityTypeEnum.RDBMS_TABLE.getName();
            inputTableList.add(entityIdAndTypeDTO);
            addProcess(EntityTypeEnum.RDBMS_TABLE, "", inputTableList, po1.atlasGuid);
        }

        return list;
    }

    /**
     * process获取关联维度表
     *
     * @param dto
     * @param dbQualifiedName
     * @param dataModelTableTypeEnum
     * @return
     */
    public List<EntityIdAndTypeDTO> associateInputTableList(SourceTableDTO dto,
                                                            String dbQualifiedName,
                                                            DataModelTableTypeEnum dataModelTableTypeEnum) {
        List<EntityIdAndTypeDTO> inputTableList = new ArrayList<>();
        List<Integer> associateIdList = dto.fieldList.stream().filter(e -> e.associatedDim == true)
                .map(e -> e.getAssociatedDimId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(associateIdList)) {
            return inputTableList;
        }
        List<String> associateDimensionQualifiedNames = associateIdList.stream().map(e -> {
            return dbQualifiedName + "_" + dataModelTableTypeEnum.getValue() + "_" + e;
        }).collect(Collectors.toList());
        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("qualified_name", associateDimensionQualifiedNames).select("atlas_guid");
        List<String> guidList = (List) metadataMapAtlasMapper.selectObjs(queryWrapper);
        for (String guid : guidList) {
            EntityIdAndTypeDTO data = new EntityIdAndTypeDTO();
            data.guid = guid;
            data.typeName = EntityTypeEnum.RDBMS_TABLE.getName();
            inputTableList.add(data);
        }
        return inputTableList;
    }

    /**
     * 添加process
     *
     * @param sql
     * @param tableList
     * @param atlasGuid
     */
    public void addProcess(EntityTypeEnum entityTypeEnum,
                           String sql,
                           List<EntityIdAndTypeDTO> tableList,
                           String atlasGuid) {
        //去除换行符,以及转小写
        sql = sql.replace("\n", "").toLowerCase();
        //组装参数
        EntityDTO entityDTO = new EntityDTO();
        EntityTypeDTO entityTypeDTO = new EntityTypeDTO();
        entityTypeDTO.typeName = EntityTypeEnum.PROCESS.getName();
        EntityAttributesDTO attributesDTO = new EntityAttributesDTO();
        attributesDTO.comment = "";
        attributesDTO.description = sql;
        attributesDTO.owner = "root";
        attributesDTO.qualifiedName = sql + "_" + atlasGuid;
        attributesDTO.contact_info = "root";
        attributesDTO.name = sql;
        //输入参数
        attributesDTO.inputs = tableList;
        //输出参数
        List<EntityIdAndTypeDTO> dtoList = new ArrayList<>();
        EntityIdAndTypeDTO dto = new EntityIdAndTypeDTO();
        dto.typeName = entityTypeEnum.getName();
        dto.guid = atlasGuid;
        dtoList.add(dto);
        attributesDTO.outputs = dtoList;
        entityTypeDTO.attributes = attributesDTO;
        //检验输入和输出参数是否有值
        if (CollectionUtils.isEmpty(attributesDTO.inputs) || CollectionUtils.isEmpty(attributesDTO.outputs)) {
            return;
        }
        entityDTO.entity = entityTypeDTO;
        String jsonParameter = JSONArray.toJSON(entityDTO).toString();
        //调用atlas添加血缘
        ResultDataDTO<String> addResult = atlasClient.post(entity, jsonParameter);
        if (addResult.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return;
        }
    }

    /**
     * 更新process
     *
     * @param processGuid
     * @param inputList
     * @param entityTypeEnum
     * @param sqlScript
     * @param atlasGuid
     */
    public void updateProcess(String processGuid,
                              List<EntityIdAndTypeDTO> inputList,
                              EntityTypeEnum entityTypeEnum,
                              String sqlScript,
                              String atlasGuid) {
        try {
            //获取process详情
            ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + processGuid);
            if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
                return;
            }
            //序列化获取数据
            ProcessDTO dto = JSONObject.parseObject(getDetail.data, ProcessDTO.class);
            //判断process是否已删除
            if (EntityTypeEnum.DELETED.getName().equals(dto.entity.status)) {
                //如果已删除,则重新添加
                addProcess(entityTypeEnum, sqlScript, inputList, atlasGuid);
                return;
            }
            List<String> inputGuidList = dto.entity.attributes.inputs.stream().map(e -> e.getGuid()).collect(Collectors.toList());
            //循环判断是否添加output参数
            for (EntityIdAndTypeDTO item : inputList) {
                if (inputGuidList.contains(item.guid)) {
                    continue;
                }
                //不存在,则添加
                QueryWrapper<MetadataMapAtlasPO> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.lambda().eq(MetadataMapAtlasPO::getAtlasGuid, item.guid);
                MetadataMapAtlasPO po1 = metadataMapAtlasMapper.selectOne(queryWrapper1);
                if (po1 == null) {
                    continue;
                }
                //获取表名
                /*Optional<SourceTableDTO> first = dtoList.stream().filter(e -> e.id == po1.tableId).findFirst();
                if (!first.isPresent()) {
                    continue;
                }*/
                ProcessAttributesPutDTO attributesPutDTO = new ProcessAttributesPutDTO();
                attributesPutDTO.guid = item.guid;
                attributesPutDTO.typeName = EntityTypeEnum.RDBMS_TABLE.getName();
                ProcessUniqueAttributesDTO uniqueAttributes = new ProcessUniqueAttributesDTO();
                uniqueAttributes.qualifiedName = dto.entity.attributes.qualifiedName;
                attributesPutDTO.uniqueAttributes = uniqueAttributes;
                dto.entity.attributes.inputs.add(attributesPutDTO);

                String relationShipGuid = addRelationShip(dto.entity.guid, dto.entity.attributes.qualifiedName, item.guid, po1.qualifiedName);
                if (relationShipGuid == "") {
                    continue;
                }
                ProcessRelationshipAttributesPutDTO inputDTO = new ProcessRelationshipAttributesPutDTO();
                inputDTO.guid = item.guid;
                inputDTO.typeName = EntityTypeEnum.RDBMS_TABLE.getName();
                inputDTO.entityStatus = EntityTypeEnum.ACTIVE.getName();
                //表名
                inputDTO.displayText = "";
                inputDTO.relationshipType = EntityTypeEnum.DATASET_PROCESS_INPUTS.getName();
                //生成的relationShip
                inputDTO.relationshipGuid = relationShipGuid;
                inputDTO.relationshipStatus = EntityTypeEnum.ACTIVE.getName();
                ProcessRelationShipAttributesTypeNameDTO attributesDTO = new ProcessRelationShipAttributesTypeNameDTO();
                attributesDTO.typeName = EntityTypeEnum.DATASET_PROCESS_INPUTS.getName();
                inputDTO.relationshipAttributes = attributesDTO;
                dto.entity.relationshipAttributes.inputs.add(inputDTO);
            }
            //取差集
            List<String> ids = dto.entity.attributes.inputs.stream().map(e -> e.guid).collect(Collectors.toList());
            List<String> ids2 = inputList.stream().map(e -> e.guid).collect(Collectors.toList());
            ids.removeAll(ids2);
            //过滤已删除关联实体
            if (!CollectionUtils.isEmpty(ids)) {
                dto.entity.attributes.inputs = dto.entity.attributes.inputs
                        .stream()
                        .filter(e -> !ids.contains(e.guid))
                        .collect(Collectors.toList());
                dto.entity.relationshipAttributes.inputs = dto.entity.relationshipAttributes.inputs
                        .stream()
                        .filter(e -> !ids.contains(e.guid))
                        .collect(Collectors.toList());
            }
            dto.entity.attributes.name = sqlScript;
            //修改process
            String jsonParameter = JSONArray.toJSON(dto).toString();
            //调用atlas修改实例
            atlasClient.post(entity, jsonParameter);
        } catch (Exception e) {
            log.error("updateProcess ex:", e);
        }
    }

    /**
     * 添加血缘关系连线
     *
     * @param end1Guid
     * @param end1QualifiedName
     * @param end2Guid
     * @param end2QualifiedName
     * @return
     */
    public String addRelationShip(String end1Guid, String end1QualifiedName, String end2Guid, String end2QualifiedName) {
        RelationshipDTO dto = new RelationshipDTO();
        dto.typeName = EntityTypeEnum.DATASET_PROCESS_INPUTS.getName();

        ProcessAttributesPutDTO end1 = new ProcessAttributesPutDTO();
        end1.guid = end1Guid;
        end1.typeName = end1QualifiedName;
        ProcessUniqueAttributesDTO attributesDTO = new ProcessUniqueAttributesDTO();
        attributesDTO.qualifiedName = end1QualifiedName;
        end1.uniqueAttributes = attributesDTO;
        dto.end1 = end1;

        ProcessAttributesPutDTO end2 = new ProcessAttributesPutDTO();
        end2.guid = end2Guid;
        end2.typeName = end2QualifiedName;
        ProcessUniqueAttributesDTO attributesDto2 = new ProcessUniqueAttributesDTO();
        attributesDto2.qualifiedName = end2QualifiedName;
        end2.uniqueAttributes = attributesDto2;
        dto.end2 = end2;

        String jsonParameter = JSONArray.toJSON(dto).toString();
        //调用atlas添加血缘关系连线
        ResultDataDTO<String> addResult = atlasClient.post(relationship, jsonParameter);
        if (addResult.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return "";
        }
        JSONObject data = JSONObject.parseObject(addResult.data);
        return data.getString("guid");
    }


}
