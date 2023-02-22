package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.dataanalysisview.DataViewThemeDTO;
import com.fisk.dataservice.entity.DataViewThemePO;
import com.fisk.dataservice.mapper.DataViewThemeMapper;
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
     * 新增视图接口
     * @param dto
     * @return
     */
    ResultEnum addViewTheme(DataViewThemeDTO dto);

    /**
     * 获取目标数据源信息列表
     * @return
     */
    List<DataSourceDTO> getTargetDbList();
}
