package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.metadata.dto.metadata.MetaDataBaseAttributeDTO;
import com.fisk.common.service.sqlparser.SqlParserUtils;
import com.fisk.common.service.sqlparser.model.TableMetaDataObject;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.datamanagement.dto.entity.EntityTreeDTO;
import com.fisk.datamanagement.dto.lineage.LineAgeDTO;
import com.fisk.datamanagement.dto.lineage.LineAgeRelationsDTO;
import com.fisk.datamanagement.dto.lineagemaprelation.LineageMapRelationDTO;
import com.fisk.datamanagement.entity.LineageMapRelationPO;
import com.fisk.datamanagement.entity.MetadataEntityPO;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.mapper.LineageMapRelationMapper;
import com.fisk.datamanagement.mapper.MetadataEntityMapper;
import com.fisk.datamanagement.service.IMetadataEntity;
import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class MetadataEntityImpl
        extends ServiceImpl<MetadataEntityMapper, MetadataEntityPO>
        implements IMetadataEntity {

    private static final String stg = "stg";
    private static final String stg_prefix = "_stg";
    private static final String processName = "抽取";

    @Resource
    MetadataEntityTypeImpl metadataEntityType;
    @Resource
    MetadataAttributeImpl metadataAttribute;
    @Resource
    MetadataLineageMapImpl metadataLineageMap;
    @Resource
    LineageMapRelationImpl lineageMapRelation;
    @Resource
    MetadataEntityImpl metadataEntity;

    @Resource
    MetadataEntityMapper metadataEntityMapper;
    @Resource
    LineageMapRelationMapper lineageMapRelationMapper;

    @Resource
    UserClient userClient;
    @Resource
    DataAccessClient dataAccessClient;

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

        List<MetadataEntityPO> poList = metadataEntityMapper.selectMetadataEntity(EntityTypeEnum.PROCESS.getValue());
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

    /**
     * 获取元数据详情
     *
     * @param entityId
     * @return
     */
    public JSONObject getMetadataEntityDetails(String entityId) {

        MetadataEntityPO one = this.query().eq("id", entityId).one();
        if (one == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        Map infoMap = metadataAttribute.setMedataAttribute(Integer.parseInt(entityId), 0);
        infoMap.put("name", one.name);
        infoMap.put("description", one.description);
        infoMap.put("owner", one.owner);
        infoMap.put("qualifiedName", one.qualifiedName);
        infoMap.put("displayName", one.displayName);

        getEntityRelation(one, infoMap);

        Map map2 = new HashMap();
        map2.put("attributes", infoMap);

        map2.put("relationshipAttributes", getRelationshipAttributes(one));

        //map2.put("classifications", "");

        getMetadataCustom(map2, Integer.parseInt(entityId));

        Map map = new HashMap();
        map.put("entity", map2);

        return JSONObject.parseObject(JSONObject.toJSONString(map));

    }

    /**
     * 获取实体上下级关系
     *
     * @param po
     * @param map
     * @return
     */
    public Map getEntityRelation(MetadataEntityPO po, Map map) {

        Map attributeMap = new HashMap();

        EntityTypeEnum value = EntityTypeEnum.getValue(po.typeId);
        switch (value) {
            case RDBMS_INSTANCE:
                map.put("databases", getEntityRelationInfo((int) po.id, value, "parent_id"));
                break;
            case RDBMS_DB:
                map.put("instance", getEntityRelationInfo((int) po.id, value, "id").get(0));
                map.put("tables", getEntityRelationInfo((int) po.id, value, "parent_id"));
                break;
            case RDBMS_TABLE:
                map.put("db", getEntityRelationInfo((int) po.id, value, "id").get(0));
                map.put("columns", getEntityRelationInfo((int) po.id, value, "parent_id"));
                break;
            case RDBMS_COLUMN:
                map.put("table", getEntityRelationInfo((int) po.id, value, "id").get(0));
                break;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }

        return attributeMap;

    }

    /**
     * 获取实体上下级关系id或类型名称
     *
     * @param entityId
     * @param entityTypeEnum
     * @param fileName
     * @return
     */
    public List<Map> getEntityRelationInfo(Integer entityId, EntityTypeEnum entityTypeEnum, String fileName) {

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

    /**
     * 获取元数据自定义属性
     *
     * @param map
     * @param entityId
     * @return
     */
    public Map getMetadataCustom(Map map, Integer entityId) {
        Map map1 = metadataAttribute.setMedataAttribute(entityId, 1);

        if (map1.size() == 0) {
            return map;
        }
        map.put("customAttributes", map1);

        return map;
    }


    public Map getRelationshipAttributes(MetadataEntityPO po) {
        Map attributeMap = new HashMap();

        EntityTypeEnum value = EntityTypeEnum.getValue(po.typeId);
        switch (value) {
            case RDBMS_INSTANCE:
                attributeMap.put("databases", getEntityRelationAttributesInfo((int) po.id, value, "parent_id"));
                break;
            case RDBMS_DB:
                attributeMap.put("instance", getEntityRelationAttributesInfo((int) po.id, value, "id").get(0));
                attributeMap.put("tables", getEntityRelationAttributesInfo((int) po.id, value, "parent_id"));
                break;
            case RDBMS_TABLE:
                attributeMap.put("db", getEntityRelationAttributesInfo((int) po.id, value, "id").get(0));
                attributeMap.put("columns", getEntityRelationAttributesInfo((int) po.id, value, "parent_id"));
                break;
            case RDBMS_COLUMN:
                attributeMap.put("table", getEntityRelationAttributesInfo((int) po.id, value, "id").get(0));
                break;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }

        return attributeMap;
    }

    /**
     * 获取实体上下级关系id或类型名称
     *
     * @param entityId
     * @param entityTypeEnum
     * @param fileName
     * @return
     */
    public List<Map> getEntityRelationAttributesInfo(Integer entityId, EntityTypeEnum entityTypeEnum, String fileName) {

        List<MetadataEntityPO> list = this.query().ne("description", stg).eq(fileName, entityId).list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<Map> mapList = new ArrayList<>();
        for (MetadataEntityPO item : list) {

            Map infoMap = new HashMap();
            infoMap.put("guid", item.id);
            infoMap.put("displayText", item.displayName);
            infoMap.put("entityStatus", "ACTIVE");
            infoMap.put("typeName", entityTypeEnum.getName());
            mapList.add(infoMap);
        }

        return mapList;
    }

    public void synchronizationTableKinShip(String dbName,
                                            String tableGuid,
                                            String tableName,
                                            String stgTableGuid) {
        String dbQualifiedName = whetherSynchronization(dbName, false);
        if (StringUtils.isEmpty(dbQualifiedName)) {
            return;
        }

        //获取dw表信息
        ResultEntity<Object> result;

        List<Long> fromEntityIdList = new ArrayList<>();

        Optional<SourceTableDTO> first = null;

        List<SourceTableDTO> list = null;

        ResultEntity<List<DataAccessSourceTableDTO>> odsResult = new ResultEntity<>();

        DataSourceDTO dataSourceInfo = getDataSourceInfo(dbName);

        boolean delete = false;

        String sqlScript = null;

        if (dataSourceInfo.sourceBusinessType == SourceBusinessTypeEnum.ODS) {
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
            //解析表名集合
            List<String> collect = res.stream().map(e -> e.name).collect(Collectors.toList());
            String dbQualifiedNames = first1.get().appId + "_" + first1.get().appAbbreviation + "_" + first1.get().dataSourceId;

            fromEntityIdList = getOdsTableList(collect, dbQualifiedNames);

            if (CollectionUtils.isEmpty(fromEntityIdList)) {
                return;
            }

            sqlScript = first1.get().sqlScript;

            //添加stg到ods血缘
            String stgQualifiedName = dataSourceInfo.conIp + "_" + dataSourceInfo.conDbname + "_" + first1.get().id + stg_prefix;
            synchronizationStgOdsKinShip(tableGuid, sqlScript, stgQualifiedName);
        }
        addProcess(sqlScript, fromEntityIdList, stgTableGuid, "抽取");

    }

    public String whetherSynchronization(String dbName, boolean isSkip) {
        DataSourceDTO dataSourceInfo = getDataSourceInfo(dbName);
        if (dataSourceInfo == null) {
            return null;
        }
        if (isSkip) {
            return dataSourceInfo.conIp + "_" + dataSourceInfo.conDbname;
        }
        int dataSourceId = 0;
        //同步ods血缘
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

    public List<Long> getOdsTableList(List<String> tableNameList, String dbQualifiedName) {

        List<String> tableQualifiedNameList = tableNameList.stream().map(e -> dbQualifiedName + "_" + e).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tableQualifiedNameList)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        QueryWrapper<MetadataEntityPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("qualified_name", tableQualifiedNameList);
        List<MetadataEntityPO> poList = metadataEntityMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        return (List) poList.stream().map(e -> e.getId()).collect(Collectors.toList());
    }

    /**
     * 同步stg相关血缘
     *
     * @param odsTableGuid
     * @param sqlScript
     * @param stgQualifiedName
     */
    public void synchronizationStgOdsKinShip(String odsTableGuid, String sqlScript, String stgQualifiedName) {

        QueryWrapper<MetadataEntityPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataEntityPO::getQualifiedName, stgQualifiedName);
        List<MetadataEntityPO> poList = metadataEntityMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            return;
        }

        List<Long> collect = poList.stream().map(e -> e.getId()).collect(Collectors.toList());

        addProcess(sqlScript, collect, odsTableGuid, processName);

        /*if (relationShipAttribute.size() == 0) {
            //addProcess(EntityTypeEnum.RDBMS_TABLE, sqlScript, list, odsTableGuid, "抽取");
        } else {

        }*/

    }

    public void addProcess(String sql,
                           List<Long> tableList,
                           String atlasGuid,
                           String processName) {
        //去除换行符,以及转小写
        sql = sql.replace("\n", "").toLowerCase();

        //新增process
        MetadataEntityPO po = new MetadataEntityPO();
        po.name = processName;
        po.description = sql;
        po.displayName = processName;
        po.typeId = EntityTypeEnum.PROCESS.getValue();
        po.qualifiedName = sql;

        Integer flat = metadataEntityMapper.insert(po);
        if (flat == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        List<LineageMapRelationDTO> dtoList = new ArrayList<>();
        for (Long item : tableList) {
            LineageMapRelationDTO data = new LineageMapRelationDTO();
            data.fromEntityId = item.intValue();
            data.toEntityId = Integer.parseInt(atlasGuid);
            data.metadataEntityId = (int) po.id;
            dtoList.add(data);
        }
        //新增process关联关系
        lineageMapRelation.addLineageMapRelation(dtoList);
    }


    public LineAgeDTO getMetaDataKinship(String guid) {

        //获取process-fromEntityId
        List<LineageMapRelationPO> list = lineageMapRelation.query()
                .eq("to_entity_id", guid)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return new LineAgeDTO();
        }

        LineAgeDTO dto = new LineAgeDTO();
        dto.relations = new ArrayList<>();
        dto.guidEntityMap = new ArrayList<>();

        List<LineageMapRelationPO> poList = new ArrayList<>();
        poList.addAll(list);

        for (LineageMapRelationPO po : list) {
            boolean flat = true;
            List<Integer> ids = new ArrayList<>();
            ids.add(po.fromEntityId);
            while (flat) {
                List<LineageMapRelationPO> list1 = lineageMapRelation.query().in("to_entity_id", ids).list();
                if (CollectionUtils.isEmpty(list1)) {
                    flat = false;
                    break;
                }
                poList.addAll(list1);
                ids.clear();
                ids.addAll(list1.stream().map(e -> e.fromEntityId).collect(Collectors.toList()));
            }
        }

        //获取process-toEntityId
        List<LineageMapRelationPO> list2 = lineageMapRelation.query()
                .eq("from_entity_id", guid)
                .list();
        if (!CollectionUtils.isEmpty(list2)) {
            poList.addAll(list2);
            for (LineageMapRelationPO po : list2) {
                boolean flat = true;
                List<Integer> ids = new ArrayList<>();
                ids.add(po.toEntityId);
                while (flat) {
                    List<LineageMapRelationPO> list1 = lineageMapRelation.query().in("from_entity_id", ids).list();
                    if (CollectionUtils.isEmpty(list1)) {
                        flat = false;
                        break;
                    }
                    poList.addAll(list1);
                    ids.clear();
                    ids.addAll(list1.stream().map(e -> e.fromEntityId).collect(Collectors.toList()));
                }
            }
        }


        if (CollectionUtils.isEmpty(poList)) {
            return dto;
        }

        List<Integer> idList = new ArrayList<>();
        for (LineageMapRelationPO po : poList) {
            idList.add(po.metadataEntityId);
            idList.add(po.fromEntityId);
            idList.add(po.toEntityId);
            addLine(po, dto);
        }

        List<Integer> collect = idList.stream().distinct().collect(Collectors.toList());
        List<MetadataEntityPO> id = metadataEntity.query().in("id", collect).list();
        for (MetadataEntityPO item : id) {
            dto.guidEntityMap.add(addJsonObject(item));
        }

        return dto;
    }

    public LineAgeDTO addLine(LineageMapRelationPO po, LineAgeDTO dataList) {

        Optional<LineAgeRelationsDTO> first1 = dataList.relations.stream()
                .filter(e -> e.fromEntityId.equals(String.valueOf(po.fromEntityId)) && e.toEntityId.equals(String.valueOf(po.metadataEntityId)))
                .findFirst();
        if (!first1.isPresent()) {
            dataList.relations.add(addRelation(po.fromEntityId, po.metadataEntityId));
        }

        Optional<LineAgeRelationsDTO> first = dataList.relations.stream()
                .filter(e -> e.fromEntityId.equals(String.valueOf(po.metadataEntityId)) && e.toEntityId.equals(String.valueOf(po.toEntityId)))
                .findFirst();
        if (!first.isPresent()) {
            dataList.relations.add(addRelation(po.metadataEntityId, po.toEntityId));
        }

        return dataList;
    }

    public JSONObject addJsonObject(MetadataEntityPO po) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("guid", po.id);
        jsonObject.put("typeName", EntityTypeEnum.getValue(po.typeId).getName());
        jsonObject.put("status", "ACTIVE");
        jsonObject.put("displayText", po.displayName);

        JSONObject attribute = new JSONObject();
        attribute.put("description", po.description);
        attribute.put("name", po.name);
        attribute.put("qualifiedName", po.displayName);
        jsonObject.put("attributes", attribute);

        return jsonObject;
    }

    public LineAgeRelationsDTO addRelation(Integer fromEntityId, Integer toEntityId) {
        LineAgeRelationsDTO line = new LineAgeRelationsDTO();
        line.fromEntityId = String.valueOf(fromEntityId);
        line.toEntityId = String.valueOf(toEntityId);

        return line;
    }


}
