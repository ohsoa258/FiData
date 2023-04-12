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

    public static void buildSchemaSql(Connection conn, String schemaName, DataSourceTypeEnum dataSourceTypeEnum) {
        //判断schema是否存在
        if (checkSchemaExist(conn, schemaName, dataSourceTypeEnum)) {
            return;
        }
        StringBuilder str = new StringBuilder();
        switch (dataSourceTypeEnum) {
            case SQLSERVER:
                str.append("CREATE SCHEMA ");
                str.append(schemaName);
                break;
            case POSTGRESQL:
                str.append("CREATE SCHEMA IF NOT EXISTS ");
                str.append("\"");
                str.append(schemaName);
                str.append("\"");
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

    /**
     * 判断schema是否存在
     *
     * @param conn
     * @param schemaName
     * @return
     */
    public static boolean checkSchemaExist(Connection conn, String schemaName, DataSourceTypeEnum dataSourceTypeEnum) {
        String sql = null;
        switch (dataSourceTypeEnum) {
            case SQLSERVER:
                sql = "SELECT count(1) as num FROM sys.schemas where name = '" + schemaName + "'";
                break;
            case POSTGRESQL:
                sql = "SELECT count(1) as num FROM pg_namespace WHERE nspname = '" + schemaName + "'";
                break;
            default:
                throw new FkException(ResultEnum.SCHEMA_ERROR);
        }
        Integer count = Integer.parseInt(AbstractCommonDbHelper.executeTotalSql(sql, conn, "num"));
        return count > 0 ? true : false;
    }

}
