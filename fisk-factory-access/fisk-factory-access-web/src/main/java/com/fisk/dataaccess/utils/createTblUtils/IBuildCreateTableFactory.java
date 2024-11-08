package com.fisk.dataaccess.utils.createTblUtils;

import com.fisk.dataaccess.dto.datasource.DataSourceFullInfoDTO;
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

    /**
     * 获取 flink source sql
     *
     * @param tableName 物理表名
     * @param fieldList 字段集合
     * @return
     */
    String createSourceSql(String tableName, List<TableFieldsPO> fieldList, DataSourceFullInfoDTO dto);

    /**
     * 获取 flink sink sql
     *
     * @param tableName 物理表名
     * @param fieldList 字段集合
     * @return
     */
    String createSinkSql(String tableName, List<TableFieldsPO> fieldList, DataSourceFullInfoDTO dto);

    /**
     * 获取 flink insert sql
     *
     * @param tableName 物理表名
     * @param fieldList 字段集合
     * @return
     */
    String createInsertSql(String tableName, List<TableFieldsPO> fieldList, DataSourceFullInfoDTO dto);

}
