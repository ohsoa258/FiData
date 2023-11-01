package com.fisk.dataaccess.utils.sql;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lsj
 * @date 20231030
 */
@Component
@Slf4j
public class HiveUtils {

    public static List<String> getAllDatabases(Connection conn) {

        List<String> dbName = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            // 执行查询语句
            ResultSet rs = stmt.executeQuery("SHOW DATABASES");

            // 遍历结果集并获取数据库名
            while (rs.next()) {
                dbName.add(rs.getString(1));
            }
        } catch (SQLException e) {
            log.error("获取HIVE所有库失败,{}", e);
            throw new FkException(ResultEnum.GET_DATABASE_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(stmt);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return dbName;

    }
}
