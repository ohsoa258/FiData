package com.fisk.common.service.dbBEBuild.common.impl;

import com.fisk.common.service.dbBEBuild.common.IBuildCommonSqlCommand;

/**
 * @author JianWenYang
 */
public class BuildCommonSqlServerCommand implements IBuildCommonSqlCommand {

    @Override
    public String buildAllDbSql() {
        return "SELECT name as dbname FROM  master..sysdatabases WHERE name NOT IN ( 'master', 'model', 'msdb', 'tempdb', 'northwind','pubs' )";
    }
}
