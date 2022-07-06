package com.fisk.datamanagement.synchronization.fidata;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleParameterDTO;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.datagovernance.client.DataQualityClient;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.datamanagement.dto.entity.*;
import com.fisk.datamanagement.dto.metadatamapatlas.UpdateMetadataMapAtlasDTO;
import com.fisk.datamanagement.entity.BusinessMetadataConfigPO;
import com.fisk.datamanagement.entity.MetadataMapAtlasPO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.enums.DataTypeEnum;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.map.MetadataMapAtlasMap;
import com.fisk.datamanagement.mapper.BusinessMetadataConfigMapper;
import com.fisk.datamanagement.mapper.MetadataMapAtlasMapper;
import com.fisk.datamanagement.service.impl.EntityImpl;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.tableconfig.SourceFieldDTO;
import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;
import com.fisk.datamodel.enums.DataModelTableTypeEnum;
import com.fisk.datamodel.enums.FactAttributeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Component
@Slf4j
public class SynchronizationData {

    @Resource
    DataModelClient client;
    @Resource
    AtlasClient atlasClient;
    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    DataQualityClient dataQualityClient;
    @Resource
    MetadataMapAtlasMapper metadataMapAtlasMapper;
    @Resource
    BusinessMetadataConfigMapper businessMetadataConfigMapper;
    @Resource
    EntityImpl entityImpl;
    @Resource
    private RedisTemplate redisTemplate;

    @Value("${atlas.entity}")
    private String entity;
    @Value("${atlas.entityByGuid}")
    private String entityByGuid;

    @Value("${fidata.database.name}")
    private String fiDataName;
    @Value("${fidata.database.hostname}")
    private String fiDataHostName;
    @Value("${fidata.database.port}")
    private String fiDataPort;
    @Value("${fidata.database.platform}")
    private String fiDataPlatform;
    @Value("${fidata.database.protocol}")
    private String fiDataProtocol;
    @Value("${fidata.database.rdbmstype}")
    private String fiDataRdbmsType;
    @Value("${fidata.database.username}")
    private String fiDataUserName;
    @Value("${fidata.database.password}")
    private String fiDataPassword;
    @Value("${fidata.database.db}")
    private String db;

    public void synchronizationPgData()
    {
        try {
            //同步实例
            synchronizationInstance();
            //同步库
            synchronizationDb();
            //同步ods
            synchronizationOds();
            //同步dw
            synchronizationDw();
            //同步doris
            synchronizationDoris();
            //同步宽表
            synchronizationWideTable();
            //同步redis中数据
            entityImpl.getEntityList();
        }
        catch (Exception e)
        {
            log.error("synchronizationPgData ex:",e);
        }
    }

    /**
     * 同步实例
     */
    public void synchronizationInstance()
    {
        try {
            QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(MetadataMapAtlasPO::getType, EntityTypeEnum.RDBMS_INSTANCE.getValue());
            String[] hostName = fiDataHostName.split(",");
            String[] hostPort=fiDataPort.split(",");
            for (int i=0;i<hostName.length;i++)
            {
                //查询实例qualifiedName是否已存在
                String hostNameQualifiedName = hostName[i] + ":" + hostPort[i];
                queryWrapper.lambda().eq(MetadataMapAtlasPO::getQualifiedName, hostNameQualifiedName);
                MetadataMapAtlasPO po = metadataMapAtlasMapper.selectOne(queryWrapper);
                String instanceGuid = "";
                //判断实例是否已存在
                if (po == null) {
                    String addResult = addEntity(EntityTypeEnum.RDBMS_INSTANCE, null, "", null, null, i);
                    if (addResult == "") {
                        return;
                    }
                    //向MetadataMapAtlas配置表添加数据
                    instanceGuid = addMetadataMapAtlas(addResult,
                            EntityTypeEnum.RDBMS_INSTANCE, hostNameQualifiedName,
                            0,
                            0,
                            0,
                            0,
                            0,
                            "",
                            "",
                            0,
                            0);
                    log.info("add entity instance name:",hostName[i]+",guid:"+instanceGuid);
                }
                else {
                    instanceGuid=po.atlasGuid;
                    //存在,获取该实例详情,并判断是否需要修改
                    updateEntity(EntityTypeEnum.RDBMS_INSTANCE,po,"","",null,null);
                }
                //数据添加redis
                setRedis(instanceGuid);
            }
        }
        catch (Exception e)
        {
            log.error("synchronizationInstance ex:",e);
        }
    }

