package com.fisk.datamanagement.service;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.businessclassification.BusinessCategorySortDTO;
import com.fisk.datamanagement.dto.businessclassification.BusinessCategoryTreeDTO;
import com.fisk.datamanagement.dto.businessclassification.BusinessMetaDataTreeDTO;
import com.fisk.datamanagement.dto.businessclassification.ParentBusinessTreeDTO;
import com.fisk.datamanagement.dto.category.BusinessCategoryAssignmentDTO;
import com.fisk.datamanagement.dto.classification.BusinessCategoryDTO;
import com.fisk.datamanagement.entity.BusinessCategoryPO;
import java.util.List;

public interface BusinessCategoryService extends IService<BusinessCategoryPO> {


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
     *
     * @param categoryId
     * @return
     */
    ResultEnum deleteCategory(String categoryId);


    /**
     * 添加指标主题数据
     *
     * @param dto
     * @return
     */
    ResultEnum addCategory(BusinessCategoryDTO dto);

    List<Integer> getBusinessCategoryAssignment(String pid);

    /**
     * 添加指标主题数据
     *
     * @param dto
     * @return
     */
    ResultEnum addBusinessCategoryAssignment(BusinessCategoryAssignmentDTO dto);

    /**
     * 获取树状指标主题数据
     *
     * @return
     */
    List<BusinessCategoryTreeDTO> getCategoryTree();

    /**
     * 获取纬度数据
     *
     * @return
     */
    JSONArray getDimensionTreeList();


    JSONArray getFactTreeList();

    JSONArray getApiTreeList();

    List<ParentBusinessTreeDTO> getParentBusinessDataList();

    List<BusinessMetaDataTreeDTO> getAllBusinessMetaDataList();

    ResultEnum businessCategorySort(BusinessCategorySortDTO dto);
}
