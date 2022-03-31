package com.fisk.datamanagement.synchronization.fidata;

import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
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
import com.fisk.datamanagement.enums.DataTypeEnum;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.enums.TableTypeEnum;
import com.fisk.datamanagement.map.MetadataMapAtlasMap;
import com.fisk.datamanagement.mapper.MetadataMapAtlasMapper;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.utils.druid.AnalysisSqlHelper;
import com.fisk.datamanagement.vo.ResultDataDTO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.tableconfig.SourceFieldDTO;
import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;
import com.fisk.datamodel.enums.FactAttributeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Component
@Slf4j
public class SynchronizationKinShip {

    @Resource
    AtlasClient atlasClient;
    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    DataModelClient client;
    @Resource
    AnalysisSqlHelper analysisSqlHelper;
    @Resource
    MetadataMapAtlasMapper metadataMapAtlasMapper;
    @Resource
    SynchronizationData synchronizationPgData;

    @Value("${atlas.entity}")
    private String entity;
    @Value("${atlas.entityByGuid}")
    private String entityByGuid;
    @Value("${atlas.relationship}")
    private String relationship;
   @Value("${fidata.database.db}")
    private String db;

    private String dbType=JdbcConstants.POSTGRESQL_DRIVER;

    public void synchronizationKinShip()
    {
        //同步库血缘
        synchronizationDbKinShip();
        //同步ods表与dw血缘
        synchronizationPgTableKinShip();
        //同步dw表与doris血缘
        synchronizationDorisTableKinShip();
    }

