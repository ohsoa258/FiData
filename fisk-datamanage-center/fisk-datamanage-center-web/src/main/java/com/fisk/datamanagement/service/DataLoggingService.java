package com.fisk.datamanagement.service;

import com.fisk.datamanagement.dto.datalogging.DataLoggingDTO;

public interface DataLoggingService {
    /**
     * 获取接入数据总记录数
     * @return
     */
    DataLoggingDTO getDataTableRows();
}
