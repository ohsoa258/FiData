package com.fisk.common.service.dbBEBuild.dataservice;

import java.util.List;
import java.util.Map;

/**
 * @author dick
 */
public interface IBuildDataServiceSqlCommand {

    /**
     * 分页SQL
     *
     * @param tableName 表名称
     * @param fields 表字段名称
     * @param orderBy 排序条件
     * @param pageIndex 页码
     * @param pageSize 页数
     * @return sql
     */
    String buildPagingSql(String tableName,String fields, String orderBy, Integer pageIndex, Integer pageSize);

    /**
     * 分页SQL
     *
     * @param tableName 表名称
     * @param fields 表字段名称
     * @param orderBy 排序条件
     * @param pageIndex 页码
     * @param pageSize 页数
     * @return sql
     */
    String buildPagingSql(String tableName, List<String> fields, String orderBy, Integer pageIndex, Integer pageSize);

    /**
     * 查询指定表数量
     *
     * @param tableName 表名称
     * @param queryConditions 查询条件
     * @return sql
     */
    String buildQueryCountSql(String tableName, String queryConditions);

    /**
     * 查询指定表数据
     *
     * @param tableName 表名称
     * @param fields 表字段
     * @param queryConditions 查询条件
     * @return sql
     */
    String buildQuerySql(String tableName, String fields, String queryConditions);

    /**
     * 单条新增SQL语句
     *
     * @param tableName 表名称
     * @param member 表字段
     * @return sql
     */
    String buildSingleInsertSql(String tableName, Map<String, Object> member);

    /**
     * 单条修改SQL语句
     *
     * @param tableName 表名称
     * @param member 表字段
     * @return sql
     */
    String buildSingleUpdateSql(String tableName, Map<String, Object> member, String editConditions);

    /**
     * 查询指定库下指定表的字段信息
     *
     * @param tableFramework 架构名
     * @param tableRelName 表名称，不带架构名
     * @return sql
     */
    String buildUseExistTableFiled(String tableFramework, String tableRelName);
}
