package com.fisk.datamanagement.synchronization.fidata;

import com.alibaba.druid.util.JdbcConstants;
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
import com.fisk.datamanagement.dto.process.*;
import com.fisk.datamanagement.dto.relationship.RelationshipDTO;
import com.fisk.datamanagement.entity.MetadataMapAtlasPO;
import com.fisk.datamanagement.enums.DataTypeEnum;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.mapper.MetadataMapAtlasMapper;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.utils.druid.AnalysisSqlHelper;
import com.fisk.datamanagement.vo.ResultDataDTO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
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
public class SynchronizationPgKinShip {

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
    SynchronizationPgData synchronizationPgData;

    @Value("${atlas.entity}")
    private String entity;
    @Value("${atlas.entityByGuid}")
    private String entityByGuid;
    @Value("${atlas.relationship}")
    private String relationship;
    @Value("${fidata.database.dw}")
    private String dw;

    private String dbType=JdbcConstants.POSTGRESQL_DRIVER;

    public void synchronizationKinShip()
    {
        //同步dw与ods血缘
        synchronizationPgDbKinShip();
        //同步dw表与ods血缘
        synchronizationPgTableKinShip();
    }

    /**
     * 同步odw与dw血缘
     */
    public void synchronizationPgDbKinShip()
    {
        QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataMapAtlasPO::getType,EntityTypeEnum.RDBMS_DB.getValue());
        List<MetadataMapAtlasPO> poList=metadataMapAtlasMapper.selectList(queryWrapper);
        Optional<MetadataMapAtlasPO> po = poList.stream().filter(e -> e.dbNameType == 2).findFirst();
        if (po==null)
        {
            return;
        }
        //获取实体详情
        ResultDataDTO<String> getDetail = atlasClient.Get(entityByGuid + "/" + po.get().atlasGuid);
        if (getDetail.code !=ResultEnum.REQUEST_SUCCESS)
        {
            return;
        }
        //解析数据
        JSONObject jsonObj = JSON.parseObject(getDetail.data);
        JSONObject entityObject= JSON.parseObject(jsonObj.getString("entity"));
        JSONObject relationShip=JSON.parseObject(entityObject.getString("relationshipAttributes"));
        JSONArray relationShipAttribute=JSON.parseArray(relationShip.getString("outputFromProcesses"));
        //条数为0,则添加process
        if (relationShipAttribute.size()==0)
        {
            Optional<MetadataMapAtlasPO> po1 = poList.stream().filter(e -> e.dbNameType == 1).findFirst();
            if (po1==null)
            {
                return;
            }
            List<EntityIdAndTypeDTO> list=new ArrayList<>();
            EntityIdAndTypeDTO dto=new EntityIdAndTypeDTO();
            dto.guid=po1.get().atlasGuid;
            dto.typeName=EntityTypeEnum.RDBMS_DB.getName();
            list.add(dto);
            addProcess(EntityTypeEnum.PROCESS,dw,list,po.get().atlasGuid);
        }
    }

    /**
     * 同步dw表与ods血缘
     */
    public void synchronizationPgTableKinShip(){
        try {
            //获取dw库表结构
            ResultEntity<Object> result = client.getDataModelTable();
            if (result.code!= ResultEnum.SUCCESS.getCode())
            {
                return;
            }
            //序列化
            List<SourceTableDTO> list= JSON.parseArray(JSON.toJSONString(result.data),SourceTableDTO.class);
            //获取数据建模MetadataMapAtlas配置表数据
            QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.lambda().eq(MetadataMapAtlasPO::getDataType, DataTypeEnum.DATA_MODEL)
                    .eq(MetadataMapAtlasPO::getColumnId,0);
            List<MetadataMapAtlasPO> poList=metadataMapAtlasMapper.selectList(queryWrapper);
            //判断是否为空
            if (CollectionUtils.isEmpty(list) || CollectionUtils.isEmpty(poList))
            {
                return;
            }
            //过滤sql脚本为空的数据
            list=list.stream().filter(e-> Objects.nonNull(e.sqlScript)).collect(Collectors.toList());
            list=list.stream().filter(e->e.tableName.equals("dim_gf")).collect(Collectors.toList());
            //获取ods表结构
            ResultEntity<List<DataAccessSourceTableDTO>> odsResult = dataAccessClient.getDataAccessMetaData();
            if (result.code!=ResultEnum.SUCCESS.getCode() || CollectionUtils.isEmpty(odsResult.data))
            {
                return;
            }
            for (SourceTableDTO item:list)
            {
                //根据guid,获取配置表信息
                Optional<MetadataMapAtlasPO> data = poList.stream().filter(e -> e.tableId == item.id).findFirst();
                if (data==null)
                {
                    continue;
                }
                String atlasGuid=data.get().atlasGuid;
                //获取实体详情
                ResultDataDTO<String> getDetail = atlasClient.Get(entityByGuid + "/" + atlasGuid);
                if (getDetail.code !=ResultEnum.REQUEST_SUCCESS)
                {
                    return;
                }
                //解析SQL,获取来源表集合
                List<String> tableNameList = analysisSqlHelper.AnalysisSql(item.sqlScript, dbType);
                //获取输入参数集合
                List<EntityIdAndTypeDTO> tableList = getTableList(tableNameList, odsResult.data);
                //解析数据
                JSONObject jsonObj = JSON.parseObject(getDetail.data);
                JSONObject entityObject= JSON.parseObject(jsonObj.getString("entity"));
                JSONObject relationShip=JSON.parseObject(entityObject.getString("relationshipAttributes"));
                JSONArray relationShipAttribute=JSON.parseArray(relationShip.getString("outputFromProcesses"));
                //条数为0,则添加process
                if (relationShipAttribute.size()==0)
                {
                    addProcess(EntityTypeEnum.RDBMS_TABLE,item.sqlScript,tableList,atlasGuid);
                }
                else {
                    for (int i=0;i<relationShipAttribute.size();i++)
                    {
                        updateProcess(
                                relationShipAttribute.getJSONObject(i).getString("guid"),
                                tableList,
                                odsResult.data
                        );
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error("synchronizationPgTableKinShip ex:",e);
        }
    }

    /**
     * 添加process
     * @param sql
     * @param tableList
     * @param atlasGuid
     */
    public void addProcess(EntityTypeEnum entityTypeEnum,String sql,List<EntityIdAndTypeDTO> tableList,String atlasGuid)
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
        attributesDTO.qualifiedName=sql;
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
        ResultDataDTO<String> addResult = atlasClient.Post(entity, jsonParameter);
        if (addResult.code!=ResultEnum.REQUEST_SUCCESS)
        {
            return;
        }
        synchronizationPgData.addMetadataMapAtlas(
                addResult.data,
                EntityTypeEnum.PROCESS,
                sql,
                0,
                0,
                0,
                0,
                0,
                dto.guid);
    }

    /**
     * 更新process
     * @param processGuid
     * @param inputList
     * @param dtoList
     */
    public void updateProcess(String processGuid,List<EntityIdAndTypeDTO> inputList,List<DataAccessSourceTableDTO> dtoList)
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
            ResultDataDTO<String> getDetail = atlasClient.Get(entityByGuid + "/" + processGuid);
            if (getDetail.code !=ResultEnum.REQUEST_SUCCESS)
            {
                return;
            }
            //序列化获取数据
            ProcessDTO dto=JSONObject.parseObject(getDetail.data,ProcessDTO.class);
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
                Optional<DataAccessSourceTableDTO> first = dtoList.stream().filter(e -> e.id == po1.tableId).findFirst();
                if (first==null)
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
                inputDTO.entityStatus="ACTIVE";
                //表名
                inputDTO.displayText=first.get().tableName;
                inputDTO.relationshipType=EntityTypeEnum.DATASET_PROCESS_INPUTS.getName();
                //生成的relationShip
                inputDTO.relationshipGuid=relationShipGuid;
                inputDTO.relationshipStatus="ACTIVE";
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
                dto.entity.relationshipAttributes.inputs=dto.entity.relationshipAttributes.inputs
                        .stream()
                        .filter(e->!ids.contains(e.guid))
                        .collect(Collectors.toList());
            }
            //修改process
            String jsonParameter= JSONArray.toJSON(dto).toString();
            //调用atlas添加实例
            atlasClient.Post(entity, jsonParameter);
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
        ProcessUniqueAttributesDTO attributesDTO2=new ProcessUniqueAttributesDTO();
        attributesDTO2.qualifiedName=end2QualifiedName;
        end2.uniqueAttributes=attributesDTO2;
        dto.end2=end2;

        String jsonParameter= JSONArray.toJSON(dto).toString();
        //调用atlas添加实例
        ResultDataDTO<String> addResult = atlasClient.Post(relationship, jsonParameter);
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
     * @return
     */
    public List<EntityIdAndTypeDTO> getTableList(List<String> tableNameList,List<DataAccessSourceTableDTO> dtoList)
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
        return list;
    }

}
