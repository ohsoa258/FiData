package com.fisk.dataservice.service;

import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.dataservice.dto.datasource.DataSourceColumnQueryDTO;
import com.fisk.dataservice.dto.datasource.DataSourceInfoDTO;
import com.fisk.dataservice.dto.datasource.DataSourceQueryDTO;
import com.fisk.dataservice.dto.datasource.DataSourceQueryResultDTO;

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

    /**
     * 表服务执行sql,获取数据
     *
     * @param dto
     * @return
     */
    DataSourceQueryResultDTO getTableServiceQueryList(DataSourceQueryDTO dto);

    /**
     * 根据数据库id获取所有表
     *
     * @param dataSourceId
     * @return
     */
    List<TableNameDTO> getAllTableByDb(Integer dataSourceId);

}
