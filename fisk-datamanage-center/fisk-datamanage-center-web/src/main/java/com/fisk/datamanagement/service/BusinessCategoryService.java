package com.fisk.datamanagement.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.businessclassification.BusinessCategoryTreeDTO;
import com.fisk.datamanagement.dto.classification.BusinessCategoryDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface BusinessCategoryService {


    /**
     * 更改指标主题数据
     *
     * @param dto
     * @return
     */
    ResultEnum updateCategory(BusinessCategoryDTO dto);

    /**
     * 更改指标主题数据顺序
     *
     * @param dto
     * @return
     */
    ResultEnum updateCategorySort(List<String> dto);


    /**
     * 删除指标主题数据
     * @param categoryId
     * @return
     */
    ResultEnum deleteCategory(String categoryId);


    /**
     * 添加指标主题数据
     * @param dto
     * @return
     */
    ResultEnum addCategory(BusinessCategoryDTO dto);

    /**
     * 获取树状指标主题数据
     *
     * @return
     */
    List<BusinessCategoryTreeDTO> getCategoryTree();



}
