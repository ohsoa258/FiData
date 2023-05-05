package com.fisk.datamanagement.synchronization.pushmetadata.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.metadataentitylog.MetaDataeLogEnum;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.server.metadata.BusinessMetaDataInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleParameterDTO;
import com.fisk.common.service.metadata.dto.metadata.*;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.datagovernance.client.DataGovernanceClient;
import com.fisk.datamanagement.dto.classification.ClassificationAddEntityDTO;
import com.fisk.datamanagement.dto.classification.ClassificationDTO;
import com.fisk.datamanagement.dto.classification.ClassificationDelAssociatedEntityDTO;
import com.fisk.datamanagement.dto.entity.EntityAttributesDTO;
import com.fisk.datamanagement.dto.entity.EntityDTO;
import com.fisk.datamanagement.dto.entity.EntityIdAndTypeDTO;
import com.fisk.datamanagement.dto.entity.EntityTypeDTO;
import com.fisk.datamanagement.dto.metadatabusinessmetadatamap.EditMetadataBusinessMetadataMapDTO;
import com.fisk.datamanagement.dto.metadatabusinessmetadatamap.MetadataBusinessMetadataMapDTO;
import com.fisk.datamanagement.dto.metadataentityoperationLog.MetaDataEntityOperationLogDTO;
import com.fisk.datamanagement.dto.process.ProcessAttributesPutDTO;
import com.fisk.datamanagement.dto.process.ProcessUniqueAttributesDTO;
import com.fisk.datamanagement.dto.relationship.RelationshipDTO;
import com.fisk.datamanagement.entity.BusinessMetadataConfigPO;
import com.fisk.datamanagement.entity.MetadataEntityPO;
import com.fisk.datamanagement.entity.MetadataMapAtlasPO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.enums.DataTypeEnum;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.map.MetadataMapAtlasMap;
import com.fisk.datamanagement.mapper.BusinessMetadataConfigMapper;
import com.fisk.datamanagement.mapper.MetadataMapAtlasMapper;
import com.fisk.datamanagement.service.IMetaDataEntityOperationLog;
import com.fisk.datamanagement.service.impl.ClassificationImpl;
import com.fisk.datamanagement.service.impl.EntityImpl;
import com.fisk.datamanagement.service.impl.MetadataBusinessMetadataMapImpl;
import com.fisk.datamanagement.service.impl.MetadataEntityImpl;
import com.fisk.datamanagement.synchronization.pushmetadata.IMetaData;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderDTO;
import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.UserInfoCurrentDTO;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.dto.userinfo.UserDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.task.BuildMetaDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 * 元数据实体同步服务类
 */
@Service
@Slf4j
public class MetaDataImpl implements IMetaData {

    //region  引入
    @Resource
    AtlasClient atlasClient;
    @Resource
    EntityImpl entityImpl;
    @Resource
    MetadataBusinessMetadataMapImpl metadataBusinessMetadataMap;
    @Resource
    ClassificationImpl classification;
    @Resource
    MetadataMapAtlasMapper metadataMapAtlasMapper;
    @Resource
    BusinessMetadataConfigMapper businessMetadataConfigMapper;
    @Resource
    PublishTaskClient client;
    @Resource
    UserClient userClient;

    @Resource
    UserHelper userHelper;
    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    DataModelClient dataModelClient;
    @Resource
    DataGovernanceClient dataQualityClient;
    @Resource
    MetadataEntityImpl metadataEntity;
    @Resource
    private IMetaDataEntityOperationLog operationLog;
    //endregion
    //region 常量
    @Value("${atlas.entity}")
    private String entity;
    @Value("${atlas.entityByGuid}")
    private String entityByGuid;
    @Value("${atlas.relationship}")
    private String relationship;
    private static final String stg_prefix = "_stg";
    private static final String stg_suffix = "stg_";
    private static final String stg = "stg";
    private static final String dim_prefix = "dim_";
    private static final String ods_suffix = "ods_";
//endregion
    @Override
    public ResultEnum metaData(MetaDataAttributeDTO data) {
        try {
            log.info("开始推送元数据实时同步， 参数:{}", JSON.toJSONString(data));
            BuildMetaDataDTO dto = new BuildMetaDataDTO();
            dto.userId = data.userId;
            dto.data = data.instanceList;
            client.metaData(dto);
            log.info("推送前，meta数据:", JSON.toJSONString(dto));
            return ResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("元数据实时同步失败,失败信息:", e);
            return ResultEnum.SAVE_DATA_ERROR;
        }
    }


