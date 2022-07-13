package com.fisk.datamanagement.service;

import com.alibaba.fastjson.JSONObject;
import com.fisk.datamanagement.dto.classification.ClassificationDefsDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryAttributeDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IGlobalSearch {

    /**
     * 全局搜索--查询Entity
     *
     * @param query
     * @param limit
     * @param offset
     * @return
     */
    JSONObject searchQuick(String query, int limit, int offset);

    /**
     * 全局搜索--查询Suggestions
     *
     * @param prefixString
     * @return
     */
    JSONObject searchSuggestions(String prefixString);

    /**
     * 全局搜索--查询业务分类
     *
     * @param keyword
     * @return
     */
    ClassificationDefsDTO searchClassification(String keyword);

    /**
     * 全局搜索--查询术语
     *
     * @param keyword
     * @return
     */
    List<GlossaryAttributeDTO> searchTerms(String keyword);

}
