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
public class BuildAccessMySqlCommandImpl implements IBuildAccessSqlCommand {
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

    /**
     * mySql转MySQL
     *
     * @param dto
     * @return
     */
    private String[] mySqlConversionMySQL(DataTypeConversionDTO dto) {
        MySqlTypeEnum typeEnum = MySqlTypeEnum.getValue(dto.dataType);
        String[] data = new String[2];
        switch (typeEnum) {
            case INT:
            case BIT:
            case SMALLINT:
            case BIGINT:
                data[0] = FiDataDataTypeEnum.INT.getDescription();
                data[1] = MySqlTypeEnum.INT.getName();
                break;
            case NUMERIC:
            case DECIMAL:
                data[0] = FiDataDataTypeEnum.FLOAT.getDescription();
                data[1] = MySqlTypeEnum.FLOAT.getName();
                break;
            case TEXT:
                data[0] = FiDataDataTypeEnum.TEXT.getDescription();
                data[1] = MySqlTypeEnum.TEXT.getName();
                break;
            case DATE:
            case TIMESTAMP:
            case TIME:
            case DATETIME:
                data[0] = FiDataDataTypeEnum.TIMESTAMP.getDescription();
                data[1] = MySqlTypeEnum.TIMESTAMP.getName();
                break;
            default:
                data[0] = FiDataDataTypeEnum.NVARCHAR.getDescription();
                data[1] = MySqlTypeEnum.VARCHAR.getName();
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
        switch (typeEnum) {
            case INT:
            case BIT:
            case SMALLINT:
            case BIGINT:
                data[0] = FiDataDataTypeEnum.INT.getDescription();
                data[1] = PgTypeEnum.INT4.getName();
                break;
            case NUMERIC:
            case DECIMAL:
                data[0] = FiDataDataTypeEnum.FLOAT.getDescription();
                data[1] = PgTypeEnum.FLOAT4.getName();
                break;
            case TEXT:
                data[0] = FiDataDataTypeEnum.TEXT.getDescription();
                data[1] = PgTypeEnum.TEXT.getName();
                break;
            case DATE:
            case TIMESTAMP:
            case TIME:
            case DATETIME:
                data[0] = FiDataDataTypeEnum.TIMESTAMP.getDescription();
                data[1] = PgTypeEnum.TIMESTAMP.getName();
                break;
            default:
                data[0] = FiDataDataTypeEnum.NVARCHAR.getDescription();
                data[1] = PgTypeEnum.VARCHAR.getName();
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
        switch (typeEnum) {
            case INT:
            case BIT:
            case SMALLINT:
            case BIGINT:
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
            case DATETIME:
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
     * mySql转SqlServer
     *
     * @param dto
     * @return
     */
    private String[] mySqlConversionSqlServer(DataTypeConversionDTO dto) {
        MySqlTypeEnum typeEnum = MySqlTypeEnum.getValue(dto.dataType);
        String[] data = new String[2];
        switch (typeEnum) {
            case INT:
            case BIT:
            case SMALLINT:
            case BIGINT:
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
            case DATETIME:
                data[0] = FiDataDataTypeEnum.TIMESTAMP.getDescription();
                data[1] = SqlServerTypeEnum.TIMESTAMP.getName();
                break;
            default:
                data[0] = FiDataDataTypeEnum.NVARCHAR.getDescription();
                data[1] = SqlServerTypeEnum.NVARCHAR.getName();
        }
        return data;
    }

}