    //region 元数据实体同步具体实现
    /**
     * 同步元数据对象，主方法
     * @param data  元数据对象实体集合
     * @param currUserName 当前账号
     */
    @Override
    public ResultEnum consumeMetaData(List<MetaDataInstanceAttributeDTO> data,String currUserName) {
        log.info("开始同步元数据***********");
        for (MetaDataInstanceAttributeDTO instance : data) {
            String instanceGuid = metaDataInstance(instance);
            if (StringUtils.isEmpty(instanceGuid) || CollectionUtils.isEmpty(instance.dbList)) {
                continue;
            }
            for (MetaDataDbAttributeDTO db : instance.dbList) {
                String dbGuid = metaDataDb(db, instanceGuid);
                if (StringUtils.isEmpty(dbGuid) || CollectionUtils.isEmpty(db.tableList)) {
                    continue;
                }
                for (MetaDataTableAttributeDTO table : db.tableList) {
                    String tableName = table.name;
                    String tableGuid = metaDataTable(table, dbGuid, db.name);
                    //新增stg表
                    String stgTableGuid = null;
                    if (!stg.equals(table.getComment())) {
                        stgTableGuid = metaDataStgTable(table, dbGuid);
                    }
                    if (StringUtils.isEmpty(tableGuid) || CollectionUtils.isEmpty(table.columnList)) {
                        continue;
                    }
                    List<String> qualifiedNames = new ArrayList<>();
                    for (MetaDataColumnAttributeDTO field : table.columnList) {
                        metaDataField(field, tableGuid,("").equals(currUserName)||currUserName==null?instance.currUserName:currUserName);
                        qualifiedNames.add(field.qualifiedName);
                        if (!stg.equals(table.getComment())) {
                            //新增stg表字段
                            metaDataStgField(field, stgTableGuid);
                        }
                    }
                    //删除
                    deleteMetaData(qualifiedNames, tableGuid);
                    //同步血缘
                    synchronizationTableKinShip(db.name, tableGuid, tableName, stgTableGuid);
                }
            }
        }
        //更新Redis
        //entityImpl.updateRedis();
        return ResultEnum.SUCCESS;
    }

    /**
     * 元数据对象：数据库实例 新增/修改
     *
     * @param dto
     * @return
     */
    private String metaDataInstance(MetaDataInstanceAttributeDTO dto) {
        Integer metadataEntity = this.metadataEntity.getMetadataEntity(dto.qualifiedName);
        if (metadataEntity == null) {
            return this.metadataEntity.addMetadataEntity(dto, EntityTypeEnum.RDBMS_INSTANCE.getName(), "-1").toString();
        }
        return this.metadataEntity.updateMetadataEntity(dto, metadataEntity, EntityTypeEnum.RDBMS_INSTANCE.getName()).toString();
    }
    /**
     * 元数据对参观：数据库 新增/修改
     *
     * @param dto
     * @param parentEntityId
     * @return
     */
    private String metaDataDb(MetaDataDbAttributeDTO dto, String parentEntityId) {
        Integer metadataEntity = this.metadataEntity.getMetadataEntity(dto.qualifiedName);
        if (metadataEntity == null) {
            return this.metadataEntity.addMetadataEntity(dto, EntityTypeEnum.RDBMS_DB.getName(), parentEntityId).toString();
        }

        return this.metadataEntity.updateMetadataEntity(dto, metadataEntity, EntityTypeEnum.RDBMS_DB.getName()).toString();

    }

    /**
     * 元数据对参观：数据表 新增/修改 表新增/修改
     *
     * @param dto
     * @param parentEntityId
     * @return
     */
    private String metaDataTable(MetaDataTableAttributeDTO dto, String parentEntityId, String dbName) {

        Integer metadataEntity = this.metadataEntity.getMetadataEntity(dto.qualifiedName);
        if (metadataEntity == null) {
            metadataEntity = this.metadataEntity.addMetadataEntity(dto, EntityTypeEnum.RDBMS_TABLE.getName(), parentEntityId);
        } else {
            metadataEntity = this.metadataEntity.updateMetadataEntity(dto, metadataEntity, EntityTypeEnum.RDBMS_TABLE.getName());
        }

        if (!"stg".equals(dto.description)) {
            //同步业务分类
            associatedClassification(metadataEntity.toString(), dto.name, dbName, dto.comment);
            //同步业务元数据
            associatedBusinessMetaData(metadataEntity.toString(), dbName, dto.name);
        }

        return metadataEntity.toString();

    }


