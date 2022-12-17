package com.fisk.datamanagement.synchronization.pushmetadata.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.server.metadata.BusinessMetaDataInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleParameterDTO;
import com.fisk.common.service.metadata.dto.metadata.*;
import com.fisk.common.service.sqlparser.SqlParserUtils;
import com.fisk.common.service.sqlparser.model.TableMetaDataObject;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceFieldDTO;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.datagovernance.client.DataQualityClient;
import com.fisk.datamanagement.dto.classification.ClassificationAddEntityDTO;
import com.fisk.datamanagement.dto.classification.ClassificationDTO;
import com.fisk.datamanagement.dto.classification.ClassificationDelAssociatedEntityDTO;
import com.fisk.datamanagement.dto.entity.*;
import com.fisk.datamanagement.dto.process.*;
import com.fisk.datamanagement.dto.relationship.RelationshipDTO;
import com.fisk.datamanagement.entity.BusinessMetadataConfigPO;
import com.fisk.datamanagement.entity.MetadataMapAtlasPO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.enums.DataTypeEnum;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.map.MetaDataMap;
import com.fisk.datamanagement.map.MetadataMapAtlasMap;
import com.fisk.datamanagement.mapper.BusinessMetadataConfigMapper;
import com.fisk.datamanagement.mapper.MetadataMapAtlasMapper;
import com.fisk.datamanagement.service.impl.ClassificationImpl;
import com.fisk.datamanagement.service.impl.EntityImpl;
import com.fisk.datamanagement.synchronization.pushmetadata.IMetaData;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.customscript.CustomScriptInfoDTO;
import com.fisk.datamodel.dto.customscript.CustomScriptQueryDTO;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderDTO;
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
import java.util.*;
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
    DataAccessClient dataAccessClient;
    @Resource
    DataModelClient dataModelClient;
    @Resource
    DataQualityClient dataQualityClient;

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

    @Override
    public ResultEnum consumeMetaData(List<MetaDataInstanceAttributeDTO> data) {
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
                    String tableGuid = metaDataTable(table, dbGuid, db.name);
                    //新增stg表
                    String stgTableGuid = null;
                    if (!stg.equals(table.getComment())) {
                        stgTableGuid = metaDataStgTable(table, dbGuid, db.name);
                    }
                    if (StringUtils.isEmpty(tableGuid) || CollectionUtils.isEmpty(table.columnList)) {
                        continue;
                    }
                    List<String> qualifiedNames = new ArrayList<>();
                    for (MetaDataColumnAttributeDTO field : table.columnList) {
                        metaDataField(field, tableGuid);
                        qualifiedNames.add(field.qualifiedName);

                        if (!stg.equals(table.getComment())) {
                            //新增stg表字段
                            metaDataStgField(field, stgTableGuid);
                        }
                    }
                    //删除
                    deleteMetaData(qualifiedNames, tableGuid);
                    //同步血缘
                    synchronizationTableKinShip(db.name, tableGuid, table.name.replace(stg_suffix, ""), stgTableGuid); //, table.columnList);
                }
            }
        }
        //更新Redis
        entityImpl.updateRedis();
        return ResultEnum.SUCCESS;
    }

    @Override
    public void synchronousTableBusinessMetaData(BusinessMetaDataInfoDTO dto) {
        associatedBusinessMetaData(null, dto.dbName, dto.tableName);
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
                                            String stgTableGuid) //,List<MetaDataColumnAttributeDTO> columnList
    {
        try {

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

            } else if (dataSourceInfo.id == DataSourceConfigEnum.DMP_DW.getValue()) {
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
        }
    }

    public void synchronizationStgAndCustomScriptTableKinShip(String stgQualifiedName,
                                                              String tableGuid,
                                                              String sqlScript,
                                                              Integer tableId,
                                                              String tableName,
                                                              String conType,
                                                              List<SourceTableDTO> sourceTableDTOList,
                                                              String newDbQualifiedName) {
        //获取实体详情
        ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + tableGuid);
        if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return;
        }

        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataMapAtlasPO::getQualifiedName, stgQualifiedName);
        List<MetadataMapAtlasPO> poList = metadataMapAtlasMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            return;
        }
        List<EntityIdAndTypeDTO> list = new ArrayList<>();
        for (MetadataMapAtlasPO item : poList) {
            EntityIdAndTypeDTO dto = new EntityIdAndTypeDTO();
            dto.guid = item.atlasGuid;
            dto.typeName = EntityTypeEnum.RDBMS_TABLE.getName();
            list.add(dto);
        }

        //解析数据
        JSONObject jsonObj = JSON.parseObject(getDetail.data);
        JSONObject entityObject = JSON.parseObject(jsonObj.getString("entity"));
        JSONObject relationShip = JSON.parseObject(entityObject.getString("relationshipAttributes"));
        JSONArray relationShipAttribute = JSON.parseArray(relationShip.getString("outputFromProcesses"));
        //条数为0,则添加process
        if (relationShipAttribute.size() == 0) {
            addProcess(EntityTypeEnum.RDBMS_TABLE, sqlScript, list, tableGuid, "抽取");
        } else {
            for (int i = 0; i < relationShipAttribute.size(); i++) {
                updateProcess(
                        relationShipAttribute.getJSONObject(i).getString("guid"),
                        null,
                        EntityTypeEnum.RDBMS_TABLE,
                        sqlScript,
                        tableGuid,
                        true);
            }
            addProcess(EntityTypeEnum.RDBMS_TABLE, sqlScript, list, tableGuid, "抽取");
        }
        synchronizationCustomScriptKinShip(tableId, tableName, sourceTableDTOList, tableGuid, conType, newDbQualifiedName, 2);
    }

    /**
     * 同步stg相关血缘
     *
     * @param odsTableGuid
     * @param sqlScript
     * @param stgQualifiedName
     */
    public void synchronizationStgOdsKinShip(String odsTableGuid, String sqlScript, String stgQualifiedName) {
        //获取实体详情
        ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + odsTableGuid);
        if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return;
        }

        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataMapAtlasPO::getQualifiedName, stgQualifiedName);
        List<MetadataMapAtlasPO> poList = metadataMapAtlasMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            return;
        }
        List<EntityIdAndTypeDTO> list = new ArrayList<>();
        for (MetadataMapAtlasPO item : poList) {
            EntityIdAndTypeDTO dto = new EntityIdAndTypeDTO();
            dto.guid = item.atlasGuid;
            dto.typeName = EntityTypeEnum.RDBMS_TABLE.getName();
            list.add(dto);
        }

        //解析数据
        JSONObject jsonObj = JSON.parseObject(getDetail.data);
        JSONObject entityObject = JSON.parseObject(jsonObj.getString("entity"));
        JSONObject relationShip = JSON.parseObject(entityObject.getString("relationshipAttributes"));
        JSONArray relationShipAttribute = JSON.parseArray(relationShip.getString("outputFromProcesses"));
        if (relationShipAttribute.size() == 0) {
            addProcess(EntityTypeEnum.RDBMS_TABLE, sqlScript, list, odsTableGuid, "抽取");
        } else {
            for (int i = 0; i < relationShipAttribute.size(); i++) {
                updateProcess(
                        relationShipAttribute.getJSONObject(i).getString("guid"),
                        list,
                        EntityTypeEnum.RDBMS_TABLE,
                        sqlScript,
                        odsTableGuid,
                        false);
            }
        }
    }

    /**
     * 同步自定义脚本血缘
     *
     * @param tableId
     * @param tableName
     * @param list
     * @param tableGuid
     * @param driveType
     * @param dbQualifiedName
     * @param execType        执行自定义脚本类型：1stg 2事实维度表
     */
    public void synchronizationCustomScriptKinShip(Integer tableId,
                                                   String tableName,
                                                   List<SourceTableDTO> list,
                                                   String tableGuid,
                                                   String driveType,
                                                   String dbQualifiedName,
                                                   Integer execType) {

        CustomScriptQueryDTO dto = new CustomScriptQueryDTO();
        dto.tableId = tableId;
        dto.execType = execType;
        dto.type = 2;
        if (dim_prefix.equals(tableName.substring(0, 4))) {
            dto.type = 1;
        }

        ResultEntity<List<CustomScriptInfoDTO>> listResultEntity = dataModelClient.listCustomScript(dto);
        if (listResultEntity.code != ResultEnum.SUCCESS.getCode()
                || CollectionUtils.isEmpty(listResultEntity.data)) {
            return;
        }

        for (CustomScriptInfoDTO item : listResultEntity.data) {
            //解析sql
            List<TableMetaDataObject> res = SqlParserUtils.sqlDriveConversionName(driveType.toLowerCase(), item.script);
            if (CollectionUtils.isEmpty(res)) {
                return;
            }

            //获取输入表集合
            List<String> collect = res.stream().map(e -> e.getName()).collect(Collectors.toList());

            List<EntityIdAndTypeDTO> inputTableList = getDwTableList(collect, list, dbQualifiedName);
            if (CollectionUtils.isEmpty(inputTableList)) {
                continue;
            }

            addProcess(EntityTypeEnum.RDBMS_TABLE, item.script, inputTableList, tableGuid, item.name);

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
                addProcess(EntityTypeEnum.RDBMS_COLUMN, fieldName, inputList, item.atlasGuid,"");
            } else {
                for (int i = 0; i < relationShipAttribute.size(); i++) {
                    updateProcess(
                            relationShipAttribute.getJSONObject(i).getString("guid"),
                            inputList,
                            EntityTypeEnum.RDBMS_TABLE,
                            fieldName,
                            item.atlasGuid,
                            false);
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
    public String metaDataTable(MetaDataTableAttributeDTO dto, String parentEntityGuid, String dbName) {
        String atlasGuid = getMetaDataConfig(dto.qualifiedName);
        boolean isAdd = false;
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
            atlasGuid = addMetaDataConfig(JSONArray.toJSON(entityDTO).toString(), dto.qualifiedName, EntityTypeEnum.RDBMS_TABLE, parentEntityGuid);
            isAdd = true;
        }
        //同步业务分类
        associatedClassification(atlasGuid, dto.name, dbName, dto.comment);
        //同步业务元数据
        //associatedBusinessMetaData(atlasGuid, dbName, dto.name);
        if (isAdd) {
            return atlasGuid;
        }
        return updateMetaDataEntity(atlasGuid, EntityTypeEnum.RDBMS_TABLE, dto);
    }

    public String metaDataStgTable(MetaDataTableAttributeDTO dto, String parentEntityGuid, String dbName) {
        String atlasGuid = getMetaDataConfig(dto.qualifiedName + stg_prefix);
        //替换前缀
        if (ods_suffix.equals(dto.name.substring(0, 4))) {
            dto.name = dto.name.replace(ods_suffix, stg_suffix);
        } else {
            dto.name = stg_suffix + dto.name;
        }
        dto.qualifiedName = dto.qualifiedName + stg_prefix;
        dto.description = stg;
        boolean isAdd = false;
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
            atlasGuid = addMetaDataConfig(JSONArray.toJSON(entityDTO).toString(), dto.qualifiedName, EntityTypeEnum.RDBMS_TABLE, parentEntityGuid);
            isAdd = true;
        }
        if (isAdd) {
            return atlasGuid;
        }
        return updateMetaDataEntity(atlasGuid, EntityTypeEnum.RDBMS_TABLE, dto);
    }

    /**
     * 同步业务元数据
     *
     * @param atlasGuid
     * @param dbName
     */
    public void associatedBusinessMetaData(String atlasGuid, String dbName, String tableName) {
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
            for (DataAccessSourceTableDTO item : result.data) {
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

    public String metaDataStgField(MetaDataColumnAttributeDTO dto, String parentEntityGuid) {
        String atlasGuid = getMetaDataConfig(dto.qualifiedName + stg_prefix);
        dto.name = stg_suffix + dto.name;
        dto.qualifiedName = dto.qualifiedName + stg_prefix;
        dto.description = stg;
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

    @Override
    public void test() {
        synchronousDisplay();
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
     * 实体关联业务分类
     *
     * @param tableGuid
     * @param tableName
     * @param dbName
     */
    public void associatedClassification(String tableGuid,
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
            if (DataSourceConfigEnum.DMP_ODS.getValue() == sourceData.get().id) {
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
                if (!StringUtils.isEmpty(first.get().appAbbreviation)) {
                    data.typeName = data.typeName + "_" + first.get().appAbbreviation;
                }
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
    public void batchAssociateClassification(String tableGuid, List<AppBusinessInfoDTO> businessAreaList) {
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
                break;
            case RDBMS_DB:
            case RDBMS_TABLE:
                attribute.put("name", dto.name);
                attribute.put("comment", dto.comment);
                attribute.put("contact_info", dto.contact_info);
                attribute.put("description", dto.description);
                attribute.put("displayName", dto.displayName);
                break;
            case RDBMS_COLUMN:
                MetaDataColumnAttributeDTO field = (MetaDataColumnAttributeDTO) dto;
                attribute.put("name", field.name);
                attribute.put("comment", field.comment);
                attribute.put("contact_info", field.contact_info);
                attribute.put("description", field.description);
                attribute.put("data_type", field.dataType);
                attribute.put("displayName", field.displayName);
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
            return "ods";
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
     * 根据库名,获取数据源配置信息
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
                .filter(e -> tableNameList.contains(e.tableName))
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
     * 获取自定义脚本与dw表血缘输入参数
     *
     * @param tableNameList
     * @param dtoList
     * @param dbQualifiedName
     * @return
     */
    public List<EntityIdAndTypeDTO> getDwTableList(List<String> tableNameList,
                                                   List<SourceTableDTO> dtoList,
                                                   String dbQualifiedName) {
        List<EntityIdAndTypeDTO> list = new ArrayList<>();

        List<String> tableQualifiedNameList = dtoList.stream()
                .filter(e -> tableNameList.contains(e.tableName))
                .map(e -> {
                    if (dim_prefix.equals(e.tableName.substring(0, 4))) {
                        return dbQualifiedName + "_1_" + e.getId();
                    }
                    return dbQualifiedName + "_2_" + e.getId();
                })
                .collect(Collectors.toList());

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

    public List<EntityIdAndTypeDTO> getOdsTableList(List<String> tableNameList,
                                                    String dbQualifiedName) {
        List<EntityIdAndTypeDTO> list = new ArrayList<>();

        List<String> tableQualifiedNameList = tableNameList.stream().map(e -> dbQualifiedName + "_" + e).collect(Collectors.toList());
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
    public List<EntityIdAndTypeDTO> getDorisTableList(SourceTableDTO dto,
                                                      String dbQualifiedName,
                                                      String newDbQualifiedName) {
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
            addProcess(EntityTypeEnum.RDBMS_TABLE, "", inputTableList, po1.atlasGuid,"抽取");
        }

        return list;
    }

    /**
     * 关联维度表血缘
     *
     * @param dto
     * @param dbQualifiedName
     * @param dataModelTableTypeEnum
     * @return
     */
    public void associateInputTableList(SourceTableDTO dto,
                                        String dbQualifiedName,
                                        DataModelTableTypeEnum dataModelTableTypeEnum,
                                        String tableGuid) {
        List<EntityIdAndTypeDTO> inputTableList = new ArrayList<>();
        List<Integer> associateIdList = dto.fieldList.stream().filter(e -> e.associatedDim == true)
                .map(e -> e.getAssociatedDimId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(associateIdList)) {
            return;
        }
        List<String> associateDimensionQualifiedNames = associateIdList.stream().map(e -> {
            return dbQualifiedName + "_" + dataModelTableTypeEnum.getValue() + "_" + e;
        }).collect(Collectors.toList());
        for (String item : associateDimensionQualifiedNames) {
            QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("qualified_name", item).select("atlas_guid");
            MetadataMapAtlasPO po = metadataMapAtlasMapper.selectOne(queryWrapper);
            if (po == null) {
                continue;
            }
            EntityIdAndTypeDTO data = new EntityIdAndTypeDTO();
            data.guid = po.atlasGuid;
            data.typeName = EntityTypeEnum.RDBMS_TABLE.getName();
            inputTableList.add(data);

            addProcess(EntityTypeEnum.RDBMS_TABLE, "", null, tableGuid, "关联维度");
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
                              String atlasGuid,
                              boolean delete) {
        try {
            if (delete) {
                entityImpl.deleteEntity(processGuid);
                return;
            }
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
                addProcess(entityTypeEnum, sqlScript, inputList, atlasGuid,"抽取");
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

    /**
     * 设置业务元数据表规则
     *
     * @param dataSourceId
     * @param tableId
     * @param dataType
     */
    public TableRuleInfoDTO setTableRuleInfo(int dataSourceId,
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

    public ResultEnum setBusinessMetaDataAttributeValue(String guid,
                                                        TableRuleInfoDTO tableRuleInfoDTO,
                                                        List<BusinessMetadataConfigPO> poList) {
        EntityAssociatedMetaDataDTO dto = new EntityAssociatedMetaDataDTO();
        dto.guid = guid;
        Map<String, List<BusinessMetadataConfigPO>> collect = poList.stream()
                .collect(Collectors.groupingBy(BusinessMetadataConfigPO::getBusinessMetadataName));
        JSONObject jsonObject = new JSONObject();
        for (String businessMetaDataName : collect.keySet()) {
            JSONObject attributeJson = new JSONObject();
            if ("QualityRules".equals(businessMetaDataName)) {
                //校验规则
                attributeJson.put("ValidationRules", tableRuleInfoDTO.checkRules);
                attributeJson.put("CleaningRules", tableRuleInfoDTO.filterRules);
                attributeJson.put("LifeCycle", tableRuleInfoDTO.lifecycleRules);
                attributeJson.put("AlarmSet", tableRuleInfoDTO.noticeRules);
            } else if ("BusinessDefinition".equals(businessMetaDataName)) {
                attributeJson.put("BusinessName", tableRuleInfoDTO.businessName);
            } else if ("BusinessRules".equals(businessMetaDataName)) {
                attributeJson.put("UpdateRules", tableRuleInfoDTO.updateRules);
                attributeJson.put("TransformationRules", tableRuleInfoDTO.transformationRules == null ? "" : tableRuleInfoDTO.transformationRules);
                attributeJson.put("ComputationalFormula", "");
                attributeJson.put("KnownDataProblem", tableRuleInfoDTO.knownDataProblem == null ? "" : tableRuleInfoDTO.knownDataProblem);
                attributeJson.put("DirectionsForUse", tableRuleInfoDTO.directionsForUse == null ? "" : tableRuleInfoDTO.directionsForUse);
                attributeJson.put("ValidValueConstraint", tableRuleInfoDTO.validValueConstraint);
            } else {
                attributeJson.put("DataResponsibilityDepartment", "");
                attributeJson.put("DataResponsiblePerson", tableRuleInfoDTO.dataResponsiblePerson);
                attributeJson.put("Stakeholders", tableRuleInfoDTO.stakeholders);
            }
            jsonObject.put(businessMetaDataName, attributeJson);
        }
        dto.businessMetaDataAttribute = jsonObject;
        return entityImpl.entityAssociatedMetaData(dto);
    }

}
