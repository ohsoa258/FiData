package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.classification.ClassificationDefsDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryAttributeDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryTermAttributeDTO;
import com.fisk.datamanagement.dto.search.SearchDslDTO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.service.IGlobalSearch;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class GlobalSearchImpl implements IGlobalSearch {

    @Resource
    AtlasClient atlasClient;
    @Resource
    ClassificationImpl classification;
    @Resource
    GlossaryImpl glossary;

    @Value("${atlas.searchQuick}")
    private String searchQuick;
    @Value("${atlas.searchSuggestions}")
    private String searchSuggestions;
    @Value("${atlas.searchDsl}")
    private String searchDsl;

    @Override
    public JSONObject searchQuick(String query, int limit, int offset) {
        ResultDataDTO<String> result = atlasClient.get(searchQuick + "?query=" + query + "&limit=" + limit + "&offset=" + offset);
        if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
            JSONObject msg = JSON.parseObject(result.data);
            throw new FkException(ResultEnum.BAD_REQUEST, msg.getString("errorMessage"));
        }
        return JSON.parseObject(result.data);
    }

    @Override
    public JSONObject searchSuggestions(String prefixString) {
        ResultDataDTO<String> result = atlasClient.get(searchSuggestions + "?prefixString=" + prefixString);
        if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
            JSONObject msg = JSON.parseObject(result.data);
            throw new FkException(ResultEnum.BAD_REQUEST, msg.getString("errorMessage"));
        }
        return JSON.parseObject(result.data);
    }

    @Override
    public ClassificationDefsDTO searchClassification(String keyword) {
        ClassificationDefsDTO classificationList = classification.getClassificationList();
        //filter过滤
        classificationList.classificationDefs = classificationList.classificationDefs
                .stream()
                .filter(e -> e.name.contains(keyword)).collect(Collectors.toList());
        return classificationList;
    }

    @Override
    public List<GlossaryAttributeDTO> searchTerms(String keyword) {
        //获取术语库列表
        List<GlossaryAttributeDTO> glossaryList = glossary.getGlossaryList();
        List<GlossaryAttributeDTO> list = new ArrayList<>();
        for (GlossaryAttributeDTO item : glossaryList) {
            if (CollectionUtils.isEmpty(item.terms)) {
                continue;
            }
            List<GlossaryTermAttributeDTO> filterTerms = item.terms.stream().filter(e -> e.displayText.contains(keyword)).collect(Collectors.toList());
            //过滤是否存在术语
            if (CollectionUtils.isEmpty(filterTerms)) {
                continue;
            }
            item.terms = filterTerms;
            list.add(item);
        }
        return list;
    }

    @Override
    public JSONObject searchDsl(SearchDslDTO dto) {
        String parameter = "?limit=" + dto.limit + "&offset=" + dto.offset + "&query=" + dto.query;
        ResultDataDTO<String> result = atlasClient.get(searchDsl + parameter);
        if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
            JSONObject msg = JSON.parseObject(result.data);
            throw new FkException(ResultEnum.BAD_REQUEST, msg.getString("errorMessage"));
        }
        return JSON.parseObject(result.data);
    }

}