    /**
     * 实体关联业务分类
     *
     * @param tableGuid
     * @param tableName
     * @param dbName
     */
    private void associatedClassification(String tableGuid,
                                          String tableName,
                                          String dbName,
                                          String comment) {
        try {
            //获取数据源列表
            ResultEntity<List<DataSourceDTO>> allFiDataDataSource = userClient.getAllFiDataDataSource();
            if (allFiDataDataSource.code != ResultEnum.SUCCESS.getCode()) {
                return;
            }
            Optional<DataSourceDTO> sourceData = allFiDataDataSource.data.stream().filter(e -> e.conDbname.equals(dbName)).findFirst();
            if (!sourceData.isPresent()) {
                return;
            }
            ClassificationAddEntityDTO dto = new ClassificationAddEntityDTO();
            dto.entityGuids = new ArrayList<>();
            dto.entityGuids.add(tableGuid);
            ClassificationDTO data = new ClassificationDTO();
            //ods表关联业务数据分类
            if (SourceBusinessTypeEnum.ODS == sourceData.get().sourceBusinessType) {
                //获取接入应用列表
                ResultEntity<List<AppBusinessInfoDTO>> appList = dataAccessClient.getAppList();
                if (appList.code != ResultEnum.SUCCESS.getCode()) {
                    return;
                }
                Optional<AppBusinessInfoDTO> first = appList.data.stream().filter(e -> e.id == Long.parseLong(comment)).findFirst();
                if (!first.isPresent()) {
                    return;
                }
                data.typeName = first.get().name;
            } else if (DataSourceConfigEnum.DMP_DW.getValue() == sourceData.get().id) {
                //获取所有业务域
                ResultEntity<List<AppBusinessInfoDTO>> businessAreaList = dataModelClient.getBusinessAreaList();
                if (businessAreaList.code != ResultEnum.SUCCESS.getCode()) {
                    return;
                }
                //判断是否为公共维度
                if (dim_prefix.equals(tableName.substring(0, 4))) {
                    ResultEntity<DimensionFolderDTO> dimensionFolder = dataModelClient.getDimensionFolderByTableName(tableName);
                    if (dimensionFolder.code != ResultEnum.SUCCESS.getCode()) {
                        return;
                    }
                    //共享维度关联所有分析指标业务分类
                    if (dimensionFolder.data.share) {
                        batchAssociateClassification(tableGuid, businessAreaList.data);
                        return;
                    }
                }
                Optional<AppBusinessInfoDTO> first = businessAreaList.data.stream().filter(e -> e.id == Long.parseLong(comment)).findFirst();
                if (!first.isPresent()) {
                    return;
                }
                data.typeName = first.get().name;
            }
            dto.classification = data;
            classification.classificationAddAssociatedEntity(dto);
        } catch (Exception e) {
            log.error("associatedClassification ex:", e);
        }

    }

    /**
     * 公共维度表批量关联业务分类
     *
     * @param tableGuid
     * @param businessAreaList
     */
    private void batchAssociateClassification(String tableGuid, List<AppBusinessInfoDTO> businessAreaList) {
        for (AppBusinessInfoDTO item : businessAreaList) {
            ClassificationAddEntityDTO dto = new ClassificationAddEntityDTO();
            dto.entityGuids = new ArrayList<>();
            dto.entityGuids.add(tableGuid);
            ClassificationDTO data = new ClassificationDTO();
            data.typeName = item.name;
            dto.classification = data;
            classification.classificationAddAssociatedEntity(dto);
        }
    }

