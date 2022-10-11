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

}
