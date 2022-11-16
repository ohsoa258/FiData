package com.fisk.dataaccess.service;

import com.fisk.dataaccess.dto.dataops.TableInfoDTO;

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

}