    /**
     * 元数据对参观：临时表 新增/修改 表新增/修改
     * @param dto
     * @param parentEntityId
     * @return
     */
    private String metaDataStgTable(MetaDataTableAttributeDTO dto, String parentEntityId) {
        Integer metadataEntity = this.metadataEntity.getMetadataEntity(dto.qualifiedName + stg_prefix);
        //替换前缀
        if (ods_suffix.equals(dto.name.substring(0, 4))) {
            dto.name = dto.name.replace(ods_suffix, stg_suffix);
        } else {
            dto.name = stg_suffix + dto.name;
        }
        dto.qualifiedName = dto.qualifiedName + stg_prefix;
        dto.description = stg;

        if (metadataEntity == null) {
            return this.metadataEntity.addMetadataEntity(dto, EntityTypeEnum.RDBMS_TABLE.getName(), parentEntityId).toString();
        }

        return this.metadataEntity.updateMetadataEntity(dto, metadataEntity, EntityTypeEnum.RDBMS_TABLE.getName()).toString();
    }
    /**
     * 字段新增/修改
     *
     * @param dto
     * @param parentEntityId
     * @return
     */
    private String metaDataField(MetaDataColumnAttributeDTO dto, String parentEntityId,String createUser) {
        MetaDataEntityOperationLogDTO operationLogDTO = new MetaDataEntityOperationLogDTO();
        Integer metadataEntity = this.metadataEntity.getMetadataEntity(dto.qualifiedName);
        if (metadataEntity == null) {
            operationLogDTO.setOperationType(MetaDataeLogEnum.INSERT_OPERATION.getName());
            operationLogDTO.setBeforeChange("");
            operationLogDTO.setAfterChange(dto.getName());
            operationLogDTO.setCreateTime(LocalDateTime.now());
            operationLogDTO.setCreateUser(createUser);
            operationLogDTO.setMetadataEntityId(parentEntityId);
            operationLog.addOperationLog(operationLogDTO);
            return this.metadataEntity.addMetadataEntity(dto, EntityTypeEnum.RDBMS_COLUMN.getName(), parentEntityId).toString();
        }
        MetadataEntityPO entityPO = this.metadataEntity.query().eq("id", metadataEntity).one();
        if(!entityPO.getName().equals(dto.getName())){
            operationLogDTO.setOperationType(MetaDataeLogEnum.UPDATE_OPERATION.getName());
            operationLogDTO.setBeforeChange(entityPO.getName());
            operationLogDTO.setAfterChange(dto.getName());
            operationLogDTO.setCreateTime(LocalDateTime.now());
            operationLogDTO.setCreateUser(createUser);
            operationLogDTO.setMetadataEntityId(parentEntityId);
            operationLog.addOperationLog(operationLogDTO);
        }
        return this.metadataEntity.updateMetadataEntity(dto, metadataEntity, EntityTypeEnum.RDBMS_COLUMN.getName()).toString();
    }


    private String metaDataStgField(MetaDataColumnAttributeDTO dto, String parentEntityId) {
        Integer metadataEntity = this.metadataEntity.getMetadataEntity(dto.qualifiedName + stg_prefix);
        dto.name = stg_suffix + dto.name;
        dto.qualifiedName = dto.qualifiedName + stg_prefix;
        dto.description = stg;

        if (metadataEntity == null) {
            return this.metadataEntity.addMetadataEntity(dto, EntityTypeEnum.RDBMS_COLUMN.getName(), parentEntityId).toString();
        }

        return this.metadataEntity.updateMetadataEntity(dto, metadataEntity, EntityTypeEnum.RDBMS_COLUMN.getName()).toString();
    }
    /**
     * 删除元数据实体
     *
     * @param qualifiedNameList
     * @param parentEntityGuid
     */
    private void deleteMetaData(List<String> qualifiedNameList, String parentEntityGuid) {
        this.metadataEntity.delMetadataEntity(qualifiedNameList, parentEntityGuid);
    }

