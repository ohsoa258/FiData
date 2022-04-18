package com.fisk.datamanagement.service;

import com.fisk.datamanagement.dto.datamasking.DataMaskingSourceDTO;
import com.fisk.datamanagement.dto.datamasking.DataMaskingTargetDTO;

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

}
