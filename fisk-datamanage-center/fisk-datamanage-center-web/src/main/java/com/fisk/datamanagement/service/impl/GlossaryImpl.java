package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.category.CategoryDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryAttributeDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryDTO;
import com.fisk.datamanagement.dto.term.TermDTO;
import com.fisk.datamanagement.dto.term.TermDetailsDTO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
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

    @Value("${atlas.glossary.url}")
    private String glossary;
    @Resource
    TermImpl termImpl;
    @Resource
    CategoryImpl categoryImpl;

    @Override
    public List<GlossaryAttributeDTO> getGlossaryList() {
        List<GlossaryAttributeDTO> list;
        try {
            ResultDataDTO<String> result = atlasClient.get(glossary);
            if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
                throw new FkException(ResultEnum.BAD_REQUEST);
            }
            list=JSONObject.parseArray(result.data, GlossaryAttributeDTO.class);
        }
        catch (Exception e)
        {
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
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum deleteGlossary(String guid)
    {
        ResultDataDTO<String> result = atlasClient.delete(glossary +"/"+ guid);
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum updateGlossary(GlossaryDTO dto) {
        String jsonParameter = JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.put(glossary + "/" + dto.guid, jsonParameter);
        return atlasClient.newResultEnum(result);
    }

    @Override
    public List<TermDTO> getTermList(String guid, Boolean parent) {
        List<TermDTO> list = new ArrayList<>();
        //是否为术语库
        if (parent) {
            /*List<GlossaryAttributeDTO> glossaryList = getGlossaryList();
            if (CollectionUtils.isEmpty(glossaryList)) {
                return list;
            }
            Optional<GlossaryAttributeDTO> first = glossaryList.stream().filter(e -> e.guid.equals(guid)).findFirst();
            if (!first.isPresent()) {
                return list;
            }
            for (GlossaryTermAttributeDTO term : first.get().terms) {
                list.add(termImpl.getTerm(term.termGuid));
            }
            return list;*/
            return new ArrayList<>();
        }
        //查询类别关联下的术语
        CategoryDTO category = categoryImpl.getCategory(guid);
        if (category == null || CollectionUtils.isEmpty(category.terms)) {
            return list;
        }
        for (TermDetailsDTO term : category.terms) {
            list.add(termImpl.getTerm(term.termGuid));
        }
        return list;
    }

}