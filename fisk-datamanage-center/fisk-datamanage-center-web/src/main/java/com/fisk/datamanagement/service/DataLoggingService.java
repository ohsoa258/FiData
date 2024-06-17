package com.fisk.datamanagement.service;

import com.fisk.datamanagement.dto.datalogging.DataLoggingDTO;
import com.fisk.datamanagement.dto.datalogging.DataTotalDTO;

public interface DataLoggingService {
    /**
     * 获取接入数据总记录数
     * @return
     */
    DataLoggingDTO getDataTableRows();

    /**
     * 获取所有数据记录数
     * @return
     */
    DataTotalDTO getDataTotals();
}
