package com.fisk.dataaccess.utils.createTblUtils;

import com.fisk.dataaccess.entity.TableFieldsPO;

import java.util.List;

public interface IBuildCreateTableFactory {

    /**
     * 获取建表语句
     *
     * @param tableName 物理表名
     * @param fieldList 字段集合
     * @return
     */
    String createTable(String tableName, List<TableFieldsPO> fieldList);

    /**
     * 获取校验表是否存在语句
     *
     * @return
     */
    String checkTableIfNotExists();

}
