package com.fisk.common.service.dimensionquerysql;


/**
 * @author lishiji
 */
public interface IBuildDimensionQuerySql {

    /**
     * 拼接数仓建模构建维度key脚本
     *
     * @return
     */
    String buildDimensionQuerySql(String startTime,String endTime);
}
