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
import com.fisk.datamanagement.entity.MetadataMapAtlasPO;
import com.fisk.datamanagement.enums.DataTypeEnum;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.mapper.MetadataMapAtlasMapper;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.utils.druid.AnalysisSqlHelper;
import com.fisk.datamanagement.vo.ResultDataDTO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${atlas.entity}")
    private String entity;
    @Value("${atlas.entityByGuid}")
    private String entityByGuid;

    private String dbType=JdbcConstants.POSTGRESQL_DRIVER;

    public void synchronizationPgDbKinShip()
    {

    }

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
            list=list.stream().filter(e->"dim_gf".equals(e.tableName) || "dim_df".equals(e.tableName)).collect(Collectors.toList());
            //获取ods表结构
            ResultEntity<List<DataAccessSourceTableDTO>> odsResult = dataAccessClient.getDataAccessMetaData();
            if (result.code!=ResultEnum.SUCCESS.getCode() || CollectionUtils.isEmpty(odsResult.data))
            {
                return;
            }
            for (SourceTableDTO item:list)
            {
                Optional<MetadataMapAtlasPO> data = poList.stream().filter(e -> e.tableId == item.id).findFirst();
                if (data==null)
                {
                    continue;
                }
                String atlasGuid=data.get().atlasGuid;
                ResultDataDTO<String> getDetail = atlasClient.Get(entityByGuid + "/" + atlasGuid);
                if (getDetail.code !=ResultEnum.REQUEST_SUCCESS)
                {
                    return;
                }
                //解析数据
                JSONObject jsonObj = JSON.parseObject(getDetail.data);
                JSONObject entityObject= JSON.parseObject(jsonObj.getString("entity"));
                JSONObject relationShip=JSON.parseObject(entityObject.getString("relationshipAttributes"));
                JSONArray relationShipAttribute=relationShip.getJSONArray(relationShip.getString("outputFromProcesses"));
                //条数为0,则添加process
                if (relationShipAttribute==null)
                {
                    List<String> tableNameList = analysisSqlHelper.AnalysisSql(item.sqlScript, dbType);
                    //组装参数
                    EntityDTO entityDTO=new EntityDTO();
                    EntityTypeDTO entityTypeDTO=new EntityTypeDTO();
                    entityTypeDTO.typeName= EntityTypeEnum.PROCESS.getName();
                    EntityAttributesDTO attributesDTO=new EntityAttributesDTO();
                    attributesDTO.comment="";
                    attributesDTO.description=item.tableName+" add process";
                    attributesDTO.owner="root";
                    attributesDTO.qualifiedName=item.tableName+"_add_process";
                    attributesDTO.contact_info="root";
                    attributesDTO.name=item.tableName+"_add_process";
                    //输入参数
                    attributesDTO.inputs=getTableList(tableNameList,odsResult.data);
                    //输出参数
                    List<EntityIdAndTypeDTO> dtoList=new ArrayList<>();
                    EntityIdAndTypeDTO dto=new EntityIdAndTypeDTO();
                    dto.typeName=EntityTypeEnum.RDBMS_TABLE.getName();
                    dto.guid=atlasGuid;
                    dtoList.add(dto);
                    attributesDTO.outputs=dtoList;
                    entityTypeDTO.attributes=attributesDTO;
                    entityDTO.entity=entityTypeDTO;
                    String jsonParameter= JSONArray.toJSON(entityDTO).toString();
                    //调用atlas添加血缘
                    ResultDataDTO<String> addResult = atlasClient.Post(entity, jsonParameter);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

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
