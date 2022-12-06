package com.fisk.common.core.utils.dbutils.utils;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
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
public class OracleUtils {

    /**
     * 读取Oracle表、字段信息
     *
     * @param conn
     * @return
     */
    public static List<TableNameDTO> getTableName(Connection conn) {
        List<TableNameDTO> list = new ArrayList<>();

        // 获取数据库中所有表名称
        List<String> tableList = getTables(conn);
        if (CollectionUtils.isEmpty(tableList)) {
            return null;
        }
        list = new ArrayList<>();
        for (String tableName : tableList) {
            TableNameDTO tablePyhNameDTO = new TableNameDTO();
            tablePyhNameDTO.setTableName(tableName);
            list.add(tablePyhNameDTO);
        }

        return list;
    }

    private static List<String> getTables(Connection conn) {
        Statement st = null;
        ResultSet rs = null;
        List<String> list = new ArrayList<>();
        try {
            st = conn.createStatement();
            rs = st.executeQuery(buildAllTableSql());
            while (rs.next()) {
                list.add(rs.getString("table_name"));
            }
        } catch (SQLException e) {
            log.error("getTables ex:{}", e);
            throw new FkException(ResultEnum.DATAACCESS_CONNECTDB_ERROR);
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
            AbstractCommonDbHelper.closeStatement(st);
        }
        return list;
    }

    /**
     * 获取该用户下所有表sql
     *
     * @return
     */
    public static String buildAllTableSql() {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append("table_name ");
        str.append("FROM ");
        str.append("user_tables");
        return str.toString();
    }

}
