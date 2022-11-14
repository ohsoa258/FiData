package com.fisk.common.service.dbBEBuild.factoryaccess.impl;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.dbdatatype.*;
import com.fisk.common.core.enums.factory.BusinessTimeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.common.service.dbBEBuild.factoryaccess.dto.DataTypeConversionDTO;
import com.fisk.common.service.dbBEBuild.factoryaccess.dto.TableBusinessTimeDTO;

/**
 * @author JianWenYang
 */
public class BuildAccessPgCommandImpl implements IBuildAccessSqlCommand {

    private static Integer sql_server_max_length = 4000;

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
    public String buildQueryTimeSql(BusinessTimeEnum timeEnum) {
        String sql = null;
        switch (timeEnum) {
            case YEAR:
                sql = "SELECT EXTRACT(MONTH FROM now()) as tmp";
                break;
            case MONTH:
                sql = "SELECT EXTRACT(DAY FROM now()) AS tmp";
                break;
            case DAY:
                sql = "SELECT EXTRACT(HOUR FROM now()) AS tmp";
                break;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
        return sql;
    }

    @Override
    public String buildBusinessCoverCondition(TableBusinessTimeDTO dto, Integer businessDate) {
        StringBuilder str = new StringBuilder();
        str.append("WHERE (CASE WHEN LENGTH(");
        str.append(dto.businessTimeField);
        str.append(")=13 THEN TO_TIMESTAMP(TO_NUMBER(");
        str.append(dto.businessTimeField);
        str.append(",");
        str.append("''9999999999999'')/1000) ELSE TO_TIMESTAMP(");
        str.append(dto.businessTimeField);
        str.append(",''YYYY-MM-DD HH24:MI:SS'') END)");

        //普通模式
        if (dto.otherLogic == 1 || businessDate < dto.businessDate) {
            str.append(dto.businessOperator);
            str.append("now() + (");
            str.append(dto.businessRange);
            str.append(" * interval ''1 ");
            str.append(dto.rangeDateUnit);
            str.append(" '') and enableflag=''Y'' and sync_type=''2'' and verify_type =''3'' or  verify_type =''4'';");

            return str.toString();
        }

        str.append(dto.businessOperatorStandby);
        str.append("now() + (");
        str.append(dto.businessRangeStandby);
        str.append(" * interval ''1");
        str.append(dto.rangeDateUnitStandby);
        str.append(" '') and enableflag=''Y'' and sync_type=''2'' and verify_type =''3'' or  verify_type =''4'';");

        return str.toString();
    }

    @Override
    public String buildVersionSql(String type, String value) {
        if (type.equals("年")) {
            value = "SELECT EXTRACT\n" +
                    "\t( YEAR FROM now( ) :: TIMESTAMP ) AS fi_version;";
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
                    "\tEND AS fi_version;";
        } else if (type.equals("月")) {
            value = "SELECT EXTRACT\n" +
                    "\t( YEAR FROM now( ) :: TIMESTAMP ) || '/' || EXTRACT ( MONTH FROM now( ) :: TIMESTAMP ) AS fi_version;";
        } else if (type.equals("周")) {
            value = "SELECT EXTRACT\n" +
                    "\t( YEAR FROM now( ) :: TIMESTAMP ) || '/W' || date_part( 'week', now( ) :: TIMESTAMP ) AS fi_version;";
        } else if (type.equals("日")) {
            value = "SELECT\n" +
                    "\tto_char( now( ) :: TIMESTAMP, 'YYYY/MM/DD' ) AS fi_version;";
        } else if (type.equals("自定义")) {
            value = String.format("SELECT (%s) AS fi_version", value);
        }
        return value;
    }

    @Override
    public String buildWeekSql(String date) {
        String sql = String.format("SELECT date_part( 'week', '%s' :: TIMESTAMP ) AS WeekValue", date);
        return sql;
    }

    @Override
    public String buildExistTableSql(String tableName) {
        String sql = String.format("select count(*) as isExists from pg_class where relname = '%s';", tableName);
        return sql;
    }

    @Override
    public String[] dataTypeConversion(DataTypeConversionDTO dto, DataSourceTypeEnum typeEnum) {
        switch (typeEnum) {
            case SQLSERVER:
                return pgConversionSqlServer(dto);
            case POSTGRESQL:
                return pgConversionPg(dto);
            case ORACLE:
                return pgConversionOracle(dto);
            case MYSQL:
                return pgConversionMySQL(dto);
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }

    }

    @Override
    public JSONObject dataTypeList() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("字符串型", "VARCHAR");
        jsonObject.put("整型", "INT4");
        jsonObject.put("大整型", "INT8");
        jsonObject.put("时间戳类型", "TIMESTAMP");
        jsonObject.put("浮点型", "FLOAT4");
        jsonObject.put("文本型", "TEXT");
        return jsonObject;
    }

