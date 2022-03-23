package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.dto.entity.EntityGuidDTO;
import com.fisk.datamanagement.dto.term.TermAssignedEntities;
import com.fisk.datamanagement.dto.term.TermDTO;
import com.fisk.datamanagement.service.ITerm;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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
    AtlasClient atlasClient;

    @Override
    public ResultEnum addTerm(TermDTO dto)
    {
        String jsonParameter= JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.Post(term,jsonParameter);
        return result.code==ResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:result.code;
    }

    @Override
    public TermDTO getTerm(String guid)
    {
        TermDTO dto=new TermDTO();
        ResultDataDTO<String> result = atlasClient.Get(term + "/" + guid);
        if (result.code != ResultEnum.REQUEST_SUCCESS)
        {
            throw new FkException(result.code);
        }
        dto= JSONObject.parseObject(result.data, TermDTO.class);
        return dto;
    }

    @Override
    public ResultEnum updateTerm(TermDTO dto)
    {
        String jsonParameter= JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.Put(term + "/" + dto.guid,jsonParameter);
        return result.code==ResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:result.code;
    }

    @Override
    public ResultEnum deleteTerm(String guid)
    {
        ResultDataDTO<String> result = atlasClient.Delete(term +"/"+ guid);
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum termAssignedEntities(TermAssignedEntities dto)
    {
        String jsonParameter= JSONArray.toJSON(dto.dto).toString();
        ResultDataDTO<String> result = atlasClient.Post(terms + "/" + dto.termGuid+"/assignedEntities",jsonParameter);
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum termDeleteAssignedEntities(TermAssignedEntities dto)
    {
        String jsonParameter= JSONArray.toJSON(dto.dto).toString();
        ResultDataDTO<String> result = atlasClient.Put(terms + "/" + dto.termGuid+"/assignedEntities",jsonParameter);
        if (result.code == ResultEnum.BAD_REQUEST)
        {
            JSONObject msg= JSON.parseObject(result.data);
            throw new FkException(result.code,msg.getString("errorMessage"));
        }
        return atlasClient.newResultEnum(result);
    }

}
