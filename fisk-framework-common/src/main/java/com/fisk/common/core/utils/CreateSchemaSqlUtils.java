package com.fisk.common.core.utils;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author JianWenYang
 */
@Slf4j
public class CreateSchemaSqlUtils {

    public static void buildSchemaSql(Connection conn, String schemaName, boolean delete, DataSourceTypeEnum dataSourceTypeEnum) {
        StringBuilder str = new StringBuilder();
        switch (dataSourceTypeEnum) {
            case SQLSERVER:
                str.append(delete == true ? "DROP SCHEMA " : "CREATE SCHEMA ");
                str.append(schemaName);
                break;
            case POSTGRESQL:
                str.append(delete == true ? "DROP SCHEMA " : "CREATE SCHEMA IF NOT EXISTS ");
                str.append(schemaName);
                break;
            default:
                throw new FkException(ResultEnum.SCHEMA_ERROR);
        }
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            boolean execute = stmt.execute(str.toString());
            if (execute) {
                throw new FkException(ResultEnum.SCHEMA_ERROR);
            }
        } catch (SQLException e) {
            log.error("operationSchema ex:", e);
            throw new FkException(ResultEnum.SCHEMA_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(stmt);
            AbstractCommonDbHelper.closeConnection(conn);
        }

    }

}
