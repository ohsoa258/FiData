package com.fisk.common.core.utils.dbutils.utils;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Slf4j
public class PgSqlUtils {

    /**
     * @param conn
     * @return
     */
    public static List<TableNameDTO> getTableName(Connection conn) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            // 获取指定数据库所有表
            rs = stmt.executeQuery(buildQueryTableBySchemaSql());
            List<TableNameDTO> list = new ArrayList<>();
            while (rs.next()) {
                TableNameDTO tablePyhName = new TableNameDTO();
                tablePyhName.tableName = rs.getString("tablename");
                list.add(tablePyhName);
            }
            return list;
        } catch (SQLException e) {
            log.error("【pgSql获取库中所有表失败】,{}", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
            AbstractCommonDbHelper.closeStatement(stmt);
        }
    }

    /**
     * 获取指定schema下所有表名sql
     *
     * @return
     */
    private static String buildQueryTableBySchemaSql() {
        StringBuilder str = new StringBuilder();
        str.append("select ");
        str.append("tablename ");
        str.append("from ");
        str.append("pg_tables ");
        str.append("' ORDER BY ");
        str.append("tablename;");

        return str.toString();
    }

}
