package com.fisk.dataservice.service;

import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.dataservice.dto.datasource.DataSourceColumnQueryDTO;
import com.fisk.dataservice.dto.datasource.DataSourceInfoDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IDataSourceConfig {

    /**
     * 获取ods dw表集合
     *
     * @return
     */
    List<DataSourceInfoDTO> getTableInfoList();

    /**
     * 获取表字段集合
     *
     * @param dto
     * @return
     */
    List<TableColumnDTO> getColumn(DataSourceColumnQueryDTO dto);

}
