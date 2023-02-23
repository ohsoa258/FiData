package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.dataservice.dto.dataanalysisview.DataViewDTO;
import com.fisk.dataservice.dto.dataanalysisview.DataSourceViewDTO;
import com.fisk.dataservice.entity.DataViewPO;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
public interface IDataViewService extends IService<DataViewPO> {

    /**
     * 分页获取数据
     * @param viewThemeId
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageDTO<DataViewDTO> getViewList(Integer viewThemeId, Integer pageNum, Integer pageSize);

    /**
     *
     * @param viewThemeId
     * @return
     */
    DataSourceViewDTO getDataSourceMeta(Integer viewThemeId);
}
