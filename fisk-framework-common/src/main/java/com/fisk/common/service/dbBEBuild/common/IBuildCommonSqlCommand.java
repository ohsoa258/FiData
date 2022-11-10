package com.fisk.common.service.dbBEBuild.common;

/**
 * @author JianWenYang
 */
public interface IBuildCommonSqlCommand {

    /**
     * 获取数据库中所有库sql
     *
     * @return
     */
    String buildAllDbSql();

    /**
     * druid分析sql
     *
     * @param sql
     * @return
     */
    Object druidAnalyseSql(String sql);

}