    /**
     * 同步表血缘
     *
     * @param dbName
     * @param tableGuid
     * @param tableName
     */
    private void synchronizationTableKinShip(String dbName,
                                            String tableGuid,
                                            String tableName,
                                            String stgTableGuid)
    {
        metadataEntity.synchronizationTableKinShip(dbName, tableGuid, tableName, stgTableGuid);
        /*try {

            //获取实体详情
            ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + stgTableGuid);
            if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
                return;
            }

            String dbQualifiedName = whetherSynchronization(dbName, false);
            if (StringUtils.isEmpty(dbQualifiedName)) {
                return;
            }

            //获取dw表信息
            ResultEntity<Object> result;

            List<EntityIdAndTypeDTO> inputTableList = new ArrayList<>();

            Optional<SourceTableDTO> first = null;

            List<SourceTableDTO> list = null;

            ResultEntity<List<DataAccessSourceTableDTO>> odsResult = new ResultEntity<>();

            DataSourceDTO dataSourceInfo = getDataSourceInfo(dbName);

            boolean delete = false;

            String sqlScript = null;
            if (dataSourceInfo.id == DataSourceConfigEnum.DMP_ODS.getValue()) {
                //同步stg与接入表血缘
                odsResult = dataAccessClient.getDataAccessMetaData();
                if (odsResult.code != ResultEnum.SUCCESS.getCode() || CollectionUtils.isEmpty(odsResult.data)) {
                    return;
                }
                Optional<DataAccessSourceTableDTO> first1 = odsResult.data.stream().filter(e -> e.tableName.equals(tableName)).findFirst();
                if (!first1.isPresent()) {
                    return;
                }

                //解析sql
                List<TableMetaDataObject> res = SqlParserUtils.sqlDriveConversionName(dataSourceInfo.conType.getName().toLowerCase(), first1.get().sqlScript);
                if (CollectionUtils.isEmpty(res)) {
                    return;
                }

                List<String> collect = res.stream().map(e -> e.name).collect(Collectors.toList());
                String dbQualifiedNames = first1.get().appId + "_" + first1.get().appAbbreviation + "_" + first1.get().appId;
                inputTableList = getOdsTableList(collect, dbQualifiedNames);
                if (CollectionUtils.isEmpty(inputTableList)) {
                    return;
                }
                sqlScript = first1.get().sqlScript;

                //添加stg到ods血缘
                String stgQualifiedName = dataSourceInfo.conIp + "_" + dataSourceInfo.conDbname + "_" + first1.get().id + stg_prefix;
                synchronizationStgOdsKinShip(tableGuid, sqlScript, stgQualifiedName);

            }
            else if (dataSourceInfo.id == DataSourceConfigEnum.DMP_DW.getValue()) {
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
                //解析sql脚本
                List<TableMetaDataObject> tableMetaDataObjects = SqlParserUtils.sqlDriveConversionName(dataSourceInfo.conType.getName().toLowerCase(), first.get().sqlScript);
                if (CollectionUtils.isEmpty(tableMetaDataObjects)) {
                    return;
                }

                List<String> tableList = tableMetaDataObjects
                        .stream()
                        .map(e -> e.getName())
                        .distinct()
                        .collect(Collectors.toList());

                //获取输入参数
                inputTableList = getTableList(tableList, odsResult.data, dbQualifiedName);
                if (CollectionUtils.isEmpty(inputTableList)) {
                    return;
                }
                sqlScript = first.get().sqlScript;
                delete = true;

                //stg与事实维度关联以及自定义脚本血缘
                String stgQualifiedName = dataSourceInfo.conIp + "_" + dataSourceInfo.conDbname + "_";
                if (dim_prefix.equals(first.get().tableName.substring(0, 4))) {
                    stgQualifiedName += "1";
                } else {
                    stgQualifiedName += "2";
                }
                stgQualifiedName = stgQualifiedName + "_" + first.get().id + stg_prefix;
                String newDbQualifiedName1 = dataSourceInfo.conIp + "_" + dataSourceInfo.conDbname;
                synchronizationStgAndCustomScriptTableKinShip(stgQualifiedName,
                        tableGuid,
                        sqlScript,
                        (int) first.get().id,
                        first.get().tableName,
                        dataSourceInfo.conType.getName().toLowerCase(),
                        list,
                        newDbQualifiedName1);
            }
            else if (dataSourceInfo.id == DataSourceConfigEnum.DMP_OLAP.getValue()) {
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
                //inputTableList.addAll(associateInputTableList(first.get(), newDbQualifiedName, DataModelTableTypeEnum.DORIS_DIMENSION));
                if (CollectionUtils.isEmpty(inputTableList)) {
                    return;
                }
                sqlScript = first.get().sqlScript;
            }
            //解析数据
            JSONObject jsonObj = JSON.parseObject(getDetail.data);
            JSONObject entityObject = JSON.parseObject(jsonObj.getString("entity"));
            JSONObject relationShip = JSON.parseObject(entityObject.getString("relationshipAttributes"));
            JSONArray relationShipAttribute = JSON.parseArray(relationShip.getString("outputFromProcesses"));
            //条数为0,则添加process
            if (relationShipAttribute.size() == 0) {
                addProcess(EntityTypeEnum.RDBMS_TABLE, sqlScript, inputTableList, stgTableGuid, "抽取");
            } else {
                for (int i = 0; i < relationShipAttribute.size(); i++) {
                    updateProcess(
                            relationShipAttribute.getJSONObject(i).getString("guid"),
                            inputTableList,
                            EntityTypeEnum.RDBMS_TABLE,
                            sqlScript,
                            stgTableGuid,
                            delete);
                }
                if (delete) {
                    addProcess(EntityTypeEnum.RDBMS_TABLE, sqlScript, inputTableList, stgTableGuid, "抽取");
                }
            }
            if (delete) {

                String newDbQualifiedName = dataSourceInfo.conIp + "_" + dataSourceInfo.conDbname;
                //关联维度
                associateInputTableList(first.get(), newDbQualifiedName, DataModelTableTypeEnum.DW_DIMENSION, stgTableGuid);

                //新增自定义脚本
                synchronizationCustomScriptKinShip((int) first.get().id, first.get().tableName, list, stgTableGuid, dataSourceInfo.conType.getName().toLowerCase(), newDbQualifiedName, 1);
            }
            //ods暂不支持字段血缘
            if (dataSourceInfo == null) {
                return;
            }
            //同步字段血缘 TODO 暂不支持字段血缘
            //synchronizationColumnKinShip(odsResult.data, first.get(), columnList, dataSourceInfo.id, dbQualifiedName);
        } catch (Exception e) {
            log.error("同步表血缘失败,表guid" + tableGuid + " ex:", e);
            return;
        }*/
    }
    /**
     * 同步业务元数据
     *
     * @param atlasGuid
     * @param dbName
     */
    private void associatedBusinessMetaData(String atlasGuid, String dbName, String tableName) {
        //获取业务元数据配置信息
        QueryWrapper<BusinessMetadataConfigPO> businessMetadataConfigPoWrapper = new QueryWrapper<>();
        List<BusinessMetadataConfigPO> poList = businessMetadataConfigMapper.selectList(businessMetadataConfigPoWrapper);

        //获取数据源
        DataSourceDTO sourceData = getDataSourceInfo(dbName);
        if (sourceData == null) {
            return;
        }

        Integer tableId = 0;
        //数据类型:1数据接入,2数据建模
        Integer dataType = 0;
        //表类型:1dw维度表,2dw事实表,3doris维度表,4doris指标表
        Integer tableType = 0;
        //ods
        if (sourceData.id == DataSourceConfigEnum.DMP_ODS.getValue()) {
            ResultEntity<List<DataAccessSourceTableDTO>> result = dataAccessClient.getDataAccessMetaData();
            if (result.code != ResultEnum.SUCCESS.getCode() || CollectionUtils.isEmpty(result.data)) {
                return;
            }
            List<SourceTableDTO> list = new ArrayList<>();
            List<DataAccessSourceTableDTO> collect = result.data.stream()
                    .filter(d -> !("sftp").equals(d.driveType))
                    .filter(d -> !("ftp").equals(d.driveType)).collect(Collectors.toList());
            for (DataAccessSourceTableDTO item : collect) {
                SourceTableDTO dto = MetadataMapAtlasMap.INSTANCES.dtoToDto(item);
                list.add(dto);
            }
            Optional<SourceTableDTO> first = list.stream().filter(e -> tableName.equals(e.tableName)).findFirst();
            if (!first.isPresent()) {
                return;
            }
            tableId = (int) first.get().id;
            dataType = DataTypeEnum.DATA_INPUT.getValue();
            tableType = first.get().type;
        }
        //dw
        else if (sourceData.id == DataSourceConfigEnum.DMP_DW.getValue()) {
            ResultEntity<Object> result = dataModelClient.getDataModelTable(1);
            if (result.code != ResultEnum.SUCCESS.getCode()) {
                return;
            }
            List<SourceTableDTO> list = JSON.parseArray(JSON.toJSONString(result.data), SourceTableDTO.class);
            Optional<SourceTableDTO> first = list.stream().filter(e -> tableName.equals(e.tableName)).findFirst();
            if (!first.isPresent()) {
                return;
            }
            tableId = (int) first.get().id;
            dataType = DataTypeEnum.DATA_MODEL.getValue();
            tableType = first.get().type;
        }
        //olap
        else if (sourceData.id == DataSourceConfigEnum.DMP_OLAP.getValue()) {
            ResultEntity<Object> result = dataModelClient.getDataModelTable(2);
            if (result.code != ResultEnum.SUCCESS.getCode()) {
                return;
            }
            List<SourceTableDTO> list = JSON.parseArray(JSON.toJSONString(result.data), SourceTableDTO.class);
            Optional<SourceTableDTO> first = list.stream().filter(e -> tableName.equals(e.tableName)).findFirst();
            if (!first.isPresent()) {
                return;
            }
            tableId = (int) first.get().id;
            dataType = DataTypeEnum.DATA_MODEL.getValue();
            tableType = first.get().type;
        }
        if (StringUtils.isEmpty(atlasGuid)) {
            String qualifiedName = sourceData.conIp + "_" + sourceData.conDbname + "_" + tableType + "_" + tableId;
            if (sourceData.id == DataSourceConfigEnum.DMP_ODS.getValue()) {
                qualifiedName = sourceData.conIp + "_" + sourceData.conDbname + "_" + tableId;
            }
            QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(MetadataMapAtlasPO::getQualifiedName, qualifiedName)
                    .eq(MetadataMapAtlasPO::getType, EntityTypeEnum.RDBMS_TABLE.getValue());
            MetadataMapAtlasPO po = metadataMapAtlasMapper.selectOne(queryWrapper);
            if (po == null) {
                return;
            }
            atlasGuid = po.atlasGuid;
        }
        TableRuleInfoDTO tableRuleInfo = setTableRuleInfo(sourceData.id, tableId, dataType, tableType);
        setBusinessMetaDataAttributeValue(atlasGuid, tableRuleInfo, poList);
    }


