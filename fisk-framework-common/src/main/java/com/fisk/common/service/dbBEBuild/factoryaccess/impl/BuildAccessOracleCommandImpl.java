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

    private String[] oracleConversionSqlServer(DataTypeConversionDTO dto) {
        OracleTypeEnum typeEnum = OracleTypeEnum.getValue(dto.dataType);
        String[] data = new String[2];
        switch (typeEnum) {
            case NUMBER:
                if (dto.precision > 0) {
                    data[0] = FiDataDataTypeEnum.FLOAT.getDescription();
                    data[1] = SqlServerTypeEnum.FLOAT.getName();
                    break;
                }
                data[0] = FiDataDataTypeEnum.INT.getDescription();
                data[1] = SqlServerTypeEnum.INT.getName();
                break;
            case FLOAT:
            case BINARY_DOUBLE:
            case BINARY_FLOAT:
                data[0] = FiDataDataTypeEnum.FLOAT.getDescription();
                data[1] = SqlServerTypeEnum.FLOAT.getName();
                break;
            case CLOB:
                data[0] = FiDataDataTypeEnum.TEXT.getDescription();
                data[1] = SqlServerTypeEnum.TEXT.getName();
                break;
            case DATE:
            case TIMESTAMP:
            case TIMESTAMPWITHLOCALTIMEZONE:
            case TIMESTAMPWITHTIMEZONE:
                data[0] = FiDataDataTypeEnum.TIMESTAMP.getDescription();
                data[1] = SqlServerTypeEnum.TIMESTAMP.getName();
                break;
            default:
                data[0] = FiDataDataTypeEnum.NVARCHAR.getDescription();
                data[1] = SqlServerTypeEnum.NVARCHAR.getName();
        }
        return data;
    }

    private String[] oracleConversionPg(DataTypeConversionDTO dto) {
        OracleTypeEnum typeEnum = OracleTypeEnum.getValue(dto.dataType);
        String[] data = new String[2];
        switch (typeEnum) {
            case NUMBER:
                if (dto.precision > 0) {
                    data[0] = FiDataDataTypeEnum.FLOAT.getDescription();
                    data[1] = PgTypeEnum.FLOAT4.getName();
                    break;
                }
                data[0] = FiDataDataTypeEnum.INT.getDescription();
                data[1] = PgTypeEnum.INT4.getName();
                break;
            case FLOAT:
            case BINARY_DOUBLE:
            case BINARY_FLOAT:
                data[0] = FiDataDataTypeEnum.FLOAT.getDescription();
                data[1] = PgTypeEnum.FLOAT4.getName();
                break;
            case CLOB:
                data[0] = FiDataDataTypeEnum.TEXT.getDescription();
                data[1] = PgTypeEnum.TEXT.getName();
                break;
            case DATE:
            case TIMESTAMP:
            case TIMESTAMPWITHLOCALTIMEZONE:
            case TIMESTAMPWITHTIMEZONE:
                data[0] = FiDataDataTypeEnum.TIMESTAMP.getDescription();
                data[1] = PgTypeEnum.TIMESTAMP.getName();
                break;
            default:
                data[0] = FiDataDataTypeEnum.NVARCHAR.getDescription();
                data[1] = PgTypeEnum.VARCHAR.getName();
        }
        return data;
    }

    private String[] oracleConversionOracle(DataTypeConversionDTO dto) {
        OracleTypeEnum typeEnum = OracleTypeEnum.getValue(dto.dataType);
        String[] data = new String[2];
        switch (typeEnum) {
            case NUMBER:
                if (dto.precision > 0) {
                    data[0] = FiDataDataTypeEnum.FLOAT.getDescription();
                    data[1] = OracleTypeEnum.FLOAT.getName();
                    break;
                }
                data[0] = FiDataDataTypeEnum.INT.getDescription();
                data[1] = OracleTypeEnum.NUMBER.getName();
                break;
            case FLOAT:
            case BINARY_DOUBLE:
            case BINARY_FLOAT:
                data[0] = FiDataDataTypeEnum.FLOAT.getDescription();
                data[1] = OracleTypeEnum.FLOAT.getName();
                break;
            case CLOB:
                data[0] = FiDataDataTypeEnum.TEXT.getDescription();
                data[1] = OracleTypeEnum.CLOB.getName();
                break;
            case DATE:
            case TIMESTAMP:
            case TIMESTAMPWITHLOCALTIMEZONE:
            case TIMESTAMPWITHTIMEZONE:
                data[0] = FiDataDataTypeEnum.TIMESTAMP.getDescription();
                data[1] = OracleTypeEnum.TIMESTAMP.getName();
                break;
            default:
                data[0] = FiDataDataTypeEnum.NVARCHAR.getDescription();
                data[1] = OracleTypeEnum.VARCHAR2.getName();
        }
        return data;
    }

    private String[] oracleConversionMySQL(DataTypeConversionDTO dto) {
        OracleTypeEnum typeEnum = OracleTypeEnum.getValue(dto.dataType);
        String[] data = new String[2];
        switch (typeEnum) {
            case NUMBER:
                if (dto.precision > 0) {
                    data[0] = FiDataDataTypeEnum.FLOAT.getDescription();
                    data[1] = MySqlTypeEnum.FLOAT.getName();
                    break;
                }
                data[0] = FiDataDataTypeEnum.INT.getDescription();
                data[1] = MySqlTypeEnum.INT.getName();
                break;
            case FLOAT:
            case BINARY_DOUBLE:
            case BINARY_FLOAT:
                data[0] = FiDataDataTypeEnum.FLOAT.getDescription();
                data[1] = MySqlTypeEnum.FLOAT.getName();
                break;
            case CLOB:
                data[0] = FiDataDataTypeEnum.TEXT.getDescription();
                data[1] = MySqlTypeEnum.TEXT.getName();
                break;
            case DATE:
            case TIMESTAMP:
            case TIMESTAMPWITHLOCALTIMEZONE:
            case TIMESTAMPWITHTIMEZONE:
                data[0] = FiDataDataTypeEnum.TIMESTAMP.getDescription();
                data[1] = MySqlTypeEnum.TIMESTAMP.getName();
                break;
            default:
                data[0] = FiDataDataTypeEnum.NVARCHAR.getDescription();
                data[1] = MySqlTypeEnum.VARCHAR.getName();
        }
        return data;
    }

}