    /**
     * pg转SqlServer
     *
     * @param dto
     * @return
     */
    private String[] pgConversionSqlServer(DataTypeConversionDTO dto) {
        PgTypeEnum typeEnum = PgTypeEnum.getValue(dto.dataType);
        String[] data = new String[2];
        data[1] = dto.dataLength;
        switch (typeEnum) {
            case INT2:
            case INT4:
            case BIT:
            case FLOAT4:
            case FLOAT8:
                data[0] = SqlServerTypeEnum.INT.getName();
                break;
            case INT8:
                data[0] = SqlServerTypeEnum.BIGINT.getName();
                break;
            case NUMERIC:
            case DECIMAL:
                data[0] = SqlServerTypeEnum.FLOAT.getName();
                break;
            case TEXT:
                data[0] = SqlServerTypeEnum.TEXT.getName();
                break;
            case DATE:
            case TIMESTAMP:
            case TIME:
            case TIMESTAMPtz:
                data[0] = SqlServerTypeEnum.TIMESTAMP.getName();
                break;
            default:
                data[0] = SqlServerTypeEnum.NVARCHAR.getName();
                if (Integer.parseInt(dto.dataLength) > sql_server_max_length) {
                    data[1] = sql_server_max_length.toString();
                }
        }
        return data;
    }

    /**
     * pg转pg
     *
     * @param dto
     * @return
     */
    private String[] pgConversionPg(DataTypeConversionDTO dto) {
        PgTypeEnum typeEnum = PgTypeEnum.getValue(dto.dataType);
        String[] data = new String[2];
        data[1] = dto.dataLength;
        switch (typeEnum) {
            case INT2:
            case INT4:
            case FLOAT4:
            case FLOAT8:
                data[0] = PgTypeEnum.INT4.getName();
                break;
            case INT8:
                data[0] = PgTypeEnum.INT8.getName();
                break;
            case TEXT:
                data[0] = PgTypeEnum.TEXT.getName();
                break;
            case DATE:
            case TIME:
            case TIMESTAMP:
            case TIMESTAMPtz:
                data[0] = PgTypeEnum.TIMESTAMP.getName();
                break;
            case DECIMAL:
            case NUMERIC:
                data[0] = FiDataDataTypeEnum.FLOAT.getName();
                break;
            default:
                data[0] = PgTypeEnum.VARCHAR.getName();
        }
        return data;
    }

    /**
     * pg转Oracle
     *
     * @param dto
     * @return
     */
    private String[] pgConversionOracle(DataTypeConversionDTO dto) {
        PgTypeEnum typeEnum = PgTypeEnum.getValue(dto.dataType);
        String[] data = new String[2];
        data[1] = dto.dataLength;
        switch (typeEnum) {
            case INT2:
            case INT4:
            case INT8:
            case BIT:
            case FLOAT4:
            case FLOAT8:
                data[0] = OracleTypeEnum.INT.getName();
                break;
            case NUMERIC:
            case DECIMAL:
                data[0] = OracleTypeEnum.NUMBER.getName();
                break;
            case TEXT:
                data[0] = OracleTypeEnum.CLOB.getName();
                break;
            case DATE:
            case TIMESTAMP:
            case TIME:
            case TIMESTAMPtz:
                data[0] = OracleTypeEnum.TIMESTAMP.getName();
                break;
            default:
                data[0] = OracleTypeEnum.VARCHAR2.getName();
        }
        return data;
    }

    /**
     * pg转MySQL
     *
     * @param dto
     * @return
     */
    private String[] pgConversionMySQL(DataTypeConversionDTO dto) {
        PgTypeEnum typeEnum = PgTypeEnum.getValue(dto.dataType);
        String[] data = new String[2];
        data[1] = dto.dataLength;
        switch (typeEnum) {
            case INT2:
            case INT4:
            case BIT:
            case FLOAT4:
            case FLOAT8:
                data[0] = MySqlTypeEnum.INT.getName();
                break;
            case INT8:
                data[0] = MySqlTypeEnum.BIGINT.getName();
                break;
            case DECIMAL:
            case NUMERIC:
                data[0] = MySqlTypeEnum.FLOAT.getName();
                break;
            case TIME:
            case DATE:
            case TIMESTAMP:
            case TIMESTAMPtz:
                data[0] = MySqlTypeEnum.TIMESTAMP.getName();
                break;
            case TEXT:
                data[0] = MySqlTypeEnum.TEXT.getName();
                break;
            default:
                data[0] = MySqlTypeEnum.VARCHAR.getName();
        }
        return data;
    }


}
