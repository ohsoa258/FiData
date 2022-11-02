package com.fisk.common.service.dbBEBuild.factoryaccess;

/**
 * @author JianWenYang
 */
public interface IBuildAccessSqlCommand {

    /**
     * cdc类型获取已存在表sql
     *
     * @return
     */
    String buildUseExistTable();

    /**
     * 基于sql,只取前几条数据sql
     *
     * @param sql
     * @param pageSize
     * @param offset
     * @return
     */
    String buildPaging(String sql, Integer pageSize, Integer offset);

    /**
     * @description 创建版本
     * @author dick
     * @date 2022/11/2 10:30
     * @version v1.0
     * @params
     * @return java.lang.String
     */
    String buildVersionSql(String type,String value);

    /**
     * @description 获取周数
     * @author dick
     * @date 2022/11/2 18:49
     * @version v1.0
     * @params
     * @return java.lang.String
     */
    String buildWeekSql();
}