    private ResultEnum setBusinessMetaDataAttributeValue(String guid,
                                                         TableRuleInfoDTO tableRuleInfoDTO,
                                                         List<BusinessMetadataConfigPO> poList) {

        //分组获取业务元数据类别
        Map<String, List<BusinessMetadataConfigPO>> collect = poList.stream()
                .collect(Collectors.groupingBy(BusinessMetadataConfigPO::getBusinessMetadataName));
        if (CollectionUtils.isEmpty(collect)) {
            throw new FkException(ResultEnum.DATA_SUBMIT_ERROR);
        }

        Integer metadataEntityId = Integer.parseInt(guid);
        List<MetadataBusinessMetadataMapDTO> list = new ArrayList<>();

        for (String businessMetaDataName : collect.keySet()) {
            List<BusinessMetadataConfigPO> list1 = collect.get(businessMetaDataName);
            switch (businessMetaDataName) {
                case "QualityRules":
                    list.addAll(metadataBusinessMetadataMap.setQualityRules(list1, metadataEntityId, tableRuleInfoDTO));
                    break;
                case "BusinessDefinition":
                    list.addAll(metadataBusinessMetadataMap.setBusinessDefinition(list1, metadataEntityId, tableRuleInfoDTO));
                    break;
                case "BusinessRules":
                    list.addAll(metadataBusinessMetadataMap.setBusinessRules(list1, metadataEntityId, tableRuleInfoDTO));
                    break;
                case "ManagementRules":
                    list.addAll(metadataBusinessMetadataMap.setManagementRules(list1, metadataEntityId, tableRuleInfoDTO));
                    break;
                default:
                    break;
            }
        }

        EditMetadataBusinessMetadataMapDTO data = new EditMetadataBusinessMetadataMapDTO();
        data.metadataEntityId = Integer.parseInt(guid);
        data.list = list;

        return metadataBusinessMetadataMap.operationMetadataBusinessMetadataMap(data);
    }

