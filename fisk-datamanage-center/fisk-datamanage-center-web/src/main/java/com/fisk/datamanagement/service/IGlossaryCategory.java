package com.fisk.datamanagement.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.category.CategoryDTO;
import com.fisk.datamanagement.dto.glossary.FirstGlossaryCategorySummaryDto;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IGlossaryCategory {

    /**
     * 添加类别
     * @param dto
     * @return
     */
    ResultEnum addCategory(CategoryDTO dto);

    /**
     * 删除类别
     * @param guid
     * @return
     */
    ResultEnum deleteCategory(String guid);

    /**
     * 获取类别详情
     * @param guid
     * @return
     */
    CategoryDTO getCategory(String guid);

    /**
     * 更改类别详情
     * @param dto
     * @return
     */
    ResultEnum updateCategory(CategoryDTO dto);

    /**
     * 获取术语库下的汇总数据
     * @return
     */
    List<FirstGlossaryCategorySummaryDto> getGlossaryCategorySummary();

}