    /**
     * 同步数据库
     */
    public void synchronizationDb()
    {
        try {
            QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(MetadataMapAtlasPO::getType, EntityTypeEnum.RDBMS_INSTANCE.getValue());
            List<MetadataMapAtlasPO> poList = metadataMapAtlasMapper.selectList(queryWrapper);
            if (CollectionUtils.isEmpty(poList))
            {
                return;
            }
            String[] instanceList=fiDataHostName.split(",");
            String[] hostPort=fiDataPort.split(",");
            String[] dbList = db.split(",");
            QueryWrapper<MetadataMapAtlasPO> queryWrapper1=new QueryWrapper<>();
            queryWrapper1.lambda().eq(MetadataMapAtlasPO::getType, EntityTypeEnum.RDBMS_DB.getValue());
            List<MetadataMapAtlasPO> mapAtlasDbPoList =metadataMapAtlasMapper.selectList(queryWrapper1);
            int index=0;
            for (int i = 0; i < dbList.length; i++) {
                index += 1;
                int j = i;
                int finalIndex = index;
                String newHostName = instanceList[i] + ":" + hostPort[i];
                Optional<MetadataMapAtlasPO> instancePo = poList.stream().filter(e -> e.qualifiedName.equals(newHostName)).findFirst();
                if (!instancePo.isPresent()) {
                    continue;
                }
                String dbQualifiedName = instancePo.get().qualifiedName + "_" + dbList[i];
                String dbGuid = "";
                List<MetadataMapAtlasPO> dbPo= mapAtlasDbPoList.stream()
                        .filter(e->e.dbNameType== finalIndex).collect(Collectors.toList());
                //存在,判断是否修改
                if (!CollectionUtils.isEmpty(dbPo)) {
                    dbGuid = dbPo.get(0).atlasGuid;
                    updateEntity(EntityTypeEnum.RDBMS_DB, dbPo.get(0), dbList[i], dbQualifiedName, null, null);
                } else {
                    String addResult = addEntity(EntityTypeEnum.RDBMS_DB, instancePo.get(), dbList[i], null, null, 0);
                    if (addResult != "") {
                        dbGuid = addMetadataMapAtlas(addResult,
                                EntityTypeEnum.RDBMS_DB,
                                dbQualifiedName,
                                0,
                                0,
                                0,
                                0,
                                finalIndex,
                                instancePo.get().atlasGuid,
                                "",
                                0,
                                0);
                        log.info("add entity db name:",db+",guid:"+dbGuid);
                    }
                }
                //数据添加redis
                setRedis(dbGuid);
            }
        }
        catch (Exception e)
        {
            log.error("synchronizationDb ex:",e);
        }
    }

