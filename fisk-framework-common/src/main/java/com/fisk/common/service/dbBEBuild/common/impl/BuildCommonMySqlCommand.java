package com.fisk.common.service.dbBEBuild.common.impl;

import com.fisk.common.service.dbBEBuild.common.IBuildCommonSqlCommand;
import com.fisk.common.service.dbBEBuild.common.dto.DruidFieldInfoDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public class BuildCommonMySqlCommand implements IBuildCommonSqlCommand {

    @Override
    public String buildAllDbSql() {
        return "SELECT `SCHEMA_NAME` as dbname FROM `information_schema`.`SCHEMATA`;";
    }

    @Override
    public List<DruidFieldInfoDTO> druidAnalyseSql(String sql) {
        return null;
    }

    @Override
    public String buildColumnInfo(String tableName) {
        return null;
    }

}
