package com.fisk.datamanagement.service;

import com.alibaba.fastjson.JSONObject;
import com.fisk.datamanagement.dto.classification.ClassificationDefsDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryTermAttributeDTO;
import com.fisk.datamanagement.dto.label.GlobalSearchDto;
import com.fisk.datamanagement.dto.search.EntitiesDTO;
import com.fisk.datamanagement.dto.search.SearchDslDTO;

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
   /* JSONObject searchQuick(String query, int limit, int offset);*/


    /**
     * 全局搜索--查询Entity
     * @param query
     * @param limit
     * @param offset
     * @return
     */
    List<EntitiesDTO>  searchQuick(String query,int limit, int offset);

    /**
     * 全局搜索--查询Suggestions
     *
     * @param prefixString
     * @return
     */
    List<GlobalSearchDto> searchSuggestions(String prefixString);



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
    List<GlobalSearchDto> searchTerms(String keyword);

    /**
     * 根据Type查询数据
     *
     * @param dto
     * @return
     */
    JSONObject searchDsl(SearchDslDTO dto);






}
