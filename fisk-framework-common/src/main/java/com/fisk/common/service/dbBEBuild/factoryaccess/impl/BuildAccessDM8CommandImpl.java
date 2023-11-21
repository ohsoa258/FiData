package com.fisk.common.service.dbBEBuild.factoryaccess.impl;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.dbdatatype.DM8TypeEnum;
import com.fisk.common.core.enums.dbdatatype.OpenEdgeTypeEnum;
import com.fisk.common.core.enums.dbdatatype.PgTypeEnum;
import com.fisk.common.core.enums.dbdatatype.SqlServerTypeEnum;
import com.fisk.common.core.enums.factory.BusinessTimeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.common.service.dbBEBuild.factoryaccess.dto.DataTypeConversionDTO;
import com.fisk.common.service.dbBEBuild.factoryaccess.dto.TableBusinessTimeDTO;

public class BuildAccessDM8CommandImpl implements IBuildAccessSqlCommand {
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
                return DM8ConversionSqlServer(dto);
            case POSTGRESQL:
                return DM8ConversionPg(dto);
            case ORACLE:
                return DM8ConversionOracle(dto);
            case MYSQL:
            case DORIS:
                return DM8ConversionMySQL(dto);
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * open数据类型转MySQL
     *
     * @param dto
     * @return
     */
    private String[] DM8ConversionMySQL(DataTypeConversionDTO dto) {
        return null;
    }

    /**
     * open数据类型转Oracle
     *
     * @param dto
     * @return
     */
    private String[] DM8ConversionOracle(DataTypeConversionDTO dto) {
        return null;
    }

    /**
     * open数据类型转Pg
     *
     * @param dto
     * @return
     */
    private String[] DM8ConversionPg(DataTypeConversionDTO dto) {
        DM8TypeEnum typeEnum = DM8TypeEnum.getEnum(dto.dataType.toUpperCase());
        String[] data = new String[2];
        data[1] = dto.dataLength;
        switch (typeEnum) {
            case TINYINT:
            case BYTE:
            case INTEGER:
            case INT:
            case NUMERIC:
            case NUMBER:
            case SMALLINT:
                data[0] = PgTypeEnum.INT4.getName();
                break;
            case BIGINT:
                data[0] = PgTypeEnum.INT8.getName();
                break;
            case CHAR:
            case CHARACTER:
                data[0] = PgTypeEnum.CHAR.getName();
                break;
            case VARCHAR:
            case VARCHAR2:
            case ROWID:
                data[0] = PgTypeEnum.VARCHAR.getName();
                break;
            case TEXT:
            case LONG:
            case LONGVARCHAR:
            case CLOB:
                data[0] = PgTypeEnum.TEXT.getName();
                break;
            case DATE:
                data[0] = PgTypeEnum.DATE.getName();
                break;
            case TIME:
                data[0] = PgTypeEnum.TIME.getName();
                break;
            case TIMESTAMP:
            case DATETIME:
                data[0] = PgTypeEnum.TIMESTAMP.getName();
                break;
            case DECIMAL:
            case DEC:
            case FLOAT:
            case DOUBLE:
                data[0] = PgTypeEnum.DECIMAL.getName();
                break;
            default:
                data[0] = PgTypeEnum.VARCHAR.getName();
        }
        return data;
    }

    /**
     * openEdge数据类型转SqlServer
     *
     * @param dto
     * @return
     */
    private String[] DM8ConversionSqlServer(DataTypeConversionDTO dto) {
        DM8TypeEnum typeEnum = DM8TypeEnum.getEnum(dto.dataType.toUpperCase());
        String[] data = new String[2];
        data[1] = dto.dataLength;
        switch (typeEnum) {
            case TINYINT:
            case BYTE:
                data[0] = SqlServerTypeEnum.TINYINT.getName();
                break;
            case BINARY:
            case VARBINARY:
            case RAW:
                data[0] = SqlServerTypeEnum.VARBINARY.getName();
                break;
            case INTEGER:
            case INT:
            case NUMERIC:
            case NUMBER:
                data[0] = SqlServerTypeEnum.INT.getName();
                break;
            case SMALLINT:
                data[0] = SqlServerTypeEnum.SMALLINT.getName();
                break;
            case BIGINT:
                data[0] = SqlServerTypeEnum.BIGINT.getName();
                break;
            case CHAR:
            case CHARACTER:
                data[0] = SqlServerTypeEnum.CHAR.getName();
                break;
            case VARCHAR:
            case VARCHAR2:
            case ROWID:
                data[0] = SqlServerTypeEnum.NVARCHAR.getName();
                break;
            case TEXT:
            case LONG:
            case LONGVARCHAR:
            case CLOB:
                data[0] = SqlServerTypeEnum.TEXT.getName();
                break;
            case DATE:
                data[0] = SqlServerTypeEnum.DATE.getName();
                break;
            case TIME:
                data[0] = SqlServerTypeEnum.TIME.getName();
                break;
            case TIMESTAMP:
            case DATETIME:
                data[0] = SqlServerTypeEnum.TIMESTAMP.getName();
                break;
            case DECIMAL:
            case DEC:
                data[0] = SqlServerTypeEnum.DECIMAL.getName();
                break;
            case FLOAT:
            case DOUBLE:
                data[0] = SqlServerTypeEnum.FLOAT.getName();
                break;
            case IMAGE:
            case LONGVARBINARY:
                data[0] = SqlServerTypeEnum.IMAGE.getName();
                break;
            default:
                data[0] = SqlServerTypeEnum.NVARCHAR.getName();
        }
        return data;
    }

    @Override
    public JSONObject dataTypeList() {
        return null;
    }

    @Override
    public String buildVersionDeleteSql(String tableName) {
        return null;
    }
}
