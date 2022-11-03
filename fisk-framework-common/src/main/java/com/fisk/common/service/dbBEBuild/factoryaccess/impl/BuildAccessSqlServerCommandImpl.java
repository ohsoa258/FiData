package com.fisk.common.service.dbBEBuild.factoryaccess.impl;

import com.fisk.common.core.enums.factory.BusinessTimeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.common.service.dbBEBuild.factoryaccess.dto.TableBusinessTimeDTO;

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
    public String buildQueryTimeSql(BusinessTimeEnum timeEnum) {
        String sql = null;
        switch (timeEnum) {
            case YEAR:
                sql = "SELECT MONTH(GETDATE()) AS tmp";
                break;
            case MONTH:
                sql = "SELECT DAY(GETDATE()) AS tmp";
                break;
            case DAY:
                sql = "SELECT DATEPART(HOUR,GETDATE()) AS tmp";
                break;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
        return sql;
    }

    @Override
    public String buildBusinessCoverCondition(TableBusinessTimeDTO dto, Integer businessDate) {
        StringBuilder str = new StringBuilder();
        str.append("where ");
        str.append(dto.businessTimeField + " ");

        //普通模式
        if (dto.otherLogic == 1 || businessDate < dto.businessDate) {
            str.append(dto.businessOperator + " ");
            str.append("DATEADD");
            str.append("(");
            str.append(dto.rangeDateUnit);
            str.append(",");
            str.append(dto.businessRange);
            str.append(",GETDATE()) AND enableflag='Y';");
            return str.toString();
        }
        //高级模式
        str.append(dto.businessOperatorStandby);
        str.append("DATEADD");
        str.append("(");
        str.append(dto.rangeDateUnitStandby);
        str.append(",");
        str.append(dto.businessRangeStandby);
        str.append(",GETDATE()) AND enableflag='Y';");

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

    @Override
    public String buildExistTableSql(String tableName) {
        String sql = String.format("select COUNT(*) as isExists from sysobjects where id = object_id('%s') and id is not null and id<>'';", tableName);
        return sql;
    }

}
