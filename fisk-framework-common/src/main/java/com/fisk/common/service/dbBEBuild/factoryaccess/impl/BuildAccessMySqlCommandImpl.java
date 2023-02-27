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
public class BuildAccessMySqlCommandImpl implements IBuildAccessSqlCommand {

    private static Integer sql_server_max_length = 4000;

    @Override
    public String buildUseExistTable() {
        return null;
    }

    @Override
    public String buildPaging(String sql, Integer pageSize, Integer offset) {
        return null;
    }

    @Override
    public String buildVersionSql(String type, String value) {
        return null;
    }

    @Override
    public String buildWeekSql(String date) {
        return null;
    }

    @Override
    public String buildExistTableSql(String tableName) {
        return null;
    }

    @Override
    public String buildQueryTimeSql(BusinessTimeEnum timeEnum) {
        return null;
    }

    @Override
    public String buildBusinessCoverCondition(TableBusinessTimeDTO dto, Integer businessDate) {
        return null;
    }

    @Override
    public String[] dataTypeConversion(DataTypeConversionDTO dto, DataSourceTypeEnum typeEnum) {
        switch (typeEnum) {
            case SQLSERVER:
                return mySqlConversionSqlServer(dto);
            case POSTGRESQL:
                return mySqlConversionPg(dto);
            case ORACLE:
                return mySqlConversionOracle(dto);
            case MYSQL:
                return mySqlConversionMySQL(dto);
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    @Override
    public JSONObject dataTypeList() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("字符串型", "VARCHAR");
        jsonObject.put("整型", "INT");
        jsonObject.put("大整型", "BIGINT");
        jsonObject.put("日期类型", "DATE");
        jsonObject.put("时间类型", "TIME");
        jsonObject.put("日期时间类型", "DATETIME");
        jsonObject.put("时间戳类型", "TIMESTAMP");
        jsonObject.put("浮点型", "FLOAT");
        jsonObject.put("文本型", "TEXT");
        jsonObject.put("大文本型", "MEDIUMTEXT");
        return jsonObject;
    }

    @Override
    public String buildVersionDeleteSql(String tableName) {
        return null;
    }

    /**
     * mySql转MySQL
     *
     * @param dto
     * @return
     */
    private String[] mySqlConversionMySQL(DataTypeConversionDTO dto) {
        MySqlTypeEnum typeEnum = MySqlTypeEnum.getValue(dto.dataType);
        String[] data = new String[2];
        data[1] = dto.dataLength;
        switch (typeEnum) {
            case INT:
            case BIT:
            case SMALLINT:
                data[0] = MySqlTypeEnum.INT.getName();
                break;
            case BIGINT:
                data[0] = MySqlTypeEnum.BIGINT.getName();
                break;
            case NUMERIC:
            case DECIMAL:
                data[0] = MySqlTypeEnum.FLOAT.getName();
                break;
            case MEDIUMTEXT:
                data[0] = MySqlTypeEnum.MEDIUMTEXT.getName();
                break;
            case TEXT:
                data[0] = MySqlTypeEnum.TEXT.getName();
                break;
            case DATE:
                data[0] = MySqlTypeEnum.DATE.getName();
                break;
            case TIMESTAMP:
                data[0] = MySqlTypeEnum.TIMESTAMP.getName();
                break;
            case TIME:
                data[0] = MySqlTypeEnum.TIME.getName();
                break;
            case DATETIME:
                data[0] = MySqlTypeEnum.DATETIME.getName();
                break;
            default:
                data[0] = MySqlTypeEnum.VARCHAR.getName();
        }
        return data;
    }

    /**
     * mySql转Pg
     *
     * @param dto
     * @return
     */
    private String[] mySqlConversionPg(DataTypeConversionDTO dto) {
        MySqlTypeEnum typeEnum = MySqlTypeEnum.getValue(dto.dataType);
        String[] data = new String[2];
        data[1] = dto.dataLength;
        switch (typeEnum) {
            case INT:
            case BIT:
            case SMALLINT:
                data[0] = PgTypeEnum.INT4.getName();
                break;
            case BIGINT:
                data[0] = PgTypeEnum.INT8.getName();
                break;
            case NUMERIC:
            case DECIMAL:
                data[0] = PgTypeEnum.FLOAT4.getName();
                break;
            case MEDIUMTEXT:
            case TEXT:
                data[0] = PgTypeEnum.TEXT.getName();
                break;
            case DATE:
                data[0] = PgTypeEnum.DATE.getName();
                break;
            case DATETIME:
            case TIMESTAMP:
                data[0] = PgTypeEnum.TIMESTAMP.getName();
                break;
            case TIME:
                data[0] = PgTypeEnum.TIME.getName();
                break;
            default:
                data[0] = PgTypeEnum.VARCHAR.getName();
        }
        return data;
    }

    /**
     * mySql转SqlServer
     *
     * @param dto
     * @return
     */
    private String[] mySqlConversionSqlServer(DataTypeConversionDTO dto) {
        MySqlTypeEnum typeEnum = MySqlTypeEnum.getValue(dto.dataType);
        String[] data = new String[2];
        data[1] = dto.dataLength;
        switch (typeEnum) {
            case INT:
            case BIT:
            case SMALLINT:
                data[0] = SqlServerTypeEnum.INT.getName();
                break;
            case TINYINT:
                data[0] = SqlServerTypeEnum.NVARCHAR.getName();
                data[1] = "10";
                break;
            case BIGINT:
                data[0] = SqlServerTypeEnum.BIGINT.getName();
                break;
            case NUMERIC:
            case DECIMAL:
                data[0] = SqlServerTypeEnum.FLOAT.getName();
                break;
            case MEDIUMTEXT:
            case TEXT:
                data[0] = SqlServerTypeEnum.NTEXT.getName();
                break;
            case DATE:
                data[0] = SqlServerTypeEnum.DATE.getName();
                break;
            case TIMESTAMP:
                data[0] = SqlServerTypeEnum.TIMESTAMP.getName();
                break;
            case TIME:
                data[0] = SqlServerTypeEnum.TIME.getName();
                break;
            case DATETIME:
                data[0] = SqlServerTypeEnum.DATETIME.getName();
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
     * mySql转Oracle
     *
     * @param dto
     * @return
     */
    private String[] mySqlConversionOracle(DataTypeConversionDTO dto) {
        MySqlTypeEnum typeEnum = MySqlTypeEnum.getValue(dto.dataType);
        String[] data = new String[2];
        data[1] = dto.dataLength;
        switch (typeEnum) {
            case INT:
            case BIT:
            case SMALLINT:
            case BIGINT:
                data[0] = OracleTypeEnum.INT.getName();
                break;
            case NUMERIC:
            case DECIMAL:
                data[0] = OracleTypeEnum.NUMBER.getName();
                break;
            case MEDIUMTEXT:
            case TEXT:
                data[0] = OracleTypeEnum.CLOB.getName();
                break;
            case DATE:
                data[0] = OracleTypeEnum.DATE.getName();
                break;
            case TIMESTAMP:
            case TIME:
            case DATETIME:
                data[0] = OracleTypeEnum.TIMESTAMP.getName();
                break;
            default:
                data[0] = OracleTypeEnum.VARCHAR2.getName();
        }
        return data;
    }

}
