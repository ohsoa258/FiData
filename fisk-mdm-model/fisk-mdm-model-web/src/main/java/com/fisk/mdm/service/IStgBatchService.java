package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.stgbatch.StgBatchDTO;

/**
 * @author JianWenYang
 */
public interface IStgBatchService {

    /**
     * 添加stg批次
     * @param dto
     * @return
     */
    ResultEnum addStgBatch(StgBatchDTO dto);

}
