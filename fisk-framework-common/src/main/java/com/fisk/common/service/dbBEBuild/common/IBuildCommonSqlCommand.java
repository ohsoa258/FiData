package com.fisk.common.service.dbBEBuild.common;

import com.fisk.common.service.dbBEBuild.common.dto.DruidFieldInfoDTO;

import java.util.List;

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
    List<DruidFieldInfoDTO> druidAnalyseSql(String sql);

}