    /**
     * 设置业务元数据表规则
     *
     * @param dataSourceId
     * @param tableId
     * @param dataType
     */
    private TableRuleInfoDTO setTableRuleInfo(int dataSourceId,
                                              int tableId,
                                              int dataType,
                                              int tableType) {
        TableRuleInfoDTO dto = new TableRuleInfoDTO();
        ResultEntity<TableRuleInfoDTO> tableRule = dataQualityClient.getTableRuleList(dataSourceId, String.valueOf(tableId), tableType);
        if (tableRule.code == ResultEnum.SUCCESS.getCode()) {
            dto = tableRule.data;
        }
        TableRuleParameterDTO parameter = new TableRuleParameterDTO();
        parameter.type = tableType;
        parameter.tableId = tableId;
        ResultEntity<TableRuleInfoDTO> result = new ResultEntity<>();
        TableRuleInfoDTO data = result.data;
        //数仓建模
        if (dataType == DataTypeEnum.DATA_MODEL.getValue()) {
            result = dataModelClient.setTableRule(parameter);
        }
        //数据接入
        else if (dataType == DataTypeEnum.DATA_INPUT.getValue()) {
            result = dataAccessClient.buildTableRuleInfo(parameter);
        }
        if (result.code == ResultEnum.SUCCESS.getCode()) {
            if (StringUtils.isEmpty(dto.name)) {
                dto = result.data;
            } else {
                dto.businessName = result.data.businessName;
                dto.dataResponsiblePerson = result.data.dataResponsiblePerson;
                if (!CollectionUtils.isEmpty(dto.fieldRules)) {
                    dto.fieldRules.stream().map(e -> {
                        e.businessName = data.businessName;
                        e.dataResponsiblePerson = data.dataResponsiblePerson;
                        return e;
                    });
                }
            }
        }
        return dto;
    }