    /**
     * 同步ods
     */
    public void synchronizationOds()
    {
        ResultEntity<List<DataAccessSourceTableDTO>> result = dataAccessClient.getDataAccessMetaData();
        if (result.code != ResultEnum.SUCCESS.getCode() || CollectionUtils.isEmpty(result.data)) {
            return;
        }
        List<SourceTableDTO> list=new ArrayList<>();
        for (DataAccessSourceTableDTO item : result.data) {
            SourceTableDTO dto = MetadataMapAtlasMap.INSTANCES.dtoToDto(item);
            dto.fieldList = MetadataMapAtlasMap.INSTANCES.fieldToDto(item.list);
            dto.fieldList = dto.fieldList.stream().distinct().collect(Collectors.toList());
            list.add(dto);
        }
        ////list=list.stream().filter(e->e.tableName.contains("timingTask_Favorite")).collect(Collectors.toList());
        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataMapAtlasPO::getDbNameType, DataTypeEnum.DATA_INPUT.getValue());
        MetadataMapAtlasPO po = metadataMapAtlasMapper.selectOne(queryWrapper);
        if (po == null) {
            return;
        }
        //同步ods元数据对象
        String[] dbList = db.split(",");
        synchronizationData(list, po.qualifiedName, DataTypeEnum.DATA_INPUT.getValue(), dbList[0]);
        //删除ods中不存在的元数据对象
        delSynchronization(list, DataTypeEnum.DATA_INPUT.getValue(), false);
    }

    /**
     * 同步dw
     */
    public void synchronizationDw()
    {
        ResultEntity<Object> result = client.getDataModelTable(1);
        if (result.code != ResultEnum.SUCCESS.getCode()) {
            return;
        }
        List<SourceTableDTO> list = JSON.parseArray(JSON.toJSONString(result.data), SourceTableDTO.class);
        ////list = list.stream().filter(e -> e.tableName.equals("dim_ghs2")).collect(Collectors.toList());
        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataMapAtlasPO::getDbNameType, DataTypeEnum.DATA_MODEL.getValue());
        MetadataMapAtlasPO po = metadataMapAtlasMapper.selectOne(queryWrapper);
        if (po == null) {
            return;
        }
        //同步dw元数据对象
        String[] dbList = db.split(",");
        synchronizationData(list, po.qualifiedName, DataTypeEnum.DATA_MODEL.getValue(), dbList[1]);
        //删除dw中不存在的元数据对象
        delSynchronization(list, DataTypeEnum.DATA_MODEL.getValue(), false);
    }

    /**
     * 同步doris
     */
    public void synchronizationDoris()
    {
        ResultEntity<Object> result = client.getDataModelTable(2);
        if (result.code != ResultEnum.SUCCESS.getCode()) {
            return;
        }
        List<SourceTableDTO> list = JSON.parseArray(JSON.toJSONString(result.data), SourceTableDTO.class);
        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataMapAtlasPO::getDbNameType, DataTypeEnum.DATA_DORIS.getValue());
        MetadataMapAtlasPO po = metadataMapAtlasMapper.selectOne(queryWrapper);
        if (po == null) {
            return;
        }
        //同步doris元数据对象
        String[] dbList = db.split(",");
        synchronizationData(list, po.qualifiedName, DataTypeEnum.DATA_DORIS.getValue(), dbList[2]);
        //删除doris中不存在的元数据对象
        delSynchronization(list, DataTypeEnum.DATA_DORIS.getValue(), false);
    }

    /**
     * 同步宽表
     */
    public void synchronizationWideTable() {
        ResultEntity<Object> result = client.getDataModelTable(3);
        if (result.code != ResultEnum.SUCCESS.getCode()) {
            return;
        }
        List<SourceTableDTO> list = JSON.parseArray(JSON.toJSONString(result.data), SourceTableDTO.class);
        List<SourceTableDTO> collect = list.stream().filter(e -> e.type == 5).collect(Collectors.toList());
        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataMapAtlasPO::getDbNameType, DataTypeEnum.DATA_DORIS.getValue());
        MetadataMapAtlasPO po = metadataMapAtlasMapper.selectOne(queryWrapper);
        if (po == null) {
            return;
        }
        //同步doris元数据对象
        String[] dbList = db.split(",");
        synchronizationData(collect, po.qualifiedName, DataTypeEnum.DATA_DORIS.getValue(), dbList[2]);
        //删除doris中不存在的元数据对象
        delSynchronization(collect, DataTypeEnum.DATA_DORIS.getValue(), true);
    }

    public void synchronizationData(List<SourceTableDTO> list, String dbNameQualified, int dataType, String dbName) {
        try {
            QueryWrapper<MetadataMapAtlasPO> mapAtlasPoQueryWrapper = new QueryWrapper<>();
            mapAtlasPoQueryWrapper.lambda().eq(MetadataMapAtlasPO::getType, EntityTypeEnum.RDBMS_DB)
                    .eq(MetadataMapAtlasPO::getQualifiedName, dbNameQualified);
            MetadataMapAtlasPO dbPo = metadataMapAtlasMapper.selectOne(mapAtlasPoQueryWrapper);
            if (dbPo == null) {
                return;
            }
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            //获取数据配置规则
            int dataSourceId = 0;
            ResultEntity<List<DataSourceConVO>> allDataSource = dataQualityClient.getAllDataSource();
            if (allDataSource.code == ResultEnum.SUCCESS.getCode()) {
                Optional<DataSourceConVO> first = allDataSource.data.stream().filter(e -> e.conDbname.equals(dbName)).findFirst();
                if (first.isPresent()) {
                    dataSourceId = first.get().id;
                }
            }
            //获取业务元数据配置表
            QueryWrapper<BusinessMetadataConfigPO> businessMetadataConfigPoWrapper = new QueryWrapper<>();
            List<BusinessMetadataConfigPO> poList = businessMetadataConfigMapper.selectList(businessMetadataConfigPoWrapper);
            for (SourceTableDTO dto : list) {
                QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(MetadataMapAtlasPO::getTableId, dto.id)
                        .eq(MetadataMapAtlasPO::getColumnId, 0)
                        .eq(MetadataMapAtlasPO::getDataType, dataType)
                        .eq(MetadataMapAtlasPO::getType, EntityTypeEnum.RDBMS_TABLE)
                        .eq(MetadataMapAtlasPO::getTableType, dto.type);
                MetadataMapAtlasPO po = metadataMapAtlasMapper.selectOne(queryWrapper);
                String qualifiedName = dbPo.qualifiedName + "_" + dto.tableName;
                if (po == null) {
                    String addResult = addEntity(EntityTypeEnum.RDBMS_TABLE, dbPo, dto.tableName, dto, null, 0);
                    if (StringUtils.isEmpty(addResult)) {
                        continue;
                    }
                    String tableGuid = addMetadataMapAtlas(addResult,
                            EntityTypeEnum.RDBMS_TABLE,
                            qualifiedName,
                            dataType,
                            dto.id,
                            0,
                            dto.type,
                            0,
                            dbPo.atlasGuid,
                            "",
                            0,
                            0);
                    log.info("add entity table name:", dto.tableName + ",guid:" + tableGuid);
                    if (CollectionUtils.isEmpty(dto.fieldList)) {
                        continue;
                    }
                    //设置表业务元数据值
                    TableRuleInfoDTO tableRuleInfo = setTableRuleInfo(dataSourceId, (int) dto.id, dto.tableName, dataType, dto.type);
                    setBusinessMetaDataAttributeValue(addResult, tableRuleInfo, poList);
                    po = metadataMapAtlasMapper.selectOne(queryWrapper);
                    for (SourceFieldDTO fieldDTO : dto.fieldList) {
                        String fieldQualifiedName = qualifiedName + "_" + fieldDTO.fieldName;
                        String addColumnResult = addEntity(EntityTypeEnum.RDBMS_COLUMN, po, fieldDTO.fieldName, null, fieldDTO, 0);
                        String dimensionKey = "";
                        if (fieldDTO.attributeType == FactAttributeEnum.DIMENSION_KEY.getValue()) {
                            dimensionKey = fieldDTO.fieldName;
                        }
                        if (!StringUtils.isEmpty(addColumnResult)) {
                            String columnGuid = addMetadataMapAtlas(addColumnResult,
                                    EntityTypeEnum.RDBMS_COLUMN,
                                    fieldQualifiedName,
                                    dataType,
                                    dto.id,
                                    fieldDTO.id,
                                    dto.type,
                                    0,
                                    tableGuid,
                                    dimensionKey,
                                    fieldDTO.attributeType,
                                    fieldDTO.atomicId);
                            log.info("add entity column name:", fieldDTO.fieldName + ",guid:" + columnGuid);
                            Optional<TableRuleInfoDTO> first = tableRuleInfo.fieldRules.stream()
                                    .filter(e -> fieldDTO.fieldName.equals(e.name))
                                    .findFirst();
                            if (first.isPresent()) {
                                setBusinessMetaDataAttributeValue(columnGuid, first.get(), poList);
                            }
                        }
                    }
                }
                else {
                    updateEntity(EntityTypeEnum.RDBMS_TABLE, po, dto.tableName, qualifiedName, dto, null);
                    //设置表业务元数据值
                    TableRuleInfoDTO tableRuleInfo = setTableRuleInfo(dataSourceId, (int) dto.id, dto.tableName, dataType, dto.type);
                    setBusinessMetaDataAttributeValue(po.atlasGuid, tableRuleInfo, poList);
                    //判断表下的字段是否需要修改
                    if (CollectionUtils.isEmpty(dto.fieldList)) {
                        continue;
                    }
                    for (SourceFieldDTO field : dto.fieldList) {
                        QueryWrapper<MetadataMapAtlasPO> queryWrapper1 = new QueryWrapper<>();
                        queryWrapper1.lambda()
                                .eq(MetadataMapAtlasPO::getTableId, dto.id)
                                .eq(MetadataMapAtlasPO::getColumnId, field.id)
                                .eq(MetadataMapAtlasPO::getType, EntityTypeEnum.RDBMS_COLUMN)
                                .eq(MetadataMapAtlasPO::getTableType, dto.type);
                        MetadataMapAtlasPO fieldData = metadataMapAtlasMapper.selectOne(queryWrapper1);
                        String dimensionKey = "";
                        if (field.attributeType == FactAttributeEnum.DIMENSION_KEY.getValue()) {
                            fieldData.dimensionKey = field.fieldName;
                        }
                        //不存在,则添加
                        String columnGuid = "";
                        if (fieldData == null) {
                            String fieldQualifiedName = po.qualifiedName + "_" + field.fieldName;
                            String addColumnResult = addEntity(EntityTypeEnum.RDBMS_COLUMN, po, field.fieldName, null, field, 0);
                            if (!StringUtils.isEmpty(addColumnResult)) {
                                columnGuid = addMetadataMapAtlas(addColumnResult,
                                        EntityTypeEnum.RDBMS_COLUMN,
                                        fieldQualifiedName,
                                        dataType,
                                        dto.id,
                                        field.id,
                                        dto.type,
                                        0,
                                        po.atlasGuid,
                                        dimensionKey,
                                        field.attributeType,
                                        field.atomicId);
                                log.info("add entity column name:", field.fieldName + ",guid:" + columnGuid);
                            }
                        } else {
                            columnGuid = fieldData.atlasGuid;
                            String newQualifiedName = po.qualifiedName + "_" + field.fieldName;
                            updateEntity(EntityTypeEnum.RDBMS_COLUMN, fieldData, field.fieldName, newQualifiedName, null, field);
                        }
                        Optional<TableRuleInfoDTO> first = tableRuleInfo.fieldRules.stream()
                                .filter(e -> field.fieldName.equals(e.name))
                                .findFirst();
                        if (first.isPresent()) {
                            setBusinessMetaDataAttributeValue(columnGuid, first.get(), poList);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("synchronizationData ex", e);
        }
    }

    /**
     * 删除元数据对象
     *
     * @param list
     * @param dataType
     */
    public void delSynchronization(List<SourceTableDTO> list, int dataType, boolean wideTable) {
        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(MetadataMapAtlasPO::getDataType, dataType)
                .eq(MetadataMapAtlasPO::getType, EntityTypeEnum.RDBMS_TABLE)
                .eq(MetadataMapAtlasPO::getColumnId, 0);
        if (dataType == DataTypeEnum.DATA_DORIS.getValue()) {
            if (wideTable) {
                queryWrapper.lambda().eq(MetadataMapAtlasPO::getTableType, DataModelTableTypeEnum.WIDE_TABLE.getValue());
            } else {
                queryWrapper.lambda().ne(MetadataMapAtlasPO::getTableType, DataModelTableTypeEnum.WIDE_TABLE.getValue());
            }
        }
        List<MetadataMapAtlasPO> poList = metadataMapAtlasMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            return;
        }
        //删除表
        List<Integer> poIdList = (List) poList.stream().map(e -> e.getTableId()).collect(Collectors.toList());
        List<Integer> dtoIdList = (List) list.stream().map(e -> e.getId()).collect(Collectors.toList());
        //取差集
        poIdList.removeAll(dtoIdList);
        //atlas删除表元数据对象
        if (!CollectionUtils.isEmpty(poIdList))
        {
            //删除atlas表数据
            List<MetadataMapAtlasPO> delList=poList.stream()
                    .filter(e->poIdList.contains(e.tableId))
                    .collect(Collectors.toList());
            for (MetadataMapAtlasPO item : delList) {
                ResultDataDTO<String> delAtlas = atlasClient.delete(entityByGuid + "/" + item.atlasGuid);
                if (delAtlas.code != AtlasResultEnum.REQUEST_SUCCESS) {
                    continue;
                }
                //删除Metadata配置表数据
                UpdateMetadataMapAtlasDTO dto = new UpdateMetadataMapAtlasDTO();
                dto.id = item.tableId;
                dto.dataType = dataType;
                dto.tableType = item.tableType;
                metadataMapAtlasMapper.delBatchMetadataMapAtlas(dto);
            }
        }
        //删除atlas字段数据
        QueryWrapper<MetadataMapAtlasPO> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.lambda().eq(MetadataMapAtlasPO::getDataType,dataType)
                .eq(MetadataMapAtlasPO::getType,EntityTypeEnum.RDBMS_COLUMN)
                .ne(MetadataMapAtlasPO::getColumnId,0);
        List<MetadataMapAtlasPO> dwMetadataList=metadataMapAtlasMapper.selectList(queryWrapper1);
        //atlas删除字段元数据对象
        for (SourceTableDTO dto:list)
        {
            //获取字段集合
            List<Integer> columnList=(List)dwMetadataList.stream()
                    .filter(e->e.tableId==dto.id && e.tableType==dto.type).map(e->e.getColumnId()).collect(Collectors.toList());
            //获取配置表字段集合
            List<Integer> fieldIdList=(List)dto.fieldList.stream()
                    .map(e->e.getId())
                    .collect(Collectors.toList());
            //取差集,获取已删除字段集合
            columnList.removeAll(fieldIdList);
            if (CollectionUtils.isEmpty(columnList))
            {
                continue;
            }
            List<MetadataMapAtlasPO> delColumnList=dwMetadataList.stream()
                    .filter(e->columnList.contains(e.columnId) && e.tableType==dto.type)
                    .collect(Collectors.toList());
            for (MetadataMapAtlasPO mapAtlasPo :delColumnList)
            {
                ResultDataDTO<String> delAtlas = atlasClient.delete(entityByGuid + "/" + mapAtlasPo.atlasGuid);
                if (delAtlas.code != AtlasResultEnum.REQUEST_SUCCESS)
                {
                    continue;
                }
                metadataMapAtlasMapper.deleteByIdWithFill(mapAtlasPo);
            }
        }
    }

    /**
     * 添加元数据对象
     * @param entityTypeEnum
     * @param po
     * @param name
     * @param dto
     * @param fieldDTO
     * @return
     */
    public String addEntity(EntityTypeEnum entityTypeEnum,
                            MetadataMapAtlasPO po,
                            String name,
                            SourceTableDTO dto,
                            SourceFieldDTO fieldDTO,
                            int index) {
        //组装参数
        EntityDTO entityDTO = new EntityDTO();
        EntityTypeDTO entityTypeDTO = new EntityTypeDTO();
        entityTypeDTO.typeName = entityTypeEnum.getName();
        EntityAttributesDTO attributesDTO = new EntityAttributesDTO();
        EntityIdAndTypeDTO parentEntity = new EntityIdAndTypeDTO();
        if (po != null) {
            parentEntity.guid = po.atlasGuid;
        }
        //获取类型
        EntityTypeEnum typeNameEnum = EntityTypeEnum.getValue(entityTypeEnum.getName());
        switch (typeNameEnum) {
            case RDBMS_INSTANCE:
                String[] userList = fiDataUserName.split(",");
                String[] passwordList = fiDataPassword.split(",");
                attributesDTO.qualifiedName = fiDataName.split(",")[index] + ":" + fiDataPort.split(",")[index];
                attributesDTO.hostname = fiDataHostName.split(",")[index];
                attributesDTO.port=fiDataPort.split(",")[index];
                attributesDTO.platform=fiDataPlatform.split(",")[index];
                attributesDTO.name=fiDataName.split(",")[index]+":"+fiDataPort.split(",")[index];
                attributesDTO.protocol=fiDataProtocol.split(",")[index];
                attributesDTO.rdbms_type=fiDataRdbmsType.split(",")[index];
                attributesDTO.description=fiDataName.split(",")[index];
                attributesDTO.comment=userList[index]+"\\"+passwordList[index];
                break;
            case RDBMS_DB:
                attributesDTO.qualifiedName=po.qualifiedName+"_"+name;
                attributesDTO.name=name;
                attributesDTO.description=name;
                attributesDTO.comment=name;
                parentEntity.typeName=EntityTypeEnum.RDBMS_INSTANCE.getName();
                attributesDTO.instance=parentEntity;
                break;
            case RDBMS_TABLE:
                attributesDTO.qualifiedName=po.qualifiedName+"_"+name;
                attributesDTO.name=dto.tableName;
                attributesDTO.comment=dto.tableName;
                attributesDTO.description=dto.tableDes==""?name:dto.tableName;
                parentEntity.typeName=EntityTypeEnum.RDBMS_DB.getName();
                attributesDTO.db=parentEntity;
                break;
            case RDBMS_COLUMN:
                attributesDTO.qualifiedName=po.qualifiedName+"_"+name;
                attributesDTO.comment=fieldDTO.fieldName;
                attributesDTO.name=fieldDTO.fieldName;
                attributesDTO.description=fieldDTO.fieldDes==""?fieldDTO.fieldName:fieldDTO.fieldDes;
                attributesDTO.data_type=fieldDTO.fieldType;
                attributesDTO.length=fieldDTO.fieldLength+"";
                parentEntity.typeName=EntityTypeEnum.RDBMS_TABLE.getName();
                attributesDTO.table=parentEntity;
                break;
            default:
                break;
        }
        attributesDTO.owner="root";
        attributesDTO.contact_info="root";
        entityTypeDTO.attributes=attributesDTO;
        entityDTO.entity=entityTypeDTO;
        String jsonParameter= JSONArray.toJSON(entityDTO).toString();
        //调用atlas添加实例
        ResultDataDTO<String> addResult = atlasClient.post(entity, jsonParameter);
        return addResult.code==AtlasResultEnum.REQUEST_SUCCESS?addResult.data:"";
    }

    /**
     * 更新元数据对象
     * @param entityTypeEnum
     * @param po
     * @param name
     * @param qualifiedName
     * @param dto
     * @param fieldDTO
     */
    public void updateEntity(EntityTypeEnum entityTypeEnum,
                             MetadataMapAtlasPO po,
                             String name,
                             String qualifiedName,
                             SourceTableDTO dto,
                             SourceFieldDTO fieldDTO) {
        boolean change = false;
        ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + po.atlasGuid);
        if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return;
        }
        //解析数据
        JSONObject jsonObj = JSON.parseObject(getDetail.data);
        JSONObject entityObject = JSON.parseObject(jsonObj.getString("entity"));
        JSONObject attribute = JSON.parseObject(entityObject.getString("attributes"));
        EntityTypeEnum typeNameEnum = EntityTypeEnum.getValue(entityTypeEnum.getName());
        switch (typeNameEnum) {
            case RDBMS_INSTANCE:
                if (!fiDataName.equals(attribute.getString("name"))
                        || !fiDataHostName.equals(attribute.getString("hostname"))
                        || !fiDataPort.equals(attribute.getString("port"))
                        || !fiDataPlatform.equals(attribute.getString("platform"))
                        || !fiDataProtocol.equals(attribute.getString("protocol"))
                        || !fiDataRdbmsType.equals(attribute.getString("rdbms_type"))
                )
                {
                    //修改数据
                    attribute.put("hostname",fiDataHostName);
                    attribute.put("name",fiDataName);
                    attribute.put("port",fiDataPort);
                    attribute.put("platform",fiDataPlatform);
                    attribute.put("protocol",fiDataProtocol);
                    attribute.put("qualifiedName",fiDataName);
                    //修改MetadataMapAtlas表中qualifiedName字段
                    po.qualifiedName=fiDataName;
                    change=true;
                }
                break;
            case RDBMS_DB:
                if (!name.equals(attribute.getString("name")))
                {
                    attribute.put("name",name);
                    attribute.put("qualifiedName",qualifiedName);
                    attribute.put("description",name);
                    po.qualifiedName=qualifiedName;
                    change=true;
                }
                break;
            case RDBMS_TABLE:
                if (!dto.tableName.equals(attribute.getString("name"))
                        || !po.qualifiedName.equals(attribute.getString("qualifiedName"))
                        || !dto.tableDes.equals(attribute.getString("description")))
                {
                    attribute.put("name",dto.tableName);
                    attribute.put("qualifiedName",qualifiedName);
                    attribute.put("description",dto.tableDes);
                    po.qualifiedName=qualifiedName;
                    change=true;
                }
                break;
            case RDBMS_COLUMN:
                String length=fieldDTO.fieldLength+"";
                if (!fieldDTO.fieldName.equals(attribute.getString("name"))
                        || !po.qualifiedName.equals(attribute.getString("qualifiedName"))
                        || !length.equals(attribute.getString("length"))
                        || !fieldDTO.fieldType.equals(attribute.getString("data_type")))
                {
                    attribute.put("name",fieldDTO.fieldName);
                    attribute.put("qualifiedName",qualifiedName);
                    attribute.put("description",fieldDTO.fieldDes);
                    attribute.put("length",fieldDTO.fieldLength);
                    attribute.put("data_type",fieldDTO.fieldType);
                    po.qualifiedName=qualifiedName;
                    change=true;
                }
                break;
            default:
                break;
        }
        if (!change)
        {
            return;
        }
        entityObject.put("attributes",attribute);
        jsonObj.put("entity",entityObject);
        String jsonParameter=JSONArray.toJSON(jsonObj).toString();
        ResultDataDTO<String> result = atlasClient.post(entity, jsonParameter);
        if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return;
        }
        metadataMapAtlasMapper.updateById(po);
    }

    /**
     * MetadataMapAtlas配置表添加数据
     *
     * @param jsonStr        添加元数据实体,返回json串
     * @param entityTypeEnum 元数据类型:1实例,2数据库,3表,4报表,5接口,6字段,7process
     * @param qualifiedName  元数据唯一标识
     * @param dataType       数据类型:1数据接入,2数据建模,3doris
     * @param tableId        表id
     * @param columnId       字段id
     * @param tableType      表类型:1dw维度表,2dw事实表,3doris维度表,4doris指标表,5宽表
     * @param parentGuid     父级 atlas guid
     * @return
     */
    public String addMetadataMapAtlas(String jsonStr,
                                      EntityTypeEnum entityTypeEnum,
                                      String qualifiedName,
                                      int dataType,
                                      long tableId,
                                      long columnId,
                                      int tableType,
                                      int dbNameType,
                                      String parentGuid,
                                      String dimensionKey,
                                      int attributeType,
                                      int atomicId) {
        try {
            JSONObject jsonObj = JSON.parseObject(jsonStr);
            JSONObject mutatedEntities = JSON.parseObject(jsonObj.getString("mutatedEntities"));
            String strMutatedEntities = mutatedEntities.toString();
            JSONArray jsonArray;
            if (strMutatedEntities.indexOf("CREATE") > -1) {
                jsonArray = mutatedEntities.getJSONArray("CREATE");
            } else {
                jsonArray = mutatedEntities.getJSONArray("UPDATE");
            }
            MetadataMapAtlasPO metadataMapAtlasPo =new MetadataMapAtlasPO();
            metadataMapAtlasPo.atlasGuid=jsonArray.getJSONObject(0).getString("guid");
            metadataMapAtlasPo.type=entityTypeEnum.getValue();
            metadataMapAtlasPo.qualifiedName=qualifiedName;
            metadataMapAtlasPo.parentAtlasGuid=parentGuid;
            metadataMapAtlasPo.dataType=dataType;
            metadataMapAtlasPo.columnId=columnId;
            metadataMapAtlasPo.tableId=tableId;
            metadataMapAtlasPo.tableType=tableType;
            metadataMapAtlasPo.dbNameType=dbNameType;
            metadataMapAtlasPo.attributeType=attributeType;
            metadataMapAtlasPo.dimensionKey = dimensionKey;
            metadataMapAtlasPo.atomicId = atomicId;
            int flat = metadataMapAtlasMapper.insert(metadataMapAtlasPo);
            return flat > 0 ? metadataMapAtlasPo.atlasGuid : "";
        } catch (Exception e) {
            log.error("addMetadataMapAtlas ex:", e);
            return "";
        }
    }

    /**
     * 存储Redis
     *
     * @param guid
     */
    public void setRedis(String guid) {
        ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + guid);
        if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
            return;
        }
        redisTemplate.opsForValue().set("metaDataEntityData:" + guid, getDetail.data);
    }

    /**
     * 设置业务元数据表规则
     *
     * @param dataSourceId
     * @param tableId
     * @param tableName
     * @param dataType
     */
    public TableRuleInfoDTO setTableRuleInfo(int dataSourceId,
                                             int tableId,
                                             String tableName,
                                             int dataType,
                                             int tableType) {
        TableRuleInfoDTO dto = new TableRuleInfoDTO();
        ResultEntity<TableRuleInfoDTO> tableRule = dataQualityClient.getTableRuleList(dataSourceId, tableName);
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
            result = client.setTableRule(parameter);
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
                dto.fieldRules.stream().map(e -> {
                    e.businessName = data.businessName;
                    e.dataResponsiblePerson = data.dataResponsiblePerson;
                    return e;
                });
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
