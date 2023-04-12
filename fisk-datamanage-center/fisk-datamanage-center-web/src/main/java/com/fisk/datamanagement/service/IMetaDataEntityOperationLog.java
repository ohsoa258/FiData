package com.fisk.datamanagement.service;

import com.fisk.datamanagement.dto.metadataentityoperationLog.MetaDataEntityOperationLogDTO;


import java.util.List;

public interface IMetaDataEntityOperationLog {
    /**
     * 保存日志
     * @param logDTO
     * @return
     */
    void addOperationLog(MetaDataEntityOperationLogDTO logDTO);

    /**
     * 根据元数据id查询对应的日志
     * @return
     */
    List<MetaDataEntityOperationLogDTO> selectLogList(Integer entityId,Integer typeId);
}