    /**
     * 根据库名,获取数据源配置信息
     *
     * @param dbName
     * @return
     */
    private DataSourceDTO getDataSourceInfo(String dbName) {
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

    //endregion

    //region Controlor 调用方法

    @Override
    public ResultEnum addFiledAndUpdateFiled(List<MetaDataInstanceAttributeDTO> data) {
        log.info("开始同步元数据***********");
        for (MetaDataInstanceAttributeDTO instance : data) {
            String instanceGuid = metaDataInstance(instance);
            if (StringUtils.isEmpty(instanceGuid) || CollectionUtils.isEmpty(instance.dbList)) {
                continue;
            }
            for (MetaDataDbAttributeDTO db : instance.dbList) {
                String dbGuid = metaDataDb(db, instanceGuid);
                if (StringUtils.isEmpty(dbGuid) || CollectionUtils.isEmpty(db.tableList)) {
                    continue;
                }
                for (MetaDataTableAttributeDTO table : db.tableList) {
                    String tableName = table.name;
                    String tableGuid = metaDataTable(table, dbGuid, db.name);
                    //新增stg表
                    String stgTableGuid = null;
                    if (!stg.equals(table.getComment())) {
                        stgTableGuid = metaDataStgTable(table, dbGuid);
                    }
                    if (StringUtils.isEmpty(tableGuid) || CollectionUtils.isEmpty(table.columnList)) {
                        continue;
                    }
                    List<String> qualifiedNames = new ArrayList<>();
                    for (MetaDataColumnAttributeDTO field : table.columnList) {
                        metaDataField(field, tableGuid,instance.owner);
                        qualifiedNames.add(field.qualifiedName);
                        if (!stg.equals(table.getComment())) {
                            //新增stg表字段
                            metaDataStgField(field, stgTableGuid);
                        }
                    }
                }
            }
        }
        //更新Redis
        //entityImpl.updateRedis();
        return ResultEnum.SUCCESS;
    }

    @Override
    public void synchronousTableBusinessMetaData(BusinessMetaDataInfoDTO dto) {
        associatedBusinessMetaData(null, dto.dbName, dto.tableName);
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

            //删除表业务分类关联
            ClassificationDelAssociatedEntityDTO associatedEntityDto = new ClassificationDelAssociatedEntityDTO();
            associatedEntityDto.classificationName = dto.classifications;
            associatedEntityDto.entityGuid = po.atlasGuid;
            ResultEnum delResult = classification.classificationDelAssociatedEntity(associatedEntityDto);
            if (delResult.getCode() != ResultEnum.SUCCESS.getCode()) {
                continue;
            }

            //删除元数据实体
            ResultEnum resultEnum = entityImpl.deleteEntity(po.atlasGuid);
            if (resultEnum.getCode() != ResultEnum.SUCCESS.getCode()) {
                continue;
            }

            //删除元数据配置
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
    private void delete(String atlasGuid) {
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

    public void synchronousDisplay() {

        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        List<MetadataMapAtlasPO> list = metadataMapAtlasMapper.selectList(queryWrapper);
        for (MetadataMapAtlasPO item : list) {
            if (item.type == 7) {
                continue;
            }
            ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + item.atlasGuid);
            if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
                return;
            }
            //解析数据
            JSONObject jsonObj = JSON.parseObject(getDetail.data);
            JSONObject entityObject = JSON.parseObject(jsonObj.getString("entity"));
            JSONObject attribute = JSON.parseObject(entityObject.getString("attributes"));

            attribute.put("displayName", attribute.get("name"));

            entityObject.put("attributes", attribute);
            jsonObj.put("entity", entityObject);
            String jsonParameter = JSONArray.toJSON(jsonObj).toString();
            ResultDataDTO<String> result = atlasClient.post(entity, jsonParameter);
            if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
                return;
            }
        }


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
                attribute.put("displayName", data.displayName);
                attribute.put("owner", dto.owner);
                break;
            case RDBMS_DB:
            case RDBMS_TABLE:
                attribute.put("name", dto.name);
                attribute.put("comment", dto.comment);
                attribute.put("contact_info", dto.contact_info);
                attribute.put("description", dto.description);
                attribute.put("displayName", dto.displayName);
                attribute.put("owner", dto.owner);
                break;
            case RDBMS_COLUMN:
                MetaDataColumnAttributeDTO field = (MetaDataColumnAttributeDTO) dto;
                attribute.put("name", field.name);
                attribute.put("comment", field.comment);
                attribute.put("contact_info", field.contact_info);
                attribute.put("description", field.description);
                attribute.put("data_type", field.dataType);
                attribute.put("displayName", field.displayName);
                attribute.put("owner", field.owner);
                break;
            default:
                break;
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
     * 添加process
     *
     * @param sql
     * @param tableList
     * @param atlasGuid
     */
    public void addProcess(EntityTypeEnum entityTypeEnum,
                           String sql,
                           List<EntityIdAndTypeDTO> tableList,
                           String atlasGuid,
                           String processName) {
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
        attributesDTO.qualifiedName = sql + "_" + UUID.randomUUID().toString();
        attributesDTO.contact_info = "root";
        attributesDTO.name = processName;
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
     * 添加血缘关系连线
     *
     * @param end1Guid
     * @param end1QualifiedName
     * @param end2Guid
     * @param end2QualifiedName
     * @return
     */
    public String addRelationShip(String end1Guid,
                                  String end1QualifiedName,
                                  String end2Guid,
                                  String end2QualifiedName) {
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
