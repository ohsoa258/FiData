package com.fisk.common.service.dbBEBuild.factoryaccess.impl;

import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;

/**
 * @author JianWenYang
 */
public class BuildAccessPgCommandImpl implements IBuildAccessSqlCommand {

    @Override
    public String buildUseExistTable() {
        return null;
    }

    @Override
    public String buildPaging(String sql, Integer pageSize, Integer offset) {
        StringBuilder str = new StringBuilder();
        str.append(sql);
        str.append(" limit " + pageSize + " offset " + offset);
        return str.toString();
    }

    @Override
    public String buildVersionSql(String type, String value) {
        if (type.equals("年")) {
            value = "SELECT EXTRACT\n" +
                    "\t( YEAR FROM now( ) :: TIMESTAMP ) AS VERSION;";
        } else if (type.equals("季")) {
            value = "SELECT EXTRACT\n" +
                    "\t( YEAR FROM now( ) :: TIMESTAMP ) || '/Q' ||\n" +
                    "CASE\n" +
                    "\t\t\n" +
                    "\t\tWHEN EXTRACT ( MONTH FROM now( ) :: TIMESTAMP ) <= 3 THEN\n" +
                    "\t\t'01' \n" +
                    "\t\tWHEN EXTRACT ( MONTH FROM now( ) :: TIMESTAMP ) <= 6 THEN\n" +
                    "\t\t'02' \n" +
                    "\t\tWHEN EXTRACT ( MONTH FROM now( ) :: TIMESTAMP ) <= 9 THEN\n" +
                    "\t\t'03' ELSE'04' \n" +
                    "\tEND AS VERSION;";
        } else if (type.equals("月")) {
            value = "SELECT EXTRACT\n" +
                    "\t( YEAR FROM now( ) :: TIMESTAMP ) || '/' || EXTRACT ( MONTH FROM now( ) :: TIMESTAMP ) AS VERSION;";
        } else if (type.equals("周")) {
            value = "SELECT EXTRACT\n" +
                    "\t( YEAR FROM now( ) :: TIMESTAMP ) || '/W' || date_part( 'week', now( ) :: TIMESTAMP ) AS VERSION;";
        } else if (type.equals("日")) {
            value = "SELECT\n" +
                    "\tto_char( now( ) :: TIMESTAMP, 'YYYY/MM/DD' ) AS VERSION;";
        } else if (type.equals("自定义")) {

        }
        return value;
    }

    @Override
    public String buildWeekSql(){
        String sql="SELECT date_part( 'week', now( ) :: TIMESTAMP ) AS WeekValue";
        return  sql;
    }
}
