package com.fisk.common.service.dbBEBuild.common.impl;

import com.fisk.common.service.dbBEBuild.common.IBuildCommonSqlCommand;
import com.fisk.common.service.dbBEBuild.common.dto.DruidFieldInfoDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public class BuildCommonOracleCommand implements IBuildCommonSqlCommand {

    @Override
    public String buildAllDbSql() {
        return "select OWNER as dbname from all_tables where OWNER not in ('SYS','SYSTEM')";
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
