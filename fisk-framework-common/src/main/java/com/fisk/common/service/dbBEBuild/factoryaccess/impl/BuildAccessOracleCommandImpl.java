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
public class BuildAccessOracleCommandImpl implements IBuildAccessSqlCommand {
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
                return oracleConversionSqlServer(dto);
            case POSTGRESQL:
                return oracleConversionPg(dto);
            case ORACLE:
                return oracleConversionOracle(dto);
            case MYSQL:
                return oracleConversionMySQL(dto);
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    @Override
    public JSONObject dataTypeList() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("字符串型", "VARCHAR2");
        jsonObject.put("整型", "INT");
        jsonObject.put("时间戳类型", "TIMESTAMP");
        jsonObject.put("浮点型", "NUMBER");
        jsonObject.put("文本型", "CLOB");
        return jsonObject;
    }

    /**
     * oracle转SqlServer
     *
     * @param dto
     * @return
     */
    private String[] oracleConversionSqlServer(DataTypeConversionDTO dto) {
        OracleTypeEnum typeEnum = OracleTypeEnum.getValue(dto.dataType);
        String[] data = new String[1];
        switch (typeEnum) {
            case NUMBER:
                if (dto.precision > 0) {
                    data[0] = SqlServerTypeEnum.FLOAT.getName();
                    break;
                }
                data[0] = SqlServerTypeEnum.INT.getName();
                break;
            case FLOAT:
            case BINARY_DOUBLE:
            case BINARY_FLOAT:
                data[0] = SqlServerTypeEnum.FLOAT.getName();
                break;
            case CLOB:
                data[0] = SqlServerTypeEnum.TEXT.getName();
                break;
            case DATE:
            case TIMESTAMP:
            case TIMESTAMPWITHLOCALTIMEZONE:
            case TIMESTAMPWITHTIMEZONE:
                data[0] = SqlServerTypeEnum.TIMESTAMP.getName();
                break;
            default:
                data[0] = SqlServerTypeEnum.NVARCHAR.getName();
        }
        return data;
    }

    /**
     * oracle转Pg
     *
     * @param dto
     * @return
     */
    private String[] oracleConversionPg(DataTypeConversionDTO dto) {
        OracleTypeEnum typeEnum = OracleTypeEnum.getValue(dto.dataType);
        String[] data = new String[1];
        switch (typeEnum) {
            case NUMBER:
                if (dto.precision > 0) {
                    data[0] = PgTypeEnum.FLOAT4.getName();
                    break;
                }
                data[0] = PgTypeEnum.INT4.getName();
                break;
            case FLOAT:
            case BINARY_DOUBLE:
            case BINARY_FLOAT:
                data[0] = PgTypeEnum.FLOAT4.getName();
                break;
            case CLOB:
                data[0] = PgTypeEnum.TEXT.getName();
                break;
            case DATE:
            case TIMESTAMP:
            case TIMESTAMPWITHLOCALTIMEZONE:
            case TIMESTAMPWITHTIMEZONE:
                data[0] = PgTypeEnum.TIMESTAMP.getName();
                break;
            default:
                data[0] = PgTypeEnum.VARCHAR.getName();
        }
        return data;
    }

    /**
     * oracle转Oracle
     *
     * @param dto
     * @return
     */
    private String[] oracleConversionOracle(DataTypeConversionDTO dto) {
        OracleTypeEnum typeEnum = OracleTypeEnum.getValue(dto.dataType);
        String[] data = new String[1];
        switch (typeEnum) {
            case NUMBER:
                if (dto.precision > 0) {
                    data[0] = OracleTypeEnum.FLOAT.getName();
                    break;
                }
                data[0] = OracleTypeEnum.NUMBER.getName();
                break;
            case FLOAT:
            case BINARY_DOUBLE:
            case BINARY_FLOAT:
                data[0] = OracleTypeEnum.FLOAT.getName();
                break;
            case CLOB:
                data[0] = OracleTypeEnum.CLOB.getName();
                break;
            case DATE:
            case TIMESTAMP:
            case TIMESTAMPWITHLOCALTIMEZONE:
            case TIMESTAMPWITHTIMEZONE:
                data[0] = OracleTypeEnum.TIMESTAMP.getName();
                break;
            default:
                data[0] = OracleTypeEnum.VARCHAR2.getName();
        }
        return data;
    }

    /**
     * oracle转MySql
     *
     * @param dto
     * @return
     */
    private String[] oracleConversionMySQL(DataTypeConversionDTO dto) {
        OracleTypeEnum typeEnum = OracleTypeEnum.getValue(dto.dataType);
        String[] data = new String[1];
        switch (typeEnum) {
            case NUMBER:
                if (dto.precision > 0) {
                    data[0] = MySqlTypeEnum.FLOAT.getName();
                    break;
                }
                data[0] = MySqlTypeEnum.INT.getName();
                break;
            case FLOAT:
            case BINARY_DOUBLE:
            case BINARY_FLOAT:
                data[0] = MySqlTypeEnum.FLOAT.getName();
                break;
            case CLOB:
                data[0] = MySqlTypeEnum.TEXT.getName();
                break;
            case DATE:
            case TIMESTAMP:
            case TIMESTAMPWITHLOCALTIMEZONE:
            case TIMESTAMPWITHTIMEZONE:
                data[0] = MySqlTypeEnum.TIMESTAMP.getName();
                break;
            default:
                data[0] = MySqlTypeEnum.VARCHAR.getName();
        }
        return data;
    }

}
