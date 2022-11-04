package com.fisk.common.service.dbBEBuild.factoryaccess.impl;

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
            value = String.format("SELECT (%s) AS version", value);
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


    /**
     * pg转SqlServer
     *
     * @param dto
     * @return
     */
    private String[] pgConversionSqlServer(DataTypeConversionDTO dto) {
        PgTypeEnum typeEnum = PgTypeEnum.getValue(dto.dataType);
        String[] data = new String[2];
        switch (typeEnum) {
            case INT2:
            case INT4:
            case INT8:
            case BIT:
            case FLOAT4:
            case FLOAT8:
                data[0] = FiDataDataTypeEnum.INT.getDescription();
                data[1] = SqlServerTypeEnum.INT.getName();
                break;
            case NUMERIC:
            case DECIMAL:
                data[0] = FiDataDataTypeEnum.FLOAT.getDescription();
                data[1] = SqlServerTypeEnum.FLOAT.getName();
                break;
            case TEXT:
                data[0] = FiDataDataTypeEnum.TEXT.getDescription();
                data[1] = SqlServerTypeEnum.TEXT.getName();
                break;
            case DATE:
            case TIMESTAMP:
            case TIME:
            case TIMESTAMPtz:
                data[0] = FiDataDataTypeEnum.TIMESTAMP.getDescription();
                data[1] = SqlServerTypeEnum.TIMESTAMP.getName();
                break;
            default:
                data[0] = FiDataDataTypeEnum.NVARCHAR.getDescription();
                data[1] = SqlServerTypeEnum.NVARCHAR.getName();
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
        switch (typeEnum) {
            case INT2:
            case INT4:
            case INT8:
            case FLOAT4:
            case FLOAT8:
                data[0] = FiDataDataTypeEnum.INT.getDescription();
                data[1] = FiDataDataTypeEnum.INT.getName();
                break;
            case TEXT:
                data[0] = FiDataDataTypeEnum.TEXT.getDescription();
                data[1] = PgTypeEnum.TEXT.getName();
                break;
            case DATE:
            case TIME:
            case TIMESTAMP:
            case TIMESTAMPtz:
                data[0] = FiDataDataTypeEnum.TIMESTAMP.getDescription();
                data[1] = PgTypeEnum.TIMESTAMP.getName();
                break;
            case DECIMAL:
            case NUMERIC:
                data[0] = FiDataDataTypeEnum.FLOAT.getDescription();
                data[1] = FiDataDataTypeEnum.FLOAT.getName();
                break;
            default:
                data[0] = FiDataDataTypeEnum.NVARCHAR.getDescription();
                data[1] = PgTypeEnum.VARCHAR.getName();
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
        switch (typeEnum) {
            case INT2:
            case INT4:
            case INT8:
            case BIT:
            case FLOAT4:
            case FLOAT8:
                data[0] = FiDataDataTypeEnum.INT.getDescription();
                data[1] = OracleTypeEnum.NUMBER.getName();
                break;
            case NUMERIC:
            case DECIMAL:
                data[0] = FiDataDataTypeEnum.FLOAT.getDescription();
                data[1] = OracleTypeEnum.NUMBER.getName();
                break;
            case TEXT:
                data[0] = FiDataDataTypeEnum.TEXT.getDescription();
                data[1] = OracleTypeEnum.CLOB.getName();
                break;
            case DATE:
            case TIMESTAMP:
            case TIME:
            case TIMESTAMPtz:
                data[0] = FiDataDataTypeEnum.TIMESTAMP.getDescription();
                data[1] = OracleTypeEnum.TIMESTAMP.getName();
                break;
            default:
                data[0] = FiDataDataTypeEnum.NVARCHAR.getDescription();
                data[1] = OracleTypeEnum.VARCHAR2.getName();
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
        switch (typeEnum) {
            case INT2:
            case INT4:
            case INT8:
            case BIT:
            case FLOAT4:
            case FLOAT8:
                data[0] = FiDataDataTypeEnum.INT.getDescription();
                data[1] = MySqlTypeEnum.INT.getName();
                break;
            case DECIMAL:
            case NUMERIC:
                data[0] = FiDataDataTypeEnum.FLOAT.getDescription();
                data[1] = MySqlTypeEnum.FLOAT.getName();
                break;
            case TIME:
            case DATE:
            case TIMESTAMP:
            case TIMESTAMPtz:
                data[0] = FiDataDataTypeEnum.TIMESTAMP.getDescription();
                data[1] = MySqlTypeEnum.TIMESTAMP.getName();
                break;
            case TEXT:
                data[0] = FiDataDataTypeEnum.TEXT.getDescription();
                data[1] = MySqlTypeEnum.TEXT.getName();
                break;
            default:
                data[0] = FiDataDataTypeEnum.NVARCHAR.getDescription();
                data[1] = MySqlTypeEnum.VARCHAR.getName();
        }
        return data;
    }


}
