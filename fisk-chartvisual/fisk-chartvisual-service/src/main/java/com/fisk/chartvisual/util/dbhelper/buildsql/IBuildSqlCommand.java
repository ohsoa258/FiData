package com.fisk.chartvisual.util.dbhelper.buildsql;

import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.dto.SlicerQueryObject;

/**
 * 构建sql命令
 *
 * @author gy
 */
public interface IBuildSqlCommand {

    /**
     * 创建sql：获取所有表/字段
     *
     * @param dbName 数据库名称
     * @return sql
     */
    String buildDataDomainQuery(String dbName);

    /**
     * 获取数据
     * @param query 查询参数
     * @param aggregation 是否是汇总查询
     * @return sql
     */
    String buildQueryData(ChartQueryObject query, boolean aggregation);

    /**
     * 获取统计数据
     * @param query 查询参数
     * @return sql
     */
    String buildQueryAggregation(ChartQueryObject query);

    /**
     * 获取切片器数据
     *
     * @param query 查询参数
     * @return sql
     */
    String buildQuerySlicer(SlicerQueryObject query);
}
