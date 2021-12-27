package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.dto.glossary.GlossaryAttributeDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryDTO;
import com.fisk.datamanagement.service.IGlossary;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class GlossaryImpl implements IGlossary {

    @Resource
    AtlasClient atlasClient;

    @Value("${atlas.glossary}")
    private String glossary;

    @Override
    public List<GlossaryAttributeDTO> getGlossaryList()
    {
        List<GlossaryAttributeDTO> list=new ArrayList<>();
        try {
            ResultDataDTO<String> result = atlasClient.Get(glossary);
            if (result.code != ResultEnum.REQUEST_SUCCESS)
            {
                throw new FkException(result.code);
            }
            list=JSONObject.parseArray(result.data, GlossaryAttributeDTO.class);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error("getGlossaryList ex:"+e);
            throw new FkException(ResultEnum.SQL_ANALYSIS);
        }
        return list;
    }

    @Override
    public ResultEnum addGlossary(GlossaryDTO dto)
    {
        String jsonParameter= JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.Post(glossary,jsonParameter);
        return result.code==ResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:result.code;
    }

    @Override
    public ResultEnum deleteGlossary(String guid)
    {
        ResultDataDTO<String> result = atlasClient.Delete(glossary +"/"+ guid);
        return result.code==ResultEnum.NO_CONTENT?ResultEnum.SUCCESS:result.code;
    }

    @Override
    public ResultEnum updateGlossary(GlossaryDTO dto)
    {
        String jsonParameter= JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.Put(glossary+"/"+dto.guid,jsonParameter);
        return result.code==ResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:result.code;
    }

}