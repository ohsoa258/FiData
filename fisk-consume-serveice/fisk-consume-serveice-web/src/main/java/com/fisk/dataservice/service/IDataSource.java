package com.fisk.dataservice.service;

import com.fisk.dataservice.dto.datasource.DataSourceInfoDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IDataSource {

    /**
     * 获取ods dw表集合
     *
     * @return
     */
    List<DataSourceInfoDTO> getTableInfoList();

}
