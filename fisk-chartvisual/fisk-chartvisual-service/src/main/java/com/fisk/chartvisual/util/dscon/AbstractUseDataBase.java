package com.fisk.chartvisual.util.dscon;

import com.alibaba.fastjson.JSON;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.utils.BeanHelper;
import com.fisk.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.List;

/**
 * 数据源连接接口
 *
 * @author gy
 */
@Slf4j
public abstract class AbstractUseDataBase {

    private final DataSourceTypeEnum type;

    public AbstractUseDataBase(DataSourceTypeEnum type) {
        this.type = type;
    }

    /**
     * 连接
     */
    public Connection connection(String connectionStr, String acc, String pwd) {
        try {
            loadDriver();
            return getConnectionByType(connectionStr, acc, pwd);
        } catch (SQLException e) {
            log.error("【connection】数据库连接获取失败, ex", e);
        } catch (Exception e) {
            log.error("【connection】" + type.getName() + "数据库连接报错, ex", e);
        }
        return null;
    }

    /**
     * 执行查询
     *
     * @param sql 查询语句
     * @param con 数据库连接
     * @return 查询结果
     */
    public <T> List<T> execQuery(String sql, Connection con, Class<T> tClass) {
        Statement st = null;
        try {
            st = con.createStatement();
            ResultSet res = st.executeQuery(sql);
            return BeanHelper.resultSetToList(res, tClass);
        } catch (SQLException ex) {
            log.error("【execQuery】执行sql查询报错, ex", ex);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    log.error("【execQuery】关闭Statement对象报错, ex", ex);
                }
            }
        }
        return null;
    }

    public abstract String buildDataDomainQuery(String dbName);

    /* ---------------------------------------------------- */

    /**
     * 根据连接类型获取数据库连接
     *
     * @param connectionStr 连接字符串
     * @param acc           账号
     * @param pwd           密码
     * @return 数据源连接
     * @throws Exception 数据源连接异常
     */
    private Connection getConnectionByType(String connectionStr, String acc, String pwd) throws Exception {
        switch (type) {
            case SQLSERVER:
                return DriverManager.getConnection(connectionStr);
            case MYSQL:
                return DriverManager.getConnection(connectionStr, acc, pwd);
            default:
                return null;
        }
    }

    /**
     * 加载驱动
     *
     * @throws Exception 驱动加载异常
     */
    private void loadDriver() throws Exception {
        if (type != null) {
            try {
                Class.forName(type.getDriverName());
            } catch (ClassNotFoundException e) {
                throw new Exception("【loadDriver】" + type.getName() + "驱动加载失败, ex", e);
            }
        } else {
            throw new Exception("【loadDriver】错误的驱动类型");
        }
    }

}
