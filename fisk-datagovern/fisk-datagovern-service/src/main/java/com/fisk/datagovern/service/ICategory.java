package com.fisk.datagovern.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datagovern.dto.category.CategoryDTO;

/**
 * @author JianWenYang
 */
public interface ICategory {

    /**
     * 添加标签类目
     * @param dto
     * @return
     */
    ResultEnum addCategory(CategoryDTO dto);

    /**
     * 删除标签类目
     * @param id
     * @return
     */
    ResultEnum delCategory(int id);

    /**
     * 更改标签类目
     * @param dto
     * @return
     */
    ResultEnum updateCategory(CategoryDTO dto);

    /**
     * 获取标签类目详情
     * @param id
     * @return
     */
    CategoryDTO getCategoryDetail(int id);

}
