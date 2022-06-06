package com.fisk.datamanagement.service;

import com.fisk.datamanagement.dto.datamasking.DataMaskingSourceDTO;
import com.fisk.datamanagement.dto.datamasking.DataMaskingTargetDTO;
import com.fisk.datamanagement.dto.datamasking.SourceTableDataDTO;

/**
 * @author JianWenYang
 */
public interface IDataMasking {

    /**
     * 数据脱敏获取实例配置信息
     * @param dto
     * @return
     */
    DataMaskingTargetDTO getSourceDataConfig(DataMaskingSourceDTO dto);

    /**
     * 获取数仓表信息数据
     * @param dto
     * @return
     */
    SourceTableDataDTO getTableData(SourceTableDataDTO dto);

}
