package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.dataanalysisview.DataViewDTO;
import com.fisk.dataservice.dto.dataanalysisview.DataViewThemeDTO;
import com.fisk.dataservice.entity.DataViewPO;
import com.fisk.dataservice.entity.DataViewThemePO;
import com.fisk.dataservice.mapper.DataViewThemeMapper;
import com.fisk.dataservice.vo.dataanalysisview.DataSourceVO;
import com.fisk.dataservice.vo.dataanalysisview.DataViewThemeVO;
import com.fisk.system.dto.datasource.DataSourceDTO;

import java.util.List;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
public interface IDataViewThemeService extends IService<DataViewThemePO> {

    /**
     * 新增视图主题接口
     * @param dto
     * @return
     */
    ResultEnum addViewTheme(DataViewThemeDTO dto);

    /**
     * 获取目标数据源信息列表
     * @return
     */
    List<DataSourceVO> getTargetDbList();

    /**
     * 删除数据视图主题
     * @param viewThemeId
     * @return
     */
    ResultEnum removeViewTheme(Integer viewThemeId);

    /**
     * 修改视图主题
     * @param dto
     * @return
     */
    ResultEnum updateViewTheme(DataViewThemeDTO dto);

    /**
     * 分页查询数据视图主题列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageDTO<DataViewThemeDTO> getViewThemeList(Integer pageNum, Integer pageSize);

    /**
     * 获取数据视图主题所在的目标数据源
     * @param viewThemeId
     * @return
     */
    DataSourceVO getDataSourceByViewThemeId(Integer viewThemeId);
}
