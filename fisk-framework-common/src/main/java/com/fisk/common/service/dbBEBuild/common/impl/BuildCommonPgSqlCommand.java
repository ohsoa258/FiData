package com.fisk.common.service.dbBEBuild.common.impl;

import com.fisk.common.service.dbBEBuild.common.IBuildCommonSqlCommand;

/**
 * @author JianWenYang
 */
public class BuildCommonPgSqlCommand implements IBuildCommonSqlCommand {

    @Override
    public String buildAllDbSql() {
        return "select datname as dbname from pg_database;";
    }

    @Override
    public Object druidAnalyseSql(String sql) {
        return null;
    }

}
