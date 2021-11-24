package com.fisk.task.utils;

import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import org.springframework.beans.factory.annotation.Value;

import java.sql.*;

/**
 * @author JianWenYang
 */
public class TaskPgTableStructureHelper {
    @Value("${pgsql-ods.url}")
    public String pgsqlOdsUrl;
    @Value("${pgsql-ods.username}")
    public String pgsqlOdsUsername;
    @Value("${pgsql-ods.password}")
    public String pgsqlOdsPassword;
    /**
     * 根据语句修改PgTable表结构
     * @param sql
     * @param tableName
     * @return
     */
    public boolean updatePgTableStructure(String sql,String tableName)
    {
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(pgsqlOdsUrl, pgsqlOdsUsername, pgsqlOdsPassword);
            //判断表是否存在
            DatabaseMetaData metaData=conn.getMetaData();
            ResultSet set=metaData.getTables(null,null,"tableName",null);
            if (!set.next())
            {
                return false;
            }
            //修改表结构
            //return st.execute(sql);
        }catch (ClassNotFoundException | SQLException e) {
            return false;
        }
        return true;
    }
}