    /**
     * 同步odw库与dw库血缘
     * 同步dw库与doris库血缘
     */
    public void synchronizationDbKinShip(){
        QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataMapAtlasPO::getType,EntityTypeEnum.RDBMS_DB.getValue());
        List<MetadataMapAtlasPO> poList=metadataMapAtlasMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList))
        {
            return;
        }
        for (MetadataMapAtlasPO item:poList)
        {
            if (item.dbNameType==DataTypeEnum.DATA_INPUT.getValue())
            {
                continue;
            }
            synchronizationPgDorisDbKinShip(item,poList);
        }
    }

    /**
     * 同步库血缘方法
     */
    public void synchronizationPgDorisDbKinShip(MetadataMapAtlasPO po,List<MetadataMapAtlasPO> poList)
    {
        //获取实体详情
        ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + po.atlasGuid);
        if (getDetail.code !=ResultEnum.REQUEST_SUCCESS)
        {
            return;
        }
        //解析数据
        JSONObject jsonObj = JSON.parseObject(getDetail.data);
        JSONObject entityObject= JSON.parseObject(jsonObj.getString("entity"));
        JSONObject relationShip=JSON.parseObject(entityObject.getString("relationshipAttributes"));
        JSONArray relationShipAttribute=JSON.parseArray(relationShip.getString("outputFromProcesses"));
        String[] dbList = db.split(",");
        int dbType=po.dbNameType==DataTypeEnum.DATA_MODEL.getValue()?DataTypeEnum.DATA_INPUT.getValue():DataTypeEnum.DATA_MODEL.getValue();
        String name=po.dbNameType==DataTypeEnum.DATA_MODEL.getValue()?dbList[1]:dbList[2];
        //条数为0,则添加process
        if (relationShipAttribute.size()==0)
        {
            Optional<MetadataMapAtlasPO> po1 = poList.stream().filter(e -> e.dbNameType ==dbType).findFirst();
            if (!po1.isPresent())
            {
                return;
            }
            List<EntityIdAndTypeDTO> list=new ArrayList<>();
            EntityIdAndTypeDTO dto=new EntityIdAndTypeDTO();
            dto.guid=po1.get().atlasGuid;
            dto.typeName=EntityTypeEnum.RDBMS_DB.getName();
            list.add(dto);
            addProcess(EntityTypeEnum.PROCESS,name,list,po.atlasGuid,0);
        }
    }

    /**
     * 同步ods表与dw血缘
     */
    public void synchronizationPgTableKinShip(){
        try {
            //获取dw库表结构
            ResultEntity<Object> result = client.getDataModelTable(1);
            if (result.code!= ResultEnum.SUCCESS.getCode())
            {
                return;
            }
            //序列化
            List<SourceTableDTO> list= JSON.parseArray(JSON.toJSONString(result.data),SourceTableDTO.class);
            //获取数据建模MetadataMapAtlas配置表数据
            QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.lambda().eq(MetadataMapAtlasPO::getDataType, DataTypeEnum.DATA_MODEL);
            List<MetadataMapAtlasPO> poList=metadataMapAtlasMapper.selectList(queryWrapper);
            //判断是否为空
            if (CollectionUtils.isEmpty(list) || CollectionUtils.isEmpty(poList))
            {
                return;
            }
            //过滤sql脚本为空的数据
            list=list.stream().filter(e-> Objects.nonNull(e.sqlScript)).collect(Collectors.toList());
            //获取ods表结构
            ResultEntity<List<DataAccessSourceTableDTO>> odsResult = dataAccessClient.getDataAccessMetaData();
            if (result.code!=ResultEnum.SUCCESS.getCode() || CollectionUtils.isEmpty(odsResult.data))
            {
                return;
            }
            for (SourceTableDTO item:list)
            {
                //根据guid,获取配置表信息
                Optional<MetadataMapAtlasPO> data = poList.stream().filter(e -> e.tableId == item.id && e.columnId==0).findFirst();
                if (!data.isPresent())
                {
                    continue;
                }
                String atlasGuid=data.get().atlasGuid;
                //获取实体详情
                ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + atlasGuid);
                if (getDetail.code !=ResultEnum.REQUEST_SUCCESS)
                {
                    return;
                }
                //解析SQL,获取来源表集合
                List<String> tableNameList = analysisSqlHelper.analysisTableSql(item.sqlScript, dbType);
                //获取输入参数集合
                List<EntityIdAndTypeDTO> tableList = getTableList(tableNameList, odsResult.data,item);
                //解析数据
                JSONObject jsonObj = JSON.parseObject(getDetail.data);
                JSONObject entityObject= JSON.parseObject(jsonObj.getString("entity"));
                JSONObject relationShip=JSON.parseObject(entityObject.getString("relationshipAttributes"));
                JSONArray relationShipAttribute=JSON.parseArray(relationShip.getString("outputFromProcesses"));
                //条数为0,则添加process
                if (relationShipAttribute.size()==0)
                {
                    addProcess(EntityTypeEnum.RDBMS_TABLE,item.sqlScript,tableList,atlasGuid,(int)item.id);
                }
                else {
                    List<SourceTableDTO> sourceTableDtoList =new ArrayList<>();
                    for (DataAccessSourceTableDTO tableDTO:odsResult.data)
                    {
                        SourceTableDTO dto = MetadataMapAtlasMap.INSTANCES.dtoToDto(tableDTO);
                        dto.fieldList=MetadataMapAtlasMap.INSTANCES.fieldToDto(tableDTO.list);
                        dto.fieldList=dto.fieldList.stream().distinct().collect(Collectors.toList());
                        sourceTableDtoList.add(dto);
                    }
                    for (int i=0;i<relationShipAttribute.size();i++)
                    {
                        updateProcess(
                                relationShipAttribute.getJSONObject(i).getString("guid"),
                                tableList,
                                sourceTableDtoList,
                                EntityTypeEnum.RDBMS_TABLE,
                                (int)item.id,
                                item.sqlScript,
                                atlasGuid
                        );
                    }
                }
                //同步字段
                synchronizationPgColumnKinShip(item);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error("synchronizationPgTableKinShip ex:",e);
        }
    }

    /**
     * 同步ods表字段与dw表字段血缘关系
     * @param dto
     */
    public void synchronizationPgColumnKinShip(SourceTableDTO dto)
    {
        //获取接入数据
        ResultEntity<List<DataAccessSourceTableDTO>> odsResult = dataAccessClient.getDataAccessMetaData();
        if (odsResult.code!=ResultEnum.SUCCESS.getCode() || CollectionUtils.isEmpty(odsResult.data))
        {
            return;
        }
        List<DataAccessSourceTableDTO> list=odsResult.data;
        for (SourceFieldDTO field:dto.fieldList)
        {
            //获取该表集合
            Optional<DataAccessSourceTableDTO> sourceTable = list.stream().filter(e ->e.tableName.toLowerCase().equals(field.sourceTable)).findFirst();
            if (!sourceTable.isPresent())
            {
                continue;
            }
            QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.lambda().eq(MetadataMapAtlasPO::getColumnId,field.id)
                    .eq(MetadataMapAtlasPO::getTableType,dto.type);
            MetadataMapAtlasPO po=metadataMapAtlasMapper.selectOne(queryWrapper);
            if (po==null)
            {
                continue;
            }
            List<EntityIdAndTypeDTO> inputColumnList = getDorisInputColumnList(sourceTable, field);
            //获取实体详情
            ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + po.atlasGuid);
            if (getDetail.code !=ResultEnum.REQUEST_SUCCESS)
            {
                continue;
            }
            JSONObject jsonObj = JSON.parseObject(getDetail.data);
            JSONObject entityObject= JSON.parseObject(jsonObj.getString("entity"));
            JSONObject relationShip=JSON.parseObject(entityObject.getString("relationshipAttributes"));
            JSONArray relationShipAttribute=JSON.parseArray(relationShip.getString("outputFromProcesses"));
            //条数为0,则添加process
            if (relationShipAttribute.size()==0)
            {
                addProcess(EntityTypeEnum.RDBMS_COLUMN,dto.sqlScript,inputColumnList,po.atlasGuid,(int)field.id);
            }else {
                List<SourceTableDTO> dtoList=new ArrayList<>();
                dtoList.add(dto);
                for (int i=0;i<relationShipAttribute.size();i++)
                {
                    updateProcess(
                            relationShipAttribute.getJSONObject(i).getString("guid"),
                            inputColumnList,
                            dtoList,
                            EntityTypeEnum.RDBMS_COLUMN,
                            (int)field.id,
                            dto.sqlScript,
                            po.atlasGuid
                    );
                }
            }
        }
    }

    public List<EntityIdAndTypeDTO> getDorisInputColumnList(Optional<DataAccessSourceTableDTO> sourceTable,
                                                            SourceFieldDTO field)
    {
        List<EntityIdAndTypeDTO> list=new ArrayList<>();
        Optional<DataAccessSourceFieldDTO> sourceField = sourceTable.get().list.stream()
                .filter(e->field.fieldName.equals(e.fieldName.toLowerCase())).findFirst();
        if (!sourceField.isPresent())
        {
            return list;
        }
        QueryWrapper<MetadataMapAtlasPO> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.lambda().eq(MetadataMapAtlasPO::getColumnId, sourceField.get().id)
                .eq(MetadataMapAtlasPO::getDataType, DataTypeEnum.DATA_INPUT);
        MetadataMapAtlasPO po1=metadataMapAtlasMapper.selectOne(queryWrapper1);
        if (po1==null)
        {
            return list;
        }
        EntityIdAndTypeDTO dto1=new EntityIdAndTypeDTO();
        dto1.guid=po1.atlasGuid;
        dto1.typeName=EntityTypeEnum.RDBMS_COLUMN.getName();
        list.add(dto1);
        //关联维度,添加维度血缘关联
        if (field.associatedDim)
        {
            QueryWrapper<MetadataMapAtlasPO> queryWrapper2=new QueryWrapper<>();
            queryWrapper2.lambda()
                    .eq(MetadataMapAtlasPO::getTableType, TableTypeEnum.DW_DIMENSION)
                    .eq(MetadataMapAtlasPO::getColumnId,field.associatedDimAttributeId);
            MetadataMapAtlasPO mapAtlasPo =metadataMapAtlasMapper.selectOne(queryWrapper2);
            if (mapAtlasPo ==null)
            {
                return list;
            }
            EntityIdAndTypeDTO field1=new EntityIdAndTypeDTO();
            field1.guid= mapAtlasPo.atlasGuid;
            field1.typeName=EntityTypeEnum.RDBMS_TABLE.getName();
            list.add(field1);
        }
        return list;
    }

    /**
     * 同步dw表与doris血缘
     */
    public void synchronizationDorisTableKinShip()
    {
        try {
            //获取dw库表结构
            ResultEntity<Object> result = client.getDataModelTable(2);
            if (result.code!= ResultEnum.SUCCESS.getCode())
            {
                return;
            }
            //序列化
            List<SourceTableDTO> list= JSON.parseArray(JSON.toJSONString(result.data),SourceTableDTO.class);
            ////list=list.stream().filter(e->e.tableName.equals("fact_User_Info")).collect(Collectors.toList());
            //获取数据建模MetadataMapAtlas配置表数据
            QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.lambda().eq(MetadataMapAtlasPO::getDataType, DataTypeEnum.DATA_DORIS);
            List<MetadataMapAtlasPO> poList=metadataMapAtlasMapper.selectList(queryWrapper);
            //判断是否为空
            if (CollectionUtils.isEmpty(list) || CollectionUtils.isEmpty(poList))
            {
                return;
            }
            for (SourceTableDTO item:list)
            {
                //根据guid,获取配置表信息
                Optional<MetadataMapAtlasPO> data = poList.stream().filter(e ->
                        e.tableId == item.id
                        && e.columnId==0
                        && e.tableType==item.type
                        && e.type==EntityTypeEnum.RDBMS_TABLE.getValue()
                ).findFirst();
                if (!data.isPresent())
                {
                    continue;
                }
                String atlasGuid=data.get().atlasGuid;
                //获取实体详情
                ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + atlasGuid);
                if (getDetail.code !=ResultEnum.REQUEST_SUCCESS)
                {
                    return;
                }
                //获取输入参数集合
                List<EntityIdAndTypeDTO> tableList = getDorisInputTableList(item);
                JSONObject jsonObj = JSON.parseObject(getDetail.data);
                JSONObject entityObject= JSON.parseObject(jsonObj.getString("entity"));
                JSONObject relationShip=JSON.parseObject(entityObject.getString("relationshipAttributes"));
                JSONArray relationShipAttribute=JSON.parseArray(relationShip.getString("outputFromProcesses"));
                //条数为0,则添加process
                if (relationShipAttribute.size()==0)
                {
                    addProcess(EntityTypeEnum.RDBMS_TABLE,"doris_"+item.sqlScript,tableList,atlasGuid,(int)item.id);
                }else {
                    for (int i=0;i<relationShipAttribute.size();i++)
                    {
                        updateProcess(
                                relationShipAttribute.getJSONObject(i).getString("guid"),
                                tableList,
                                list,
                                EntityTypeEnum.RDBMS_TABLE,
                                (int)item.id,
                                item.sqlScript,
                                atlasGuid
                        );
                    }
                }
                //同步字段血缘
                synchronizationDorisColumnKinShip(item);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public List<EntityIdAndTypeDTO> getDorisInputTableList(SourceTableDTO dto)
    {
        int tableType=dto.type==3?1:2;
        List<EntityIdAndTypeDTO> list=new ArrayList<>();
        QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataMapAtlasPO::getTableId,dto.id)
                .eq(MetadataMapAtlasPO::getColumnId,0)
                .eq(MetadataMapAtlasPO::getDataType,DataTypeEnum.DATA_MODEL)
                .eq(MetadataMapAtlasPO::getType,EntityTypeEnum.RDBMS_TABLE)
                .eq(MetadataMapAtlasPO::getTableType,tableType);
        MetadataMapAtlasPO po=metadataMapAtlasMapper.selectOne(queryWrapper);
        if (po==null)
        {
            return list;
        }
        EntityIdAndTypeDTO data=new EntityIdAndTypeDTO();
        data.guid=po.atlasGuid;
        data.typeName=EntityTypeEnum.RDBMS_TABLE.getName();
        list.add(data);
        List<Integer> associateIdList;
        if (dto.type==3)
        {
            associateIdList=dto.fieldList.stream().filter(e->e.associatedDim==true).map(e->e.getAssociatedDimId()).collect(Collectors.toList());
        }else {
            associateIdList=(List)dto.fieldList.stream().filter(e->e.attributeType==2).map(e->e.getAssociatedDimId()).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(associateIdList))
        {
            return list;
        }
        List<Integer> collect = associateIdList.stream().distinct().collect(Collectors.toList());
        int dorisType=dto.type==4?3:2;
        for (Integer id:collect)
        {
            QueryWrapper<MetadataMapAtlasPO> queryWrapper1=new QueryWrapper<>();
            queryWrapper1.lambda()
                    .eq(MetadataMapAtlasPO::getDataType,dorisType)
                    .eq(MetadataMapAtlasPO::getTableId,id)
                    .eq(MetadataMapAtlasPO::getType,dto.type)
                    .eq(MetadataMapAtlasPO::getType,EntityTypeEnum.RDBMS_TABLE)
                    .eq(MetadataMapAtlasPO::getColumnId,0);
            MetadataMapAtlasPO mapAtlasPo =metadataMapAtlasMapper.selectOne(queryWrapper1);
            if (mapAtlasPo ==null)
            {
                continue;
            }
            EntityIdAndTypeDTO field=new EntityIdAndTypeDTO();
            field.guid= mapAtlasPo.atlasGuid;
            field.typeName=EntityTypeEnum.RDBMS_TABLE.getName();
            list.add(field);
        }
        return list;
    }

    /**
     * 同步dw表字段与doris表字段血缘关系
     */
    public void synchronizationDorisColumnKinShip(SourceTableDTO dto)
    {
        //获取dw库表结构
        ResultEntity<Object> result = client.getDataModelTable(1);
        if (result.code!= ResultEnum.SUCCESS.getCode())
        {
            return;
        }
        //序列化
        List<SourceTableDTO> list= JSON.parseArray(JSON.toJSONString(result.data),SourceTableDTO.class);
        //获取该表集合
        Optional<SourceTableDTO> sourceTable = list.stream().filter(e -> e.tableName.equals(dto.tableName)).findFirst();
        if (!sourceTable.isPresent())
        {
            return;
        }
        for (SourceFieldDTO field:dto.fieldList)
        {
            QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
            if (field.id==0)
            {
                queryWrapper.lambda().eq(MetadataMapAtlasPO::getColumnId,0)
                        .eq(MetadataMapAtlasPO::getType,EntityTypeEnum.RDBMS_COLUMN.getValue())
                        .eq(MetadataMapAtlasPO::getTableType,dto.type)
                        .eq(MetadataMapAtlasPO::getTableId,dto.id);
            }
            else {
                queryWrapper.lambda().eq(MetadataMapAtlasPO::getColumnId,field.id)
                        .eq(MetadataMapAtlasPO::getTableId,dto.id)
                        .eq(MetadataMapAtlasPO::getTableType,dto.type);
            }
            MetadataMapAtlasPO po=metadataMapAtlasMapper.selectOne(queryWrapper);
            if (po==null)
            {
                continue;
            }
            List<EntityIdAndTypeDTO> inputColumnList = getDorisProcessInput(sourceTable, field);
            if (CollectionUtils.isEmpty(inputColumnList))
            {
                continue;
            }
            //获取实体详情
            ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + po.atlasGuid);
            if (getDetail.code !=ResultEnum.REQUEST_SUCCESS)
            {
                continue;
            }
            JSONObject jsonObj = JSON.parseObject(getDetail.data);
            JSONObject entityObject= JSON.parseObject(jsonObj.getString("entity"));
            JSONObject relationShip=JSON.parseObject(entityObject.getString("relationshipAttributes"));
            JSONArray relationShipAttribute=JSON.parseArray(relationShip.getString("outputFromProcesses"));
            String processName=dto.sqlScript;
            if (field.attributeType== FactAttributeEnum.MEASURE.getValue())
            {
                processName=field.calculationLogic;
            }
            //条数为0,则添加process
            if (relationShipAttribute.size()==0)
            {
                addProcess(EntityTypeEnum.RDBMS_COLUMN,processName,inputColumnList,po.atlasGuid,(int)field.id);
            }else {
                List<SourceTableDTO> dtoList=new ArrayList<>();
                dtoList.add(dto);
                for (int i=0;i<relationShipAttribute.size();i++)
                {
                    updateProcess(
                            relationShipAttribute.getJSONObject(i).getString("guid"),
                            inputColumnList,
                            dtoList,
                            EntityTypeEnum.RDBMS_COLUMN,
                            (int)field.id,
                            processName,
                            po.atlasGuid
                    );
                }
            }
        }
    }

    public List<EntityIdAndTypeDTO> getDorisProcessInput(Optional<SourceTableDTO> sourceTable,SourceFieldDTO field)
    {
        List<EntityIdAndTypeDTO> list=new ArrayList<>();
        //退化维度
        if (field.attributeType==FactAttributeEnum.DEGENERATION_DIMENSION.getValue())
        {
            Optional<SourceFieldDTO> sourceField = sourceTable.get().fieldList.stream()
                    .filter(e -> field.fieldName.equals(e.fieldName)).findFirst();
            if (!sourceField.isPresent())
            {
                return list;
            }
            QueryWrapper<MetadataMapAtlasPO> queryWrapper1=new QueryWrapper<>();
            //判断是否为维度key
            if (sourceField.get().id==0)
            {
                queryWrapper1.lambda().eq(MetadataMapAtlasPO::getColumnId,0)
                        .eq(MetadataMapAtlasPO::getTableType,sourceTable.get().type)
                        .eq(MetadataMapAtlasPO::getTableId,sourceTable.get().id)
                        .eq(MetadataMapAtlasPO::getType, EntityTypeEnum.RDBMS_COLUMN);
            }
            else {
                queryWrapper1.lambda().eq(MetadataMapAtlasPO::getColumnId, sourceField.get().id)
                        .eq(MetadataMapAtlasPO::getTableId,sourceTable.get().id)
                        .eq(MetadataMapAtlasPO::getDataType, DataTypeEnum.DATA_MODEL);
            }
            MetadataMapAtlasPO po1=metadataMapAtlasMapper.selectOne(queryWrapper1);
            if (po1==null)
            {
                return list;
            }
            EntityIdAndTypeDTO dto1=new EntityIdAndTypeDTO();
            dto1.guid=po1.atlasGuid;
            dto1.typeName=EntityTypeEnum.RDBMS_COLUMN.getName();
            list.add(dto1);
        }
        //关联维度
        else if (field.attributeType==FactAttributeEnum.DIMENSION_KEY.getValue())
        {
            //截取关联维度表名称
            String tableName="dim_"+field.fieldName.replace("_key","");
            //获取dw库表结构
            ResultEntity<Object> result = client.getDataModelTable(1);
            if (result.code!= ResultEnum.SUCCESS.getCode())
            {
                return list;
            }
            //序列化
            List<SourceTableDTO> data= JSON.parseArray(JSON.toJSONString(result.data),SourceTableDTO.class);
            Optional<SourceTableDTO> dimensionTable = data.stream().filter(e -> e.tableName.equals(tableName)).findFirst();
            if (!dimensionTable.isPresent())
            {
                return list;
            }
            Optional<SourceFieldDTO> dimensionField = dimensionTable.get().fieldList.stream().filter(e -> e.id == field.associatedDimId).findFirst();
            if (!dimensionField.isPresent())
            {
                return list;
            }
            QueryWrapper<MetadataMapAtlasPO> queryWrapper1=new QueryWrapper<>();
            queryWrapper1.lambda()
                    .eq(MetadataMapAtlasPO::getTableId,field.id)
                    .eq(MetadataMapAtlasPO::getColumnId,0)
                    .eq(MetadataMapAtlasPO::getTableType,TableTypeEnum.DORIS_DIMENSION.getValue())
                    .eq(MetadataMapAtlasPO::getType,EntityTypeEnum.RDBMS_COLUMN.getValue());
            MetadataMapAtlasPO po1=metadataMapAtlasMapper.selectOne(queryWrapper1);
            if (po1==null)
            {
                return list;
            }
            EntityIdAndTypeDTO dto1=new EntityIdAndTypeDTO();
            dto1.guid=po1.atlasGuid;
            dto1.typeName=EntityTypeEnum.RDBMS_COLUMN.getName();
            list.add(dto1);
        }
        //原子指标
        else {
            Optional<SourceFieldDTO> sourceField = sourceTable.get().fieldList.stream()
                    .filter(e -> field.sourceField.equals(e.fieldName.toLowerCase())).findFirst();
            if (!sourceField.isPresent())
            {
                return list;
            }
            QueryWrapper<MetadataMapAtlasPO> queryWrapper1=new QueryWrapper<>();
            queryWrapper1.lambda().eq(MetadataMapAtlasPO::getColumnId,sourceField.get().id)
                    .eq(MetadataMapAtlasPO::getTableType,TableTypeEnum.DW_FACT)
                    .eq(MetadataMapAtlasPO::getDataType, DataTypeEnum.DATA_MODEL);
            MetadataMapAtlasPO po1=metadataMapAtlasMapper.selectOne(queryWrapper1);
            if (po1==null)
            {
                return list;
            }
            EntityIdAndTypeDTO dto1=new EntityIdAndTypeDTO();
            dto1.guid=po1.atlasGuid;
            dto1.typeName=EntityTypeEnum.RDBMS_COLUMN.getName();
            list.add(dto1);
        }
        return list;
    }

    /**
     * 添加process
     * @param sql
     * @param tableList
     * @param atlasGuid
     */
    public void addProcess(EntityTypeEnum entityTypeEnum,
                           String sql,
                           List<EntityIdAndTypeDTO> tableList,
                           String atlasGuid,
                           int dataInputId
                           )
    {
        //去除换行符,以及转小写
        sql=sql.replace("\n","").toLowerCase();
        //组装参数
        EntityDTO entityDTO=new EntityDTO();
        EntityTypeDTO entityTypeDTO=new EntityTypeDTO();
        entityTypeDTO.typeName= EntityTypeEnum.PROCESS.getName();
        EntityAttributesDTO attributesDTO=new EntityAttributesDTO();
        attributesDTO.comment="";
        attributesDTO.description=sql;
        attributesDTO.owner="root";
        attributesDTO.qualifiedName=sql+"_"+atlasGuid;
        attributesDTO.contact_info="root";
        attributesDTO.name=sql;
        //输入参数
        attributesDTO.inputs=tableList;
        //输出参数
        List<EntityIdAndTypeDTO> dtoList=new ArrayList<>();
        EntityIdAndTypeDTO dto=new EntityIdAndTypeDTO();
        dto.typeName=entityTypeEnum.getName();
        dto.guid=atlasGuid;
        dtoList.add(dto);
        attributesDTO.outputs=dtoList;
        entityTypeDTO.attributes=attributesDTO;
        //检验输入和输出参数是否有值
        if (CollectionUtils.isEmpty(attributesDTO.inputs) || CollectionUtils.isEmpty(attributesDTO.outputs))
        {
            return;
        }
        entityDTO.entity=entityTypeDTO;
        String jsonParameter= JSONArray.toJSON(entityDTO).toString();
        //调用atlas添加血缘
        ResultDataDTO<String> addResult = atlasClient.post(entity, jsonParameter);
        if (addResult.code!=ResultEnum.REQUEST_SUCCESS)
        {
            return;
        }
        int tableId=0;
        int columnId=0;
        if (entityTypeEnum==EntityTypeEnum.RDBMS_TABLE)
        {
            tableId=dataInputId;
        }else {
            columnId=dataInputId;
        }
        synchronizationPgData.addMetadataMapAtlas(
                addResult.data,
                EntityTypeEnum.PROCESS,
                sql,
                DataTypeEnum.DATA_MODEL.getValue(),
                tableId,
                columnId,
                0,
                0,
                dto.guid,
                "",
                0);
    }

    /**
     * 更新process
     * @param processGuid
     * @param inputList
     * @param dtoList
     * @param entityTypeEnum
     * @param dataInputId
     * @param sqlScript
     * @param atlasGuid
     */
    public void updateProcess(String processGuid,
                              List<EntityIdAndTypeDTO> inputList,
                              List<SourceTableDTO> dtoList,
                              EntityTypeEnum entityTypeEnum,
                              int dataInputId,
                              String sqlScript,
                              String atlasGuid
    )
    {
        try {
            //查询是否为定时服务生成的process
            QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.lambda().eq(MetadataMapAtlasPO::getAtlasGuid,processGuid);
            MetadataMapAtlasPO po=metadataMapAtlasMapper.selectOne(queryWrapper);
            if (po==null)
            {
                return;
            }
            //获取process详情
            ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + processGuid);
            if (getDetail.code !=ResultEnum.REQUEST_SUCCESS)
            {
                return;
            }
            //序列化获取数据
            ProcessDTO dto=JSONObject.parseObject(getDetail.data,ProcessDTO.class);
            //判断process是否已删除
            if (EntityTypeEnum.DELETED.getName().equals(dto.entity.status))
            {
                //如果已删除,则重新添加
                addProcess(entityTypeEnum,sqlScript,inputList,atlasGuid,dataInputId);
                return;
            }
            List<String> inputGuidList=dto.entity.attributes.inputs.stream().map(e->e.getGuid()).collect(Collectors.toList());
            //循环判断是否添加output参数
            for (EntityIdAndTypeDTO item:inputList)
            {
                if (inputGuidList.contains(item.guid))
                {
                    continue;
                }
                //不存在,则添加
                QueryWrapper<MetadataMapAtlasPO> queryWrapper1=new QueryWrapper<>();
                queryWrapper1.lambda().eq(MetadataMapAtlasPO::getAtlasGuid,item.guid);
                MetadataMapAtlasPO po1=metadataMapAtlasMapper.selectOne(queryWrapper1);
                if (po1==null)
                {
                    continue;
                }
                //获取表名
                Optional<SourceTableDTO> first = dtoList.stream().filter(e -> e.id == po1.tableId).findFirst();
                if (!first.isPresent())
                {
                    continue;
                }
                ProcessAttributesPutDTO attributesPutDTO=new ProcessAttributesPutDTO();
                attributesPutDTO.guid=item.guid;
                attributesPutDTO.typeName=EntityTypeEnum.RDBMS_TABLE.getName();
                ProcessUniqueAttributesDTO uniqueAttributes=new ProcessUniqueAttributesDTO();
                uniqueAttributes.qualifiedName=po.qualifiedName;
                attributesPutDTO.uniqueAttributes=uniqueAttributes;
                dto.entity.attributes.inputs.add(attributesPutDTO);

                String relationShipGuid = addRelationShip(dto.entity.guid, dto.entity.attributes.qualifiedName, item.guid, po1.qualifiedName);
                if (relationShipGuid=="")
                {
                    continue;
                }
                ProcessRelationshipAttributesPutDTO inputDTO=new ProcessRelationshipAttributesPutDTO();
                inputDTO.guid=item.guid;
                inputDTO.typeName=EntityTypeEnum.RDBMS_TABLE.getName();
                inputDTO.entityStatus=EntityTypeEnum.ACTIVE.getName();
                //表名
                inputDTO.displayText=first.get().tableName;
                inputDTO.relationshipType=EntityTypeEnum.DATASET_PROCESS_INPUTS.getName();
                //生成的relationShip
                inputDTO.relationshipGuid=relationShipGuid;
                inputDTO.relationshipStatus=EntityTypeEnum.ACTIVE.getName();
                ProcessRelationShipAttributesTypeNameDTO attributesDTO=new ProcessRelationShipAttributesTypeNameDTO();
                attributesDTO.typeName=EntityTypeEnum.DATASET_PROCESS_INPUTS.getName();
                inputDTO.relationshipAttributes=attributesDTO;
                dto.entity.relationshipAttributes.inputs.add(inputDTO);
            }
            //取差集
            List<String> ids=dto.entity.attributes.inputs.stream().map(e->e.guid).collect(Collectors.toList());
            List<String> ids2=inputList.stream().map(e->e.guid).collect(Collectors.toList());
            ids.removeAll(ids2);
            //过滤已删除关联实体
            if (!CollectionUtils.isEmpty(ids))
            {
                dto.entity.attributes.inputs=dto.entity.attributes.inputs
                        .stream()
                        .filter(e->!ids.contains(e.guid))
                        .collect(Collectors.toList());
                dto.entity.relationshipAttributes.inputs =dto.entity.relationshipAttributes.inputs
                        .stream()
                        .filter(e->!ids.contains(e.guid))
                        .collect(Collectors.toList());
            }
            dto.entity.attributes.name=sqlScript;
            //修改process
            String jsonParameter= JSONArray.toJSON(dto).toString();
            //调用atlas修改实例
            atlasClient.post(entity, jsonParameter);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error("updateProcess ex:",e);
        }
    }

    /**
     * 添加血缘关系连线
     * @param end1Guid
     * @param end1QualifiedName
     * @param end2Guid
     * @param end2QualifiedName
     * @return
     */
    public String addRelationShip(String end1Guid,String end1QualifiedName,String end2Guid,String end2QualifiedName)
    {
        RelationshipDTO dto=new RelationshipDTO();
        dto.typeName=EntityTypeEnum.DATASET_PROCESS_INPUTS.getName();
        ProcessAttributesPutDTO end1=new ProcessAttributesPutDTO();
        end1.guid=end1Guid;
        end1.typeName=end1QualifiedName;
        ProcessUniqueAttributesDTO attributesDTO=new ProcessUniqueAttributesDTO();
        attributesDTO.qualifiedName=end1QualifiedName;
        end1.uniqueAttributes=attributesDTO;
        dto.end1=end1;

        ProcessAttributesPutDTO end2=new ProcessAttributesPutDTO();
        end2.guid=end2Guid;
        end2.typeName=end2QualifiedName;
        ProcessUniqueAttributesDTO attributesDto2 =new ProcessUniqueAttributesDTO();
        attributesDto2.qualifiedName=end2QualifiedName;
        end2.uniqueAttributes= attributesDto2;
        dto.end2=end2;

        String jsonParameter= JSONArray.toJSON(dto).toString();
        //调用atlas添加血缘关系连线
        ResultDataDTO<String> addResult = atlasClient.post(relationship, jsonParameter);
        if (addResult.code !=ResultEnum.REQUEST_SUCCESS)
        {
            return "";
        }
        JSONObject data=JSONObject.parseObject(addResult.data);
        return data.getString("guid");
    }

    /**
     * 拼接process输入参数
     * @param tableNameList
     * @param dtoList
     * @param associateDto
     * @return
     */
    public List<EntityIdAndTypeDTO> getTableList(List<String> tableNameList,
                                                 List<DataAccessSourceTableDTO> dtoList,
                                                 SourceTableDTO associateDto)
    {
        List<EntityIdAndTypeDTO> list=new ArrayList<>();

        List<Integer> ids=(List)dtoList.stream()
                .filter(e->tableNameList.contains(e.tableName))
                .map(e->e.id).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids))
        {
            return list;
        }
        QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.in("table_id",ids).lambda()
                .eq(MetadataMapAtlasPO::getDataType,DataTypeEnum.DATA_INPUT)
                .eq(MetadataMapAtlasPO::getColumnId,0);
        List<MetadataMapAtlasPO> poList=metadataMapAtlasMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList))
        {
            return list;
        }
        for (MetadataMapAtlasPO item:poList)
        {
            EntityIdAndTypeDTO dto=new EntityIdAndTypeDTO();
            dto.guid=item.atlasGuid;
            dto.typeName=EntityTypeEnum.RDBMS_TABLE.getName();
            list.add(dto);
        }
        List<Integer> associateIdList=associateDto.fieldList.stream().filter(e->e.associatedDim==true).map(e->e.getAssociatedDimId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(associateIdList))
        {
            return list;
        }
        List<Integer> collect = associateIdList.stream().distinct().collect(Collectors.toList());
        for (Integer id:collect)
        {
            QueryWrapper<MetadataMapAtlasPO> queryWrapper1=new QueryWrapper<>();
            queryWrapper1.lambda().eq(MetadataMapAtlasPO::getDataType,DataTypeEnum.DATA_MODEL)
                    .eq(MetadataMapAtlasPO::getType,EntityTypeEnum.RDBMS_TABLE)
                    .eq(MetadataMapAtlasPO::getTableId,id)
                    .eq(MetadataMapAtlasPO::getTableType,1)
                    .eq(MetadataMapAtlasPO::getColumnId,0);
            MetadataMapAtlasPO mapAtlasPo =metadataMapAtlasMapper.selectOne(queryWrapper1);
            if (mapAtlasPo ==null)
            {
                continue;
            }
            EntityIdAndTypeDTO dto=new EntityIdAndTypeDTO();
            dto.guid= mapAtlasPo.atlasGuid;
            dto.typeName=EntityTypeEnum.RDBMS_TABLE.getName();
            list.add(dto);
        }
        return list;
    }

}
