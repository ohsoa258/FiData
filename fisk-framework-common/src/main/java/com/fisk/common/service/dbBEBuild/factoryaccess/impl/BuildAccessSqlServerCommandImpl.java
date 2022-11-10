package com.fisk.common.service.dbBEBuild.factoryaccess.impl;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.dbdatatype.MySqlTypeEnum;
import com.fisk.common.core.enums.dbdatatype.OracleTypeEnum;
import com.fisk.common.core.enums.dbdatatype.PgTypeEnum;
import com.fisk.common.core.enums.dbdatatype.SqlServerTypeEnum;
import com.fisk.common.core.enums.factory.BusinessTimeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.common.service.dbBEBuild.factoryaccess.dto.DataTypeConversionDTO;
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
                    "\t( GETDATE( ) ) AS fi_version;";
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
                    "\tEND AS fi_version;";
        } else if (type.equals("月")) {
            value = "SELECT CAST\n" +
                    "\t( YEAR ( GETDATE( ) ) AS VARCHAR ) + '/' + CAST ( MONTH ( GETDATE( ) ) AS VARCHAR ) AS fi_version;";
        } else if (type.equals("周")) {
            value = "SELECT CAST\n" +
                    "\t( YEAR ( GETDATE( ) ) AS VARCHAR ) + '/W' + Datename( week, GetDate( ) ) AS fi_version;";
        } else if (type.equals("日")) {
            value = "SELECT CONVERT\n" +
                    "\t( CHAR ( 10 ), GetDate( ), 111 ) AS fi_version;";
        } else if (type.equals("自定义")) {
            value = String.format("SELECT (%s) AS fi_version", value);
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

    @Override
    public String[] dataTypeConversion(DataTypeConversionDTO dto, DataSourceTypeEnum typeEnum) {
        switch (typeEnum) {
            case SQLSERVER:
                return sqlServerConversionSqlServer(dto);
            case POSTGRESQL:
                return sqlServerConversionPg(dto);
            case ORACLE:
                return sqlServerConversionOracle(dto);
            case MYSQL:
                return sqlServerConversionMySQL(dto);
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }

    }

    @Override
    public JSONObject dataTypeList() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("字符串型", "NVARCHAR");
        jsonObject.put("整型", "INT");
        jsonObject.put("时间戳类型", "TIMESTAMP");
        jsonObject.put("浮点型", "FLOAT");
        jsonObject.put("文本型", "TEXT");
        return jsonObject;
    }

    /**
     * SqlServer转pg
     *
     * @param dto
     * @return
     */
    private String[] sqlServerConversionPg(DataTypeConversionDTO dto) {
        SqlServerTypeEnum typeEnum = SqlServerTypeEnum.getValue(dto.dataType);
        String[] data = new String[1];
        switch (typeEnum) {
            case INT:
            case SMALLINT:
            case BIGINT:
                data[0] = PgTypeEnum.INT4.getName();
                break;
            case TEXT:
                data[0] = PgTypeEnum.TEXT.getName();
                break;
            case DATE:
            case TIME:
            case TIMESTAMP:
            case DATETIME:
                data[0] = PgTypeEnum.TIMESTAMP.getName();
                break;
            case NUMERIC:
            case DECIMAL:
                data[0] = PgTypeEnum.NUMERIC.getName();
                break;
            default:
                data[0] = PgTypeEnum.VARCHAR.getName();
        }
        return data;
    }

    /**
     * SqlServer转SqlServer
     *
     * @param dto
     * @return
     */
    private String[] sqlServerConversionSqlServer(DataTypeConversionDTO dto) {
        SqlServerTypeEnum typeEnum = SqlServerTypeEnum.getValue(dto.dataType);
        String[] data = new String[1];
        switch (typeEnum) {
            case INT:
            case SMALLINT:
            case BIGINT:
                data[0] = SqlServerTypeEnum.INT.getName();
                break;
            case TEXT:
                data[0] = SqlServerTypeEnum.TEXT.getName();
                break;
            case DATE:
            case TIME:
            case TIMESTAMP:
            case DATETIME:
                data[0] = SqlServerTypeEnum.TIMESTAMP.getName();
                break;
            case NUMERIC:
            case DECIMAL:
                data[0] = SqlServerTypeEnum.NUMERIC.getName();
                break;
            default:
                data[0] = SqlServerTypeEnum.NVARCHAR.getName();
        }
        return data;
    }

    /**
     * SqlServer转MySQL
     *
     * @param dto
     * @return
     */
    private String[] sqlServerConversionMySQL(DataTypeConversionDTO dto) {
        SqlServerTypeEnum typeEnum = SqlServerTypeEnum.getValue(dto.dataType);
        String[] data = new String[2];
        switch (typeEnum) {
            case INT:
            case SMALLINT:
            case BIGINT:
                data[0] = MySqlTypeEnum.INT.getName();
                break;
            case TEXT:
                data[0] = MySqlTypeEnum.TEXT.getName();
                break;
            case DATE:
            case TIME:
            case TIMESTAMP:
            case DATETIME:
                data[0] = MySqlTypeEnum.TIMESTAMP.getName();
                break;
            case NUMERIC:
            case DECIMAL:
                data[0] = MySqlTypeEnum.NUMERIC.getName();
                break;
            default:
                data[0] = MySqlTypeEnum.VARCHAR.getName();
        }
        return data;
    }

    /**
     * SqlServer转Oracle
     *
     * @param dto
     * @return
     */
    private String[] sqlServerConversionOracle(DataTypeConversionDTO dto) {
        SqlServerTypeEnum typeEnum = SqlServerTypeEnum.getValue(dto.dataType);
        String[] data = new String[1];
        switch (typeEnum) {
            case INT:
            case SMALLINT:
            case BIGINT:
                data[0] = OracleTypeEnum.INT.getName();
                break;
            case TEXT:
                data[0] = OracleTypeEnum.CLOB.getName();
                break;
            case DATE:
            case TIME:
            case TIMESTAMP:
            case DATETIME:
                data[0] = OracleTypeEnum.TIMESTAMP.getName();
                break;
            case NUMERIC:
            case DECIMAL:
                data[0] = OracleTypeEnum.NUMBER.getName();
                break;
            default:
                data[0] = OracleTypeEnum.VARCHAR2.getName();
        }
        return data;
    }

}
