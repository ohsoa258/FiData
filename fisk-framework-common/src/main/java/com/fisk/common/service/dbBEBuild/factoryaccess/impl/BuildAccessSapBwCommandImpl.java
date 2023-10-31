package com.fisk.common.service.dbBEBuild.factoryaccess.impl;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.dbdatatype.SapBwFieldTypeEnum;
import com.fisk.common.core.enums.dbdatatype.SqlServerTypeEnum;
import com.fisk.common.core.enums.factory.BusinessTimeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.common.service.dbBEBuild.factoryaccess.dto.DataTypeConversionDTO;
import com.fisk.common.service.dbBEBuild.factoryaccess.dto.TableBusinessTimeDTO;

public class BuildAccessSapBwCommandImpl implements IBuildAccessSqlCommand {
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
                return sapBwConversionSqlServer(dto);
            case POSTGRESQL:
                return sapBwConversionPg(dto);
            case ORACLE:
                return sapBwConversionOracle(dto);
            case MYSQL:
            case DORIS:
                return sapBwConversionMySQL(dto);
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    private String[] sapBwConversionMySQL(DataTypeConversionDTO dto) {
        return null;
    }

    private String[] sapBwConversionOracle(DataTypeConversionDTO dto) {
        return null;
    }

    private String[] sapBwConversionPg(DataTypeConversionDTO dto) {
        return null;
    }

    private String[] sapBwConversionSqlServer(DataTypeConversionDTO dto) {
        SapBwFieldTypeEnum typeEnum = SapBwFieldTypeEnum.getValue(dto.dataType);
        String[] data = new String[2];
        data[1] = dto.dataLength;
        switch (typeEnum) {
            case INT:
                data[0] = SqlServerTypeEnum.INT.getName();
                break;
            case SMALLINT:
                data[0] = SqlServerTypeEnum.SMALLINT.getName();
                break;
            case BIGINT:
                data[0] = SqlServerTypeEnum.BIGINT.getName();
                break;
            case CHAR:
                data[0] = SqlServerTypeEnum.CHAR.getName();
                break;
            case VARCHAR:
                data[0] = SqlServerTypeEnum.VARCHAR.getName();
                break;
            case TEXT:
                data[0] = SqlServerTypeEnum.TEXT.getName();
                break;
            case DATE:
                data[0] = SqlServerTypeEnum.DATE.getName();
                break;
            case TIME:
                data[0] = SqlServerTypeEnum.TIME.getName();
                break;
            case TIMESTAMP:
                data[0] = SqlServerTypeEnum.TIMESTAMP.getName();
                break;
            case DECIMAL:
                data[0] = SqlServerTypeEnum.DECIMAL.getName();
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
