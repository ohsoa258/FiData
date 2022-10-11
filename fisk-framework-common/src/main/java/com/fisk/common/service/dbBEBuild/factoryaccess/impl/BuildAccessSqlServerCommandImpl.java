package com.fisk.common.service.dbBEBuild.factoryaccess.impl;

import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;

/**
 * @author JianWenYang
 */
public class BuildAccessSqlServerCommandImpl implements IBuildAccessSqlCommand {

    @Override
    public String buildUseExistTable() {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append("* ");
        str.append("FROM");
        str.append("(");
        str.append("SELECT ");
        str.append("CONCAT(sysusers.name,'.',sysobjects.name) as name,");
        str.append("substring(sysobjects.name,0,5) as prefix ");
        str.append("FROM ");
        str.append("sysobjects ");
        str.append("INNER JOIN sysusers ON sysobjects.uid = sysusers.uid ");
        str.append("WHERE ");
        str.append("xtype= 'U' ");
        str.append(") as t ");
        str.append("where ");
        str.append("t.prefix <> 'ods_' ");
        str.append("and t.prefix <> 'stg_'");
        return str.toString();
    }

    @Override
    public String buildPaging(String sql, Integer pageSize, Integer offset) {
        StringBuilder str = new StringBuilder();
        str.append(sql);
        str.append(" ORDER BY 1 OFFSET 0 ROWS FETCH NEXT " + pageSize + " ROWS ONLY;");
        return str.toString();
    }

}
