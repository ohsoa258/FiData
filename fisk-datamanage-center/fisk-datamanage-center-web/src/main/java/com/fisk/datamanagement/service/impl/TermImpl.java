package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.term.TermAssignedEntities;
import com.fisk.datamanagement.dto.term.TermDTO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.service.ITerm;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Service
public class TermImpl implements ITerm {

    @Value("${atlas.glossary.term}")
    public String term;
    @Value("${atlas.glossary.terms}")
    public String terms;
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    EntityImpl entity;
    @Resource
    AtlasClient atlasClient;

    @Override
    public ResultEnum addTerm(TermDTO dto)
    {
        String jsonParameter= JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.post(term,jsonParameter);
        return result.code== AtlasResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:ResultEnum.BAD_REQUEST;
    }

    @Override
    public TermDTO getTerm(String guid)
    {
        TermDTO dto;
        ResultDataDTO<String> result = atlasClient.get(term + "/" + guid);
        if (result.code != AtlasResultEnum.REQUEST_SUCCESS)
        {
            throw new FkException(ResultEnum.BAD_REQUEST);
        }
        dto= JSONObject.parseObject(result.data, TermDTO.class);
        return dto;
    }

    @Override
    public ResultEnum updateTerm(TermDTO dto)
    {
        String jsonParameter= JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.put(term + "/" + dto.guid,jsonParameter);
        return result.code== AtlasResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:ResultEnum.BAD_REQUEST;
    }

    @Override
    public ResultEnum deleteTerm(String guid)
    {
        ResultDataDTO<String> result = atlasClient.delete(term +"/"+ guid);
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum termAssignedEntities(TermAssignedEntities dto)
    {
        String jsonParameter= JSONArray.toJSON(dto.dto).toString();
        ResultDataDTO<String> result = atlasClient.post(terms + "/" + dto.termGuid+"/assignedEntities",jsonParameter);
        Boolean exist = redisTemplate.hasKey("metaDataEntityData:"+dto.dto.get(0).guid);
        if (exist)
        {
            entity.setRedis(dto.dto.get(0).guid);
        }
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum termDeleteAssignedEntities(TermAssignedEntities dto)
    {
        String jsonParameter= JSONArray.toJSON(dto.dto).toString();
        ResultDataDTO<String> result = atlasClient.put(terms + "/" + dto.termGuid+"/assignedEntities",jsonParameter);
        if (result.code == AtlasResultEnum.BAD_REQUEST)
        {
            JSONObject msg= JSON.parseObject(result.data);
            throw new FkException(ResultEnum.BAD_REQUEST,msg.getString("errorMessage"));
        }
        return atlasClient.newResultEnum(result);
    }

}
