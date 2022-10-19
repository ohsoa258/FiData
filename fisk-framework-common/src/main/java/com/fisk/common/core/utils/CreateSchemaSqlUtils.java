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
                if (!delete) {
                    str.append("CREATE SCHEMA " + schemaName + ";");
                    str.append("CREATE SCHEMA " + schemaName + ";");
                    break;
                }
                str.append("DROP SCHEMA " + schemaName + ";");
                str.append("DROP SCHEMA " + schemaName + ";");
                break;
            default:
                break;
        }
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            if (!stmt.execute(str.toString())) {
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
