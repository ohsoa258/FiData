package com.fisk.datamanagement.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.labelcategory.FirstLabelCategorySummaryDto;
import com.fisk.datamanagement.dto.labelcategory.LabelCategoryDTO;
import com.fisk.datamanagement.dto.labelcategory.LabelCategoryDataDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface ILabelCategory {

    /**
     * 添加标签类目
     * @param dto
     * @return
     */
    ResultEnum addLabelCategory(LabelCategoryDTO dto);

    /**
     * 删除标签类目
     * @param id
     * @return
     */
    ResultEnum delLabelCategory(int id);

    /**
     * 更改标签类目
     * @param dto
     * @return
     */
    ResultEnum updateLabelCategory(LabelCategoryDTO dto);

    /**
     * 获取标签类目详情
     * @param id
     * @return
     */
    LabelCategoryDTO getCategoryDetail(int id);

    /**
     * 获取类目列表
     * @param queryName
     * @return
     */
    List<LabelCategoryDataDTO> getLabelCategoryList(String queryName);


    /**
     * 获取第一级属性分类下的汇总数据
     * @return
     */
    List<FirstLabelCategorySummaryDto> getFirstLabelCategorySummary();

}
