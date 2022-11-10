package com.fisk.dataaccess.service;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataReqDTO;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;

import java.util.List;

/**
 * @author Lock
 * @version 2.0
 * @description
 * @date 2022/1/6 15:16
 */
public interface IDataAccess {

    /**
     * 获取数据接入已发布的元数据对象
     *
     * @return 元数据对象
     */
    ResultEntity<List<DataAccessSourceTableDTO>> getDataAccessMetaData();

    /**
     * 构建元数据查询对象(表及下面的字段)
     *
     * @param dto dto
     * @return 元数据对象
     */
    List<FiDataTableMetaDataDTO> buildFiDataTableMetaData(FiDataTableMetaDataReqDTO dto);
}
