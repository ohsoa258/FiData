package com.fisk.datamodel.service;

import com.fisk.datamodel.dto.dataops.DataModelTableInfoDTO;

import java.util.List;

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

    /**
     * 根据表名字段显示名称
     *
     * @param tableName
     * @return
     */
    List<String[]> getTableColumnDisplay(String tableName);

}
