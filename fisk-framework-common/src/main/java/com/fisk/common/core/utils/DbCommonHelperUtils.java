package com.fisk.common.core.utils;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Slf4j
public class DbCommonHelperUtils {

    /**
     * 获取数据库中所有库
     *
     * @param conn
     * @param sql
     * @return
     */
    public static List<String> getAllDatabases(Connection conn, String sql) {
        List<String> dbName = new ArrayList<>();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                dbName.add(rs.getString("dbname"));
            }
            //Oracle数据库会存在查询重复的问题
            if (!CollectionUtils.isEmpty(dbName)) {
                dbName = dbName.stream().distinct().collect(Collectors.toList());
            }
        } catch (SQLException e) {
            log.error("获取所有数据库失败,{}", e);
            throw new FkException(ResultEnum.GET_DATABASE_ERROR);
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return dbName;
    }

}
