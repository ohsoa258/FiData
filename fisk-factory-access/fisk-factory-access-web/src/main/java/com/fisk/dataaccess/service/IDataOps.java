package com.fisk.dataaccess.service;

import com.fisk.dataaccess.dto.dataops.TableInfoDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IDataOps {

    /**
     * 根据表名称,获取表信息
     *
     * @param tableName
     * @return
     */
    TableInfoDTO getTableInfo(String tableName);

    /**
     * 获取表字段显示列表
     *
     * @param tableName
     * @return
     */
    List<String[]> getTableColumnDisplay(String tableName);

}
