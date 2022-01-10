package com.fisk.datamanagement.synchronization.fidata;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.datamanagement.dto.entity.EntityAttributesDTO;
import com.fisk.datamanagement.dto.entity.EntityDTO;
import com.fisk.datamanagement.dto.entity.EntityIdAndTypeDTO;
import com.fisk.datamanagement.dto.entity.EntityTypeDTO;
import com.fisk.datamanagement.dto.metadatamapatlas.UpdateMetadataMapAtlasDTO;
import com.fisk.datamanagement.entity.MetadataMapAtlasPO;
import com.fisk.datamanagement.enums.DataTypeEnum;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.map.MetadataMapAtlasMap;
import com.fisk.datamanagement.mapper.MetadataMapAtlasMapper;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.tableconfig.SourceFieldDTO;
import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Component
@Slf4j
public class SynchronizationPgData {

    @Resource
    DataModelClient client;
    @Resource
    AtlasClient atlasClient;
    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    MetadataMapAtlasMapper metadataMapAtlasMapper;

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
    @Value("${fidata.database.ods}")
    private String ods;
    @Value("${fidata.database.dw}")
    private String dw;

    public void synchronizationPgData()
    {
        try {
            synchronizationInstance();
            synchronizationDb();
            synchronizationOds();
            synchronizationDw();
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
                    .eq(MetadataMapAtlasPO::getQualifiedName,fiDataName)
                    .eq(MetadataMapAtlasPO::getType, EntityTypeEnum.RDBMS_INSTANCE.getValue());
            MetadataMapAtlasPO po=metadataMapAtlasMapper.selectOne(queryWrapper);
            //判断实例是否已存在
            if (po ==null)
            {
                String addResult = addEntity(EntityTypeEnum.RDBMS_INSTANCE,null,"",null,null);
                if (addResult!="")
                {
                    //向MetadataMapAtlas表添加数据
                    String guid = addMetadataMapAtlas(addResult,
                            EntityTypeEnum.RDBMS_INSTANCE, fiDataName,
                            0,
                            0,
                            0,
                            0,
                            0,
                            "");
                    log.info("add entity instance name:",fiDataName+",guid:"+guid);
                    return;
                }
            }
            //存在,获取该实例详情,并判断是否需要修改
            updateEntity(EntityTypeEnum.RDBMS_INSTANCE,po,"","",null,null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error("synchronizationInstance ex:",e);
            return;
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
            MetadataMapAtlasPO instancePo=metadataMapAtlasMapper.selectOne(queryWrapper);
            if (instancePo==null)
            {
                return;
            }
            List<String> dbList=new ArrayList<>();
            dbList.add(ods);
            dbList.add(dw);
            QueryWrapper<MetadataMapAtlasPO> queryWrapper1=new QueryWrapper<>();
            queryWrapper1.lambda().eq(MetadataMapAtlasPO::getType, EntityTypeEnum.RDBMS_DB.getValue());
            List<MetadataMapAtlasPO> mapAtlasDbPOS=metadataMapAtlasMapper.selectList(queryWrapper1);
            //定义查询数据库名称类型,1:odw、2:dw
            int index=0;
            for (String db:dbList)
            {
                String dbQualifiedName=instancePo.qualifiedName+"_"+db;
                index+=1;
                int finalIndex = index;
                List<MetadataMapAtlasPO> dbPo=mapAtlasDbPOS.stream()
                        .filter(e->e.dbNameType== finalIndex).collect(Collectors.toList());
                //存在,判断是否修改
                if (!CollectionUtils.isEmpty(dbPo) && !dbPo.get(0).qualifiedName.equals(dbQualifiedName))
                {
                    updateEntity(EntityTypeEnum.RDBMS_DB,dbPo.get(0),db,dbQualifiedName,null,null);
                }
                else {
                    String addResult = addEntity(EntityTypeEnum.RDBMS_DB, instancePo, db,null,null);
                    if (addResult !="")
                    {
                        String guid = addMetadataMapAtlas(addResult,
                                EntityTypeEnum.RDBMS_DB,
                                db,
                                0,
                                0,
                                0,
                                0,
                                finalIndex,
                                instancePo.atlasGuid);
                        log.info("add entity db name:",db+",guid:"+guid);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error("synchronizationDb ex:",e);
            return;
        }
    }

    /**
     * 同步ods
     */
    public void synchronizationOds()
    {
        ResultEntity<List<DataAccessSourceTableDTO>> result = dataAccessClient.getDataAccessMetaData();
        if (result.code!=ResultEnum.SUCCESS.getCode() || CollectionUtils.isEmpty(result.data))
        {
            return;
        }
        List<SourceTableDTO> list=new ArrayList<>();
        for (DataAccessSourceTableDTO item:result.data)
        {
            SourceTableDTO dto = MetadataMapAtlasMap.INSTANCES.dtoToDto(item);
            dto.fieldList=MetadataMapAtlasMap.INSTANCES.fieldToDto(item.list);
            dto.fieldList=dto.fieldList.stream().distinct().collect(Collectors.toList());
            list.add(dto);
        }
        QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataMapAtlasPO::getDbNameType,1);
        MetadataMapAtlasPO po=metadataMapAtlasMapper.selectOne(queryWrapper);
        if (po==null)
        {
            return;
        }
        synchronizationData(list,po.qualifiedName, DataTypeEnum.DATA_INPUT.getValue());
        //delSynchronization(list,DataTypeEnum.DATA_INPUT.getValue());
    }

    /**
     * 同步dw
     */
    public void synchronizationDw()
    {
        ResultEntity<Object> result = client.getDataModelTable();
        if (result.code!=ResultEnum.SUCCESS.getCode())
        {
            return;
        }
        List<SourceTableDTO> list=JSON.parseArray(JSON.toJSONString(result.data),SourceTableDTO.class);
        QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataMapAtlasPO::getDbNameType,2);
        MetadataMapAtlasPO po=metadataMapAtlasMapper.selectOne(queryWrapper);
        if (po==null)
        {
            return;
        }
        synchronizationData(list,po.qualifiedName,DataTypeEnum.DATA_MODEL.getValue());
        //delSynchronization(list,DataTypeEnum.DATA_MODEL.getValue());
    }

    public void synchronizationData(List<SourceTableDTO> list, String dbName, int dataType)
    {
        try {
            QueryWrapper<MetadataMapAtlasPO> mapAtlasPOQueryWrapper=new QueryWrapper<>();
            mapAtlasPOQueryWrapper.lambda().eq(MetadataMapAtlasPO::getType,EntityTypeEnum.RDBMS_DB)
                    .eq(MetadataMapAtlasPO::getQualifiedName,dbName);
            MetadataMapAtlasPO dbPO=metadataMapAtlasMapper.selectOne(mapAtlasPOQueryWrapper);
            if (dbPO==null)
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
                        .eq(MetadataMapAtlasPO::getTableType,dto.type);
                MetadataMapAtlasPO po=metadataMapAtlasMapper.selectOne(queryWrapper);
                String qualifiedName=dbPO.qualifiedName+"_"+dto.tableName;
                if (po==null)
                {
                    String addResult = addEntity(EntityTypeEnum.RDBMS_TABLE, dbPO, dto.tableName, dto,null);
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
                            dbPO.atlasGuid);
                    log.info("add entity table name:",dto.tableName+",guid:"+tableGuid);
                    if (CollectionUtils.isEmpty(dto.fieldList))
                    {
                        continue;
                    }
                    po=metadataMapAtlasMapper.selectOne(queryWrapper);
                    for (SourceFieldDTO fieldDTO:dto.fieldList)
                    {

                        String fieldQualifiedName=qualifiedName+"_"+fieldDTO.fieldName;
                        String addColumnResult = addEntity(EntityTypeEnum.RDBMS_COLUMN, po, fieldDTO.fieldName, null, fieldDTO);
                        if (addColumnResult !="")
                        {
                            String columnGuid=addMetadataMapAtlas(addColumnResult,
                                    EntityTypeEnum.RDBMS_COLUMN,
                                    fieldQualifiedName,
                                    dataType,
                                    dto.id,
                                    fieldDTO.id,
                                    dto.type,
                                    0,
                                    tableGuid);
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
                                .eq(MetadataMapAtlasPO::getTableType,dto.type);
                        MetadataMapAtlasPO fieldData=metadataMapAtlasMapper.selectOne(queryWrapper1);
                        //不存在,则添加
                        if (fieldData==null)
                        {
                            String fieldQualifiedName=po.qualifiedName+"_"+field.fieldName;
                            String addColumnResult = addEntity(EntityTypeEnum.RDBMS_COLUMN, po, field.fieldName, null, field);
                            if (addColumnResult !="")
                            {
                                String columnGuid=addMetadataMapAtlas(addColumnResult,
                                        EntityTypeEnum.RDBMS_COLUMN,
                                        fieldQualifiedName,
                                        dataType,
                                        dto.id,
                                        field.id,
                                        dto.type,
                                        0,
                                        po.atlasGuid);
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
            e.printStackTrace();
            log.error("synchronizationData ex",e);
            return;
        }
    }

    /**
     * 删除元数据对象
     * @param list
     * @param dataType
     */
    public void delSynchronization(List<SourceTableDTO> list,int dataType)
    {
        QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(MetadataMapAtlasPO::getDataType,dataType)
                .eq(MetadataMapAtlasPO::getColumnId,0);

        List<MetadataMapAtlasPO> poList=metadataMapAtlasMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList))
        {
            return;
        }
        //删除表
        List<Integer> poIdList=(List)poList.stream().map(e->e.getTableId()).collect(Collectors.toList());
        List<Integer> dtoIdList=(List)list.stream().map(e->e.getId()).collect(Collectors.toList());
        //取差集
        poIdList.removeAll(dtoIdList);
        //atlas删除表元数据对象
        if (!CollectionUtils.isEmpty(poIdList))
        {
            List<MetadataMapAtlasPO> delList=poList.stream()
                    .filter(e->poIdList.contains(e.tableId))
                    .collect(Collectors.toList());
            for (MetadataMapAtlasPO item:delList )
            {
                ResultDataDTO<String> delAtlas = atlasClient.Delete(entityByGuid + "/" + item.atlasGuid);
                if (delAtlas.code !=ResultEnum.NO_CONTENT)
                {
                    continue;
                }
                UpdateMetadataMapAtlasDTO dto=new UpdateMetadataMapAtlasDTO();
                dto.id=item.tableId;
                dto.dataType=dataType;
                dto.tableType=item.tableType;
                metadataMapAtlasMapper.delBatchMetadataMapAtlas(dto);
            }
        }

        QueryWrapper<MetadataMapAtlasPO> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.lambda().eq(MetadataMapAtlasPO::getDataType,dataType);
        List<MetadataMapAtlasPO> dwMetadataList=metadataMapAtlasMapper.selectList(queryWrapper1);
        //atlas删除字段元数据对象
        for (SourceTableDTO dto:list)
        {
            //获取字段集合
            List<Integer> tableList=(List)dwMetadataList.stream()
                    .filter(e->e.tableId==dto.id && e.tableType==dto.type).map(e->e.getColumnId()).collect(Collectors.toList());
            //获取配置表字段集合
            List<Integer> fieldIdList=(List)dto.fieldList.stream()
                    .map(e->e.getId())
                    .collect(Collectors.toList());
            //取交集,获取已删除字段集合
            tableList.removeAll(fieldIdList);
            if (CollectionUtils.isEmpty(tableList))
            {
                continue;
            }
            List<MetadataMapAtlasPO> delList=dwMetadataList.stream()
                    .filter(e->tableList.contains(e.columnId) && e.tableType==dto.type)
                    .collect(Collectors.toList());
            for (MetadataMapAtlasPO mapAtlasPO:delList)
            {
                ResultDataDTO<String> delAtlas = atlasClient.Delete(entityByGuid + "/" + mapAtlasPO.atlasGuid);
                if (delAtlas.code !=ResultEnum.NO_CONTENT)
                {
                    continue;
                }
                metadataMapAtlasMapper.deleteByIdWithFill(mapAtlasPO);
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
                            SourceFieldDTO fieldDTO)
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
                attributesDTO.qualifiedName=fiDataName;
                attributesDTO.hostname =fiDataHostName;
                attributesDTO.port=fiDataPort;
                attributesDTO.platform=fiDataPlatform;
                attributesDTO.name=fiDataName;
                attributesDTO.protocol=fiDataProtocol;
                attributesDTO.rdbms_type=fiDataRdbmsType;
                attributesDTO.description=fiDataName;
                attributesDTO.comment=fiDataName;
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
        ResultDataDTO<String> addResult = atlasClient.Post(entity, jsonParameter);
        return addResult.code==ResultEnum.REQUEST_SUCCESS?addResult.data:"";
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
        ResultDataDTO<String> getDetail = atlasClient.Get(entityByGuid + "/" + po.atlasGuid);
        if (getDetail.code !=ResultEnum.REQUEST_SUCCESS)
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
                        || !dto.tableDes.equals(attribute.getString("description")))
                {
                    attribute.put("name",dto.tableName);
                    attribute.put("qualifiedName",qualifiedName);
                    attribute.put("description",dto.tableDes);
                    po.qualifiedName=fiDataName;
                    change=true;
                }
                break;
            case RDBMS_COLUMN:
                if (!fieldDTO.fieldName.equals(attribute.getString("name"))
                        || !qualifiedName.equals(attribute.getString("qualifiedName"))
                        //|| !field.fieldDes.equals(attribute1.getString("description"))
                        || !fieldDTO.equals(attribute.getString("length"))
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
        ResultDataDTO<String> result = atlasClient.Post(entity, jsonParameter);
        if (result.code !=ResultEnum.REQUEST_SUCCESS)
        {
            return;
        }
        metadataMapAtlasMapper.updateById(po);
    }

    /**
     * MetadataMapAtlas表添加数据
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
                                    String parentGuid)
    {
        try {
            JSONObject jsonObj = JSON.parseObject(jsonStr);
            JSONObject mutatedEntities = JSON.parseObject(jsonObj.getString("mutatedEntities"));
            JSONArray jsonArray=mutatedEntities.getJSONArray("CREATE");
            MetadataMapAtlasPO metadataMapAtlasPO=new MetadataMapAtlasPO();
            metadataMapAtlasPO.atlasGuid=jsonArray.getJSONObject(0).getString("guid");
            metadataMapAtlasPO.type=entityTypeEnum.getValue();
            metadataMapAtlasPO.qualifiedName=qualifiedName;
            metadataMapAtlasPO.parentAtlasGuid=parentGuid;
            metadataMapAtlasPO.dataType=dataType;
            metadataMapAtlasPO.columnId=columnId;
            metadataMapAtlasPO.tableId=tableId;
            metadataMapAtlasPO.tableType=tableType;
            metadataMapAtlasPO.dbNameType=dbNameType;
            int flat = metadataMapAtlasMapper.insert(metadataMapAtlasPO);
            return flat>0?metadataMapAtlasPO.atlasGuid:"";
        }
        catch (Exception e)
        {
            return "";
        }
    }

}
