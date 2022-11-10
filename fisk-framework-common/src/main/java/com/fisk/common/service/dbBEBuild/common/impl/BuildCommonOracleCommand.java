package com.fisk.common.service.dbBEBuild.common.impl;

import com.fisk.common.service.dbBEBuild.common.IBuildCommonSqlCommand;

/**
 * @author JianWenYang
 */
public class BuildCommonOracleCommand implements IBuildCommonSqlCommand {

    @Override
    public String buildAllDbSql() {
        return "select OWNER as dbname from all_tables where OWNER not in ('SYS','SYSTEM')";
    }

    @Override
    public Object druidAnalyseSql(String sql) {
        return null;
    }

}
