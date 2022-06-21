package com.fisk.datamanagement.synchronization.fidata;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.datamanagement.dto.entity.EntityAttributesDTO;
import com.fisk.datamanagement.dto.entity.EntityDTO;
import com.fisk.datamanagement.dto.entity.EntityIdAndTypeDTO;
import com.fisk.datamanagement.dto.entity.EntityTypeDTO;
import com.fisk.datamanagement.dto.metadatamapatlas.UpdateMetadataMapAtlasDTO;
import com.fisk.datamanagement.entity.MetadataMapAtlasPO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.enums.DataTypeEnum;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.map.MetadataMapAtlasMap;
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

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
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
    MetadataMapAtlasMapper metadataMapAtlasMapper;
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
        QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataMapAtlasPO::getDbNameType,DataTypeEnum.DATA_INPUT.getValue());
        MetadataMapAtlasPO po=metadataMapAtlasMapper.selectOne(queryWrapper);
        if (po == null) {
            return;
        }
        //同步ods元数据对象
        synchronizationData(list,po.qualifiedName, DataTypeEnum.DATA_INPUT.getValue());
        //删除ods中不存在的元数据对象
        delSynchronization(list, DataTypeEnum.DATA_INPUT.getValue(), false);
    }

    /**
     * 同步dw
     */
    public void synchronizationDw()
    {
        ResultEntity<Object> result = client.getDataModelTable(1);
        if (result.code!=ResultEnum.SUCCESS.getCode())
        {
            return;
        }
        List<SourceTableDTO> list=JSON.parseArray(JSON.toJSONString(result.data),SourceTableDTO.class);
        ////list=list.stream().filter(e->e.tableName.equals("fact_UserTest")).collect(Collectors.toList());
        QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataMapAtlasPO::getDbNameType,DataTypeEnum.DATA_MODEL.getValue());
        MetadataMapAtlasPO po=metadataMapAtlasMapper.selectOne(queryWrapper);
        if (po==null)
        {
            return;
        }
        //同步dw元数据对象
        synchronizationData(list,po.qualifiedName,DataTypeEnum.DATA_MODEL.getValue());
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
        synchronizationData(list, po.qualifiedName, DataTypeEnum.DATA_DORIS.getValue());
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
        synchronizationData(collect, po.qualifiedName, DataTypeEnum.DATA_DORIS.getValue());
        //删除doris中不存在的元数据对象
        delSynchronization(collect, DataTypeEnum.DATA_DORIS.getValue(), true);
    }

    public void synchronizationData(List<SourceTableDTO> list, String dbName, int dataType) {
        try {
            QueryWrapper<MetadataMapAtlasPO> mapAtlasPoQueryWrapper = new QueryWrapper<>();
            mapAtlasPoQueryWrapper.lambda().eq(MetadataMapAtlasPO::getType, EntityTypeEnum.RDBMS_DB)
                    .eq(MetadataMapAtlasPO::getQualifiedName, dbName);
            MetadataMapAtlasPO dbPo = metadataMapAtlasMapper.selectOne(mapAtlasPoQueryWrapper);
            if (dbPo == null)
            {
                return;
            }
            if (CollectionUtils.isEmpty(list))
            {
                return;
            }
            for (SourceTableDTO dto:list)
            {
                QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
                queryWrapper.lambda().eq(MetadataMapAtlasPO::getTableId,dto.id)
                        .eq(MetadataMapAtlasPO::getColumnId,0)
                        .eq(MetadataMapAtlasPO::getDataType,dataType)
                        .eq(MetadataMapAtlasPO::getType,EntityTypeEnum.RDBMS_TABLE)
                        .eq(MetadataMapAtlasPO::getTableType,dto.type);
                MetadataMapAtlasPO po=metadataMapAtlasMapper.selectOne(queryWrapper);
                String qualifiedName=dbPo.qualifiedName+"_"+dto.tableName;
                if (po==null)
                {
                    String addResult = addEntity(EntityTypeEnum.RDBMS_TABLE, dbPo, dto.tableName, dto,null,0);
                    if (addResult=="")
                    {
                        continue;
                    }
                    String tableGuid=addMetadataMapAtlas(addResult,
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
                    log.info("add entity table name:",dto.tableName+",guid:"+tableGuid);
                    if (CollectionUtils.isEmpty(dto.fieldList))
                    {
                        continue;
                    }
                    po=metadataMapAtlasMapper.selectOne(queryWrapper);
                    for (SourceFieldDTO fieldDTO:dto.fieldList)
                    {

                        String fieldQualifiedName=qualifiedName+"_"+fieldDTO.fieldName;
                        String addColumnResult = addEntity(EntityTypeEnum.RDBMS_COLUMN, po, fieldDTO.fieldName, null, fieldDTO,0);
                        String dimensionKey="";
                        if (fieldDTO.attributeType== FactAttributeEnum.DIMENSION_KEY.getValue())
                        {
                            dimensionKey=fieldDTO.fieldName;
                        }
                        if (addColumnResult !="")
                        {
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
                            log.info("add entity column name:",fieldDTO.fieldName+",guid:"+columnGuid);
                        }
                    }
                }
                else {
                    updateEntity(EntityTypeEnum.RDBMS_TABLE,po,dto.tableName,qualifiedName,dto,null);
                    //判断表下的字段是否需要修改
                    if (CollectionUtils.isEmpty(dto.fieldList))
                    {
                        continue;
                    }
                    for (SourceFieldDTO field:dto.fieldList)
                    {
                        QueryWrapper<MetadataMapAtlasPO> queryWrapper1=new QueryWrapper<>();
                        queryWrapper1.lambda()
                                .eq(MetadataMapAtlasPO::getTableId,dto.id)
                                .eq(MetadataMapAtlasPO::getColumnId,field.id)
                                .eq(MetadataMapAtlasPO::getType,EntityTypeEnum.RDBMS_COLUMN)
                                .eq(MetadataMapAtlasPO::getTableType,dto.type);
                        MetadataMapAtlasPO fieldData=metadataMapAtlasMapper.selectOne(queryWrapper1);
                        String dimensionKey="";
                        if (field.attributeType== FactAttributeEnum.DIMENSION_KEY.getValue())
                        {
                            fieldData.dimensionKey=field.fieldName;
                        }
                        //不存在,则添加
                        if (fieldData==null)
                        {
                            String fieldQualifiedName=po.qualifiedName+"_"+field.fieldName;
                            String addColumnResult = addEntity(EntityTypeEnum.RDBMS_COLUMN, po, field.fieldName, null, field,0);
                            if (addColumnResult !="")
                            {
                                String columnGuid = addMetadataMapAtlas(addColumnResult,
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
                                log.info("add entity column name:",field.fieldName+",guid:"+columnGuid);
                            }
                        }
                        else {
                            String newQualifiedName=po.qualifiedName+"_"+field.fieldName;
                            updateEntity(EntityTypeEnum.RDBMS_COLUMN,fieldData,field.fieldName,newQualifiedName,null,field);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("synchronizationData ex",e);
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
        if (wideTable) {
            queryWrapper.lambda().eq(MetadataMapAtlasPO::getTableType, DataModelTableTypeEnum.WIDE_TABLE.getValue());
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
                            int index)
    {
        //组装参数
        EntityDTO entityDTO=new EntityDTO();
        EntityTypeDTO entityTypeDTO=new EntityTypeDTO();
        entityTypeDTO.typeName=entityTypeEnum.getName();
        EntityAttributesDTO attributesDTO=new EntityAttributesDTO();
        EntityIdAndTypeDTO parentEntity=new EntityIdAndTypeDTO();
        if (po!=null)
        {
            parentEntity.guid=po.atlasGuid;
        }
        //获取类型
        EntityTypeEnum typeNameEnum = EntityTypeEnum.getValue(entityTypeEnum.getName());
        switch (typeNameEnum)
        {
            case RDBMS_INSTANCE:
                String[] userList = fiDataUserName.split(",");
                String[] passwordList = fiDataPassword.split(",");
                attributesDTO.qualifiedName=fiDataName.split(",")[index]+":"+fiDataPort.split(",")[index];
                attributesDTO.hostname =fiDataHostName.split(",")[index];
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
                             SourceFieldDTO fieldDTO)
    {
        boolean change=false;
        ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + po.atlasGuid);
        if (getDetail.code !=AtlasResultEnum.REQUEST_SUCCESS)
        {
            return;
        }
        //解析数据
        JSONObject jsonObj = JSON.parseObject(getDetail.data);
        JSONObject entityObject= JSON.parseObject(jsonObj.getString("entity"));
        JSONObject attribute=JSON.parseObject(entityObject.getString("attributes"));
        EntityTypeEnum typeNameEnum = EntityTypeEnum.getValue(entityTypeEnum.getName());
        switch (typeNameEnum)
        {
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
     * @param jsonStr
     * @param entityTypeEnum
     * @param qualifiedName
     * @param dataType
     * @param tableId
     * @param columnId
     * @param tableType
     * @param parentGuid
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

}
