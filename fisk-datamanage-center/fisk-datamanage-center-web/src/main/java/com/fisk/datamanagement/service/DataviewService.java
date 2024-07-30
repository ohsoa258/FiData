package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.dataview.DataViewAddDTO;
import com.fisk.datamanagement.dto.dataview.DataViewDTO;
import com.fisk.datamanagement.dto.dataview.DataViewEditDTO;
import com.fisk.datamanagement.enums.serverModuleTypeEnum;

/**
 * @Author: wangjian
 * @Date: 2024-06-18
 * @Description:
 */
public interface DataviewService {
    /**
     * 查询获取个人视图和系统视图
     *
     * @param currentPage
     * @param pageSize
     * @param type
     * @param filterName
     * @return
     */
    Page<DataViewDTO> queryAll(Integer currentPage, Integer pageSize, serverModuleTypeEnum type,String filterName);

    /**
     * 添加数据视图
     * @param dto
     * @return
     */
    ResultEnum saveView(DataViewAddDTO dto);

    /**
     * 更新数据视图
     * @param dto
     * @return
     */
    ResultEnum updateView(DataViewEditDTO dto);

    /**
     * 删除视图
     * @param dto
     * @return
     */
    ResultEnum deleteView(Integer dto);
}
