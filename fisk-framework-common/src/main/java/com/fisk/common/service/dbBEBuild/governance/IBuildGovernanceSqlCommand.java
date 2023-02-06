package com.fisk.common.service.dbBEBuild.governance;

import com.fisk.common.service.dbBEBuild.governance.dto.KeyValueMapDto;

import java.util.List;
import java.util.Map;

/**
 * @author dick
 */
public interface IBuildGovernanceSqlCommand {

    /**
     * 分页SQL
     *
     * @param tableName 表名称
     * @param fields    表字段名称
     * @param orderBy   排序条件
     * @param pageIndex 页码
     * @param pageSize  页数
     * @return sql
     */
    String buildPagingSql(String tableName, String fields, String orderBy, Integer pageIndex, Integer pageSize);

    /**
     * 分页SQL
     *
     * @param tableName 表名称
     * @param fields    表字段名称
     * @param orderBy   排序条件
     * @param pageIndex 页码
     * @param pageSize  页数
     * @return sql
     */
    String buildPagingSql(String tableName, List<String> fields, String orderBy, Integer pageIndex, Integer pageSize);

    /**
     * 查询指定表数量
     *
     * @param tableName       表名称
     * @param queryConditions 查询条件
     * @return sql
     */
    String buildQueryCountSql(String tableName, String queryConditions);

    /**
     * 查询指定表数据
     *
     * @param tableName       表名称
     * @param fields          表字段
     * @param queryConditions 查询条件
     * @return sql
     */
    String buildQuerySql(String tableName, String fields, String queryConditions);

    /**
     * 单条新增SQL语句
     *
     * @param tableName 表名称
     * @param member    表字段
     * @return sql
     */
    String buildSingleInsertSql(String tableName, Map<String, Object> member);

    /**
     * 单条修改SQL语句
     *
     * @param tableName 表名称
     * @param member    表字段
     * @return sql
     */
    String buildSingleUpdateSql(String tableName, Map<String, Object> member, String editConditions);

    /**
     * 查询某个库下Schema信息
     *
     * @return sql
     */
    String buildQuerySchemaSql();

    /**
     * 查询某个库下Schema和Table信息
     *
     * @return sql
     */
    String buildQuerySchema_TableSql(List<String> schemaList);

    /**
     * 查询某个库下Schema、Table、Field信息
     *
     * @return sql
     */
    String buildQuerySchema_Table_FieldSql(List<String> schemaList, List<String> tableNameList,List<KeyValueMapDto> fieldNameList);
}
