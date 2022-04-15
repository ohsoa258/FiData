package com.fisk.datamanagement.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.dataquality.DataQualityDTO;
import com.fisk.datamanagement.dto.dataquality.UpperLowerBloodParameterDTO;
import com.fisk.datamanagement.dto.entity.EntityTreeDTO;
import com.fisk.datamanagement.dto.lineage.LineAgeDTO;
import com.fisk.datamanagement.dto.lineage.LineAgeRelationsDTO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.enums.UpperLowerBloodEnum;
import com.fisk.datamanagement.service.IDataQuality;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class DataQualityImpl implements IDataQuality {

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    AtlasClient atlasClient;
    @Resource
    EntityImpl entityImpl;

    @Value("${spring.metadataentity}")
    private String metaDataEntity;
    @Value("${atlas.lineage}")
    private String lineage;
    @Value("${atlas.entityByGuid}")
    private String entityByGuid;

    @Override
    public boolean existAtlas(DataQualityDTO dto)
    {
        List<EntityTreeDTO> list=getTreeList();
        if (CollectionUtils.isEmpty(list))
        {
            return false;
        }
        //判断实例
        Optional<EntityTreeDTO> instanceData = list.stream().filter(e -> e.label.equals(dto.instanceName)).findFirst();
        if (!instanceData.isPresent())
        {
            return false;
        }
        //判断库名
        Optional<EntityTreeDTO> dbData = instanceData.get().children.stream().filter(e -> e.label.equals(dto.dbName)).findFirst();
        if (!dbData.isPresent())
        {
            return false;
        }
        JSONObject entityDetails = entityImpl.getEntity(instanceData.get().id);
        //获取entity
        JSONObject entity = JSON.parseObject(entityDetails.getString("entity"));
        //获取attributes
        JSONObject attributes=JSON.parseObject(entity.getString("attributes"));
        if (dto.port.equals(attributes.getString("port"))
                && dto.rdbmsType.toLowerCase().equals(attributes.getString("rdbms_type").toLowerCase()))
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean existUpperLowerBlood(UpperLowerBloodParameterDTO dto)
    {
        List<EntityTreeDTO> list= getTreeList();
        if (CollectionUtils.isEmpty(list))
        {
            return false;
        }
        //判断实例
        Optional<EntityTreeDTO> instanceData = list.stream().filter(e -> e.label.equals(dto.instanceName)).findFirst();
        if (!instanceData.isPresent())
        {
            return false;
        }
        //判断库名
        Optional<EntityTreeDTO> dbData = instanceData.get().children.stream().filter(e -> e.label.equals(dto.dbName)).findFirst();
        if (!dbData.isPresent())
        {
            return false;
        }
        //判断表名
        Optional<EntityTreeDTO> tableData = dbData.get().children.stream().filter(e -> e.label.equals(dto.tableName)).findFirst();
        if (!tableData.isPresent())
        {
            return false;
        }
        String tableGuid=tableData.get().id;
        //获取血缘数据
        ResultDataDTO<String> result = atlasClient.get(lineage + "/" + tableGuid);
        if (result.code != AtlasResultEnum.REQUEST_SUCCESS)
        {
            return false;
        }
        List<JSONObject> jsonArrayList=new ArrayList<>();
        //解析数据
        JSONObject jsonObj = JSON.parseObject(result.data);
        //判断是否存在血缘关系
        JSONArray dataArray=jsonObj.getJSONArray("relations");
        if (dataArray.size()==0)
        {
            return false;
        }
        //获取血缘关联实体列表
        JSONObject guidEntityMapJson = JSON.parseObject(jsonObj.getString("guidEntityMap"));
        String entityDetail = guidEntityMapJson.getString(tableGuid);
        JSONObject entityDetailJson = JSON.parseObject(entityDetail);
        jsonArrayList.add(entityDetailJson);
        String typeName=entityDetailJson.getString("typeName");
        //关系集合
        List<LineAgeRelationsDTO> relationsDtoList;
        relationsDtoList=JSONObject.parseArray(jsonObj.getString("relations"), LineAgeRelationsDTO.class);
        if (!CollectionUtils.isNotEmpty(relationsDtoList))
        {
            return false;
        }
        //获取上游血缘
        LineAgeDTO inPutData = entityImpl.getInPutData(tableGuid, relationsDtoList, guidEntityMapJson, typeName);
        LineAgeDTO outPutData = entityImpl.getOutPutData(tableGuid, relationsDtoList, guidEntityMapJson, typeName);
        if (UpperLowerBloodEnum.UPPER.getValue()==dto.checkConsanguinity && !CollectionUtils.isEmpty(inPutData.relations))
        {
            return true;
        }
        else if (UpperLowerBloodEnum.LOWER.getValue()==dto.checkConsanguinity && !CollectionUtils.isEmpty(outPutData.relations))
        {
            return true;
        }
        else if (UpperLowerBloodEnum.UPPER_LOWER.getValue()==dto.checkConsanguinity
                && !CollectionUtils.isEmpty(inPutData.relations)
                && !CollectionUtils.isEmpty(outPutData.relations)){
            return true;
        }
        return false;
    }

    public List<EntityTreeDTO> getTreeList(){
        List<EntityTreeDTO> list=new ArrayList<>();
        Boolean exist = redisTemplate.hasKey(metaDataEntity);
        if (!exist) {
            return list;
        }
        String treeList = redisTemplate.opsForValue().get(metaDataEntity).toString();
        return JSONObject.parseArray(treeList, EntityTreeDTO.class);
    }

}
