package com.fisk.common.service.dbBEBuild.common.impl;

import com.fisk.common.service.dbBEBuild.common.IBuildCommonSqlCommand;

/**
 * @author JianWenYang
 */
public class BuildCommonMySqlCommand implements IBuildCommonSqlCommand {

    @Override
    public String buildAllDbSql() {
        return "SELECT `SCHEMA_NAME` as dbname FROM `information_schema`.`SCHEMATA`;";
    }
}
