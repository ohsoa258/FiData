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

    @Override
    public String buildVersionSql(String type, String value) {
        if (type.equals("年")) {
            value = "SELECT YEAR\n" +
                    "\t( GETDATE( ) ) AS version;";
        } else if (type.equals("季")) {
            value = "SELECT CAST\n" +
                    "\t( YEAR ( GETDATE( ) ) AS VARCHAR ) + '/Q' +\n" +
                    "CASE\n" +
                    "\t\t\n" +
                    "\t\tWHEN MONTH ( GETDATE( ) ) <= 3 THEN\n" +
                    "\t\t'01' \n" +
                    "\t\tWHEN MONTH ( GETDATE( ) ) <= 6 THEN\n" +
                    "\t\t'02' \n" +
                    "\t\tWHEN MONTH ( GETDATE( ) ) <= 9 THEN\n" +
                    "\t\t'03' ELSE '04' \n" +
                    "\tEND AS version;";
        } else if (type.equals("月")) {
            value = "SELECT CAST\n" +
                    "\t( YEAR ( GETDATE( ) ) AS VARCHAR ) + '/' + CAST ( MONTH ( GETDATE( ) ) AS VARCHAR ) AS version;";
        } else if (type.equals("周")) {
            value = "SELECT CAST\n" +
                    "\t( YEAR ( GETDATE( ) ) AS VARCHAR ) + '/W' + Datename( week, GetDate( ) ) AS version;";
        } else if (type.equals("日")) {
            value = "SELECT CONVERT\n" +
                    "\t( CHAR ( 10 ), GetDate( ), 111 ) AS version;";
        } else if (type.equals("自定义")) {
            value = String.format("SELECT (%s) AS version", value);
        }
        return value;
    }

    @Override
    public String buildWeekSql(String date) {
        String sql = String.format("SELECT Datename( week, '%s' ) AS WeekValue;", date);
        return sql;
    }

}
