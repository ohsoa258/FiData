package com.fisk.chartvisual.util.dbhelper.buildsql;

import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.dto.SlicerQueryObject;

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
     * @param query 查询参数
     * @return sql
     */
    String buildQueryData(ChartQueryObject query);

    /**
     * 获取切片器数据
     * @param query 查询参数
     * @return sql
     */
    String buildQuerySlicer(SlicerQueryObject query);
}
