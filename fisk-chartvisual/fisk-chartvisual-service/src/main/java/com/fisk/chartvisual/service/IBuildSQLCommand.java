package com.fisk.chartvisual.service;

import com.fisk.chartvisual.dto.ChartQueryObject;

/**
 * 构建sql命令
 * @author gy
 */
public interface IBuildSQLCommand {

    /**
     * 创建sql：获取所有表/字段
     * @param dbName 数据库名称
     * @return sql
     */
    String buildDataDomainQuery(String dbName);

    /**
     * 创建sql：获取所有表/字段
     * @return sql
     */
    String buildQueryData(ChartQueryObject query);
}
