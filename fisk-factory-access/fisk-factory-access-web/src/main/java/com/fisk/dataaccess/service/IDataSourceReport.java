package com.fisk.dataaccess.service;

import com.fisk.dataaccess.dto.statementofassets.DataSourceReportDTO;

import java.util.List;

public interface IDataSourceReport {
    /**
     * 获取数据来源信息
     * @return
     */
    List<DataSourceReportDTO> getDataSourceReportDTO();
}
