package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.classification.ClassificationAddEntityDTO;
import com.fisk.datamanagement.dto.classification.ClassificationDefContentDTO;
import com.fisk.datamanagement.dto.classification.ClassificationDefsDTO;
import com.fisk.datamanagement.dto.classification.ClassificationDelAssociatedEntityDTO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.service.IClassification;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class ClassificationImpl implements IClassification {

    @Resource
    AtlasClient atlasClient;

    @Value("${atlas.typedefs}")
    private String typedefs;
    @Value("${atlas.delTypeDefs}")
    private String delTypeDefs;
    @Value("${atlas.bulkClassification}")
    private String bulkClassification;
    @Value("${atlas.entityByGuid}")
    private String entityByGuid;
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    EntityImpl entity;

    @Override
    public ClassificationDefsDTO getClassificationList()
    {
        ClassificationDefsDTO data=new ClassificationDefsDTO();
        try {
            ResultDataDTO<String> result = atlasClient.get(typedefs + "?type=classification");
            if (result.code != AtlasResultEnum.REQUEST_SUCCESS)
            {
                throw new FkException(ResultEnum.BAD_REQUEST);
            }
            JSONObject jsonObj = JSON.parseObject(result.data);
            String classificationDefs=jsonObj.getString("classificationDefs");
            data.classificationDefs = JSONObject.parseArray(classificationDefs, ClassificationDefContentDTO.class);
            //根据创建时间升序
            data.classificationDefs.sort(Comparator.comparing(ClassificationDefContentDTO::getCreateTime));
            //反转
            Collections.reverse(data.classificationDefs);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error("getClassificationList ex:"+e);
            throw new FkException(ResultEnum.SQL_ANALYSIS);
        }
        return data;
    }

    @Override
    public ResultEnum updateClassification(ClassificationDefsDTO dto)
    {
        String jsonParameter=JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.put(typedefs + "?type=classification", jsonParameter);
        return result.code==AtlasResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:ResultEnum.BAD_REQUEST;
    }

    @Override
    public ResultEnum deleteClassification(String classificationName)
    {
        ResultDataDTO<String> result = atlasClient.delete(delTypeDefs + classificationName);
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum addClassification(ClassificationDefsDTO dto)
    {
        //设置时间戳
        dto.classificationDefs
                .stream()
                .map(e->e.createTime=System.currentTimeMillis())
                .collect(Collectors.toList());
        String jsonParameter=JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.post(typedefs + "?type=classification", jsonParameter);
        return result.code==AtlasResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:ResultEnum.BAD_REQUEST;
    }

    @Override
    public ResultEnum classificationAddAssociatedEntity(ClassificationAddEntityDTO dto)
    {
        String jsonParameter=JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.post(bulkClassification, jsonParameter);
        Boolean exist = redisTemplate.hasKey("metaDataEntityData:"+dto.entityGuids.get(0));
        if (exist)
        {
            entity.setRedis(dto.entityGuids.get(0));
        }
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum classificationDelAssociatedEntity(ClassificationDelAssociatedEntityDTO dto)
    {
        ResultDataDTO<String> result = atlasClient.delete(entityByGuid + "/" + dto.entityGuid+"/classification/"+dto.classificationName);
        return atlasClient.newResultEnum(result);
    }

}
