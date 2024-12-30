package com.fisk.common.service.dbBEBuild.factoryaccess.impl;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.dbdatatype.PITypeEnum;
import com.fisk.common.core.enums.dbdatatype.PgTypeEnum;
import com.fisk.common.core.enums.dbdatatype.SqlServerTypeEnum;
import com.fisk.common.core.enums.factory.BusinessTimeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.common.service.dbBEBuild.factoryaccess.dto.DataTypeConversionDTO;
import com.fisk.common.service.dbBEBuild.factoryaccess.dto.TableBusinessTimeDTO;

public class BuildAccessPICommandImpl implements IBuildAccessSqlCommand {
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
                return PIConversionSqlServer(dto);
            case POSTGRESQL:
                return PIConversionPg(dto);
            case ORACLE:
                return PIConversionOracle(dto);
            case MYSQL:
            case DORIS:
                return PIConversionMySQL(dto);
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
    private String[] PIConversionMySQL(DataTypeConversionDTO dto) {
        return null;
    }

    /**
     * open数据类型转Oracle
     *
     * @param dto
     * @return
     */
    private String[] PIConversionOracle(DataTypeConversionDTO dto) {
        return null;
    }

    /**
     * open数据类型转Pg
     *
     * @param dto
     * @return
     */
    private String[] PIConversionPg(DataTypeConversionDTO dto) {
        PITypeEnum typeEnum = PITypeEnum.getEnum(dto.dataType.toUpperCase());
        String[] data = new String[2];
        data[1] = dto.dataLength;
        switch (typeEnum) {
            case Integer:
                data[0] = PgTypeEnum.INT4.getName();
                break;
            case String:
                data[0] = PgTypeEnum.CHAR.getName();
                break;
            case Binary:
                data[0] = PgTypeEnum.TEXT.getName();
                break;
            case Float:
            case Real:
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
    private String[] PIConversionSqlServer(DataTypeConversionDTO dto) {
        PITypeEnum typeEnum = PITypeEnum.getEnum(dto.dataType.toUpperCase());
        String[] data = new String[2];
        data[1] = dto.dataLength;
        switch (typeEnum) {
            case Integer:
                data[0] = SqlServerTypeEnum.INT.getName();
                break;
            case String:
                data[0] = SqlServerTypeEnum.NVARCHAR.getName();
                break;
            case Binary:
                data[0] = SqlServerTypeEnum.TEXT.getName();
                break;
            case Float:
            case Real:
                data[0] = SqlServerTypeEnum.FLOAT.getName();
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
