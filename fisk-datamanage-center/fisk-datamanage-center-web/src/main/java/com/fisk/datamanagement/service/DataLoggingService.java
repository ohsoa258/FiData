package com.fisk.datamanagement.service;

import com.fisk.datamanagement.dto.datalogging.DataLoggingDTO;
import com.fisk.datamanagement.dto.datalogging.DataTotalDTO;
import com.fisk.datamanagement.dto.datalogging.PipelTotalDTO;
import com.fisk.datamanagement.dto.datalogging.PipelWeekDTO;

import java.util.List;

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

    /**
     * 获取管道当天运行成功失败次数
     * @return
     */
    PipelTotalDTO getPipelTotals();

    /**
     * 获取管道过去7天每天的运行次数
     * @return
     */
    List<PipelWeekDTO> getPipelWeek();
}
