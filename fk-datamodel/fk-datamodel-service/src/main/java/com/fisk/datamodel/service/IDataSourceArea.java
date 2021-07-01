package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.BusinessAreaDTO;
import com.fisk.datamodel.dto.DataSourceAreaDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.DataSourceAreaPO;

import java.util.List;
import java.util.Map;

/**
 * @author: Lock
 */
public interface IDataSourceArea extends IService<DataSourceAreaPO> {

    ResultEnum addData(DataSourceAreaDTO dto);

    DataSourceAreaDTO getData(long id);

    ResultEnum updateDataSourceArea(DataSourceAreaDTO dto);

    ResultEnum deleteDataSourceArea(long id);

    List<DataSourceAreaDTO> listDataSource();
}
