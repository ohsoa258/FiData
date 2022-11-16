package com.fisk.datamodel.service;

import com.fisk.datamodel.dto.dataops.DataModelTableInfoDTO;

/**
 * @author JianWenYang
 */
public interface IDataOps {

    /**
     * 根据表名获取表信息
     *
     * @param tableName
     * @return
     */
    DataModelTableInfoDTO getTableInfo(String tableName);

}
