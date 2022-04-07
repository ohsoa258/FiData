package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.glossary.GlossaryAttributeDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryDTO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.service.IGlossary;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class GlossaryImpl implements IGlossary {

    @Resource
    AtlasClient atlasClient;

    @Value("${atlas.glossary.url}")
    private String glossary;

    @Override
    public List<GlossaryAttributeDTO> getGlossaryList()
    {
        List<GlossaryAttributeDTO> list;
        try {
            ResultDataDTO<String> result = atlasClient.get(glossary);
            if (result.code != AtlasResultEnum.REQUEST_SUCCESS)
            {
                throw new FkException(ResultEnum.BAD_REQUEST);
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
        ResultDataDTO<String> result = atlasClient.post(glossary,jsonParameter);
        return result.code== AtlasResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:ResultEnum.BAD_REQUEST;
    }

    @Override
    public ResultEnum deleteGlossary(String guid)
    {
        ResultDataDTO<String> result = atlasClient.delete(glossary +"/"+ guid);
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum updateGlossary(GlossaryDTO dto)
    {
        String jsonParameter= JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.put(glossary+"/"+dto.guid,jsonParameter);
        return result.code== AtlasResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:ResultEnum.BAD_REQUEST;
    }

}