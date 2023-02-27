package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.dataservice.dto.dataanalysisview.*;
import com.fisk.dataservice.entity.DataViewPO;
import com.fisk.dataservice.entity.ViewFieldsPO;
import com.fisk.system.dto.datasource.DataSourceDTO;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
public interface IDataViewFieldsService extends IService<ViewFieldsPO> {

    void saveViewFields(SaveDataViewDTO dto, Integer dataViewId, DataSourceDTO dsDto);

    void updateViewFields(DataViewPO dto, Integer viewThemeId, DataSourceDTO dataSourceDTO);

    void saveBatchViewFields(DataViewPO dto, Integer dataViewId, DataSourceDTO dataSourceDTO);
}
