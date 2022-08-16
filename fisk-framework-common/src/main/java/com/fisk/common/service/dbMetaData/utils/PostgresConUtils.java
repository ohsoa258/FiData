package com.fisk.common.service.dbMetaData.utils;

import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbMetaData.dto.DataBaseViewDTO;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dick
 * @version 1.0
 * @description Postgres 获取表及表字段
 * @date 2022/4/22 14:46
 */
@Slf4j
public class PostgresConUtils {
    /**
     * 获取表及表字段
     *
     * @param url      url
     * @param user     user
     * @param password password
     * @return 查询结果
     */
    public List<TablePyhNameDTO> getTableNameAndColumns(String url, String user, String password, DriverTypeEnum driver) {

        List<TablePyhNameDTO> list = null;
        try {
            Class.forName(driver.getName());
            Connection conn = DriverManager.getConnection(url, user, password);
            // 获取数据库中所有表名称
            List<String> tableNames = getTables(conn);
            Statement st = conn.createStatement();

            list = new ArrayList<>();

            int tag = 0;

            for (String tableName : tableNames) {

                List<TableStructureDTO> colNames = getColNames(st, tableName);

                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(tableName);
                tablePyhNameDTO.setFields(colNames);

                tag++;
                tablePyhNameDTO.setTag(tag);
                list.add(tablePyhNameDTO);

            }
            st.close();
            conn.close();
        } catch (ClassNotFoundException e) {
            System.out.println("找不到驱动程序类 ，加载驱动失败！");
            throw new FkException(ResultEnum.CREATE_PG_CONNECTION);
        } catch (SQLException e) {
            log.error("【PostgresConUtils/getTableNameAndColumns】建立pg数据库连接异常, ex", e);
            throw new FkException(ResultEnum.PG_CONNECT_ERROR);
        }
        return list;
    }

    /**
     * @return java.util.List<com.fisk.dataaccess.dto.DataBaseViewDTO>
     * @description 加载视图详情
     * @author dick
     * @date 2021/12/31 17:46
     * @version v1.0
     * @params driverTypeEnum
     * @params url
     * @params user
     * @params password
     * @params dbName
     */
    public List<DataBaseViewDTO> loadViewDetails(DriverTypeEnum driverTypeEnum, String url, String user, String password, String dbName) {

        List<DataBaseViewDTO> list = null;
        try {
            Class.forName(driverTypeEnum.getName());
            Connection conn = DriverManager.getConnection(url, user, password);
            // 获取数据库中所有视图名称
            List<String> viewNameList = loadViewNameList(conn);
            Statement st = conn.createStatement();

            list = new ArrayList<>();
            for (String viewName : viewNameList) {
                List<TableStructureDTO> colNames = getViewColumns(conn, viewName);
                DataBaseViewDTO dto = new DataBaseViewDTO();
                dto.viewName = viewName;
                dto.fields = colNames;
                list.add(dto);
            }
            st.close();
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            log.error("【loadViewDetails】获取视图信息报错, ex", e);
            throw new FkException(ResultEnum.LOAD_VIEW_STRUCTURE_ERROR);
        }

        return list;
    }

    /**
     * @return java.util.List<java.lang.String>
     * @description 获取视图名称列表
     * @author Lock
     * @date 2021/12/31 17:45
     * @version v1.0
     * @params conn
     * @params dbName
     */
    private List<String> loadViewNameList(Connection conn) {
        ArrayList<String> viewNameList = null;
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            String[] types = {"VIEW"};

            ResultSet rs = null;
            rs = databaseMetaData.getTables(null, null, "%", types);
            viewNameList = new ArrayList<>();
            while (rs.next()) {
                viewNameList.add(rs.getString(3));
            }
        } catch (SQLException e) {
            throw new FkException(ResultEnum.LOAD_VIEW_NAME_ERROR);
        }
        return viewNameList;
    }

    /**
     * 获取数据库中所有表名称
     *
     * @return 返回值
     */
    public List<String> getTableList(String url, String user, String password, String driver) {
        List<String> tableNames = null;
        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, user, password);
            tableNames = getTables(conn);
            conn.close();
        } catch (ClassNotFoundException e) {
            System.out.println("找不到驱动程序类 ，加载驱动失败！");
            throw new FkException(ResultEnum.CREATE_PG_CONNECTION);
        } catch (SQLException e) {
            log.error("【PostgresConUtils/getTableList】建立pg数据库连接异常, ex", e);
            throw new FkException(ResultEnum.PG_READ_TABLE_ERROR);
        }
        return tableNames;
    }

    /**
     * 获取数据库某张表的字段
     *
     * @return 返回值
     */
    public Map<String, List<TableStructureDTO>> getTableColumnList(String url, String user,
                                                                   String password, String driver, List<String> tableNames) {
        Map<String, List<TableStructureDTO>> map = new IdentityHashMap<>();
        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement st = conn.createStatement();
            for (String tableName : tableNames) {
                List<TableStructureDTO> colNames = getColNames(st, tableName);
                map.put(tableName, colNames);
            }
            st.close();
            conn.close();
        } catch (ClassNotFoundException e) {
            System.out.println("找不到驱动程序类 ，加载驱动失败！");
            throw new FkException(ResultEnum.CREATE_PG_CONNECTION);
        } catch (SQLException e) {
            log.error("【PostgresConUtils/getTableColumnList】建立pg数据库连接异常, ex", e);
            throw new FkException(ResultEnum.PG_READ_FIELD_ERROR);
        }
        return map;
    }


    /**
     * 获取数据库中所有表名称
     *
     * @param conn conn
     * @return 返回值
     */
    public List<String> getTables(Connection conn) {
        ArrayList<String> tablesList = null;
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet tables = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"});
            tablesList = new ArrayList<String>();
            while (tables.next()) {
                tablesList.add(tables.getString("TABLE_NAME"));
            }
            tables.close();
        } catch (SQLException e) {
            log.error("【PostgresConUtils/getTables】读取表信息异常, ex", e);
            throw new FkException(ResultEnum.PG_READ_TABLE_ERROR);
        }
        return tablesList;
    }

    /**
     * 获取表中所有字段名称
     *
     * @param tableName tableName
     */
    public List<TableStructureDTO> getColNames(Statement st, String tableName) {
        ResultSet rs = null;
        List<TableStructureDTO> colNameList = null;
        try {
            rs = st.executeQuery("select * from " + tableName + " LIMIT 0;");

            ResultSetMetaData metaData = rs.getMetaData();
            int count = metaData.getColumnCount();
            colNameList = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                TableStructureDTO tableStructureDTO = new TableStructureDTO();

                // 字段名称
                tableStructureDTO.fieldName = metaData.getColumnName(i);
                // 字段类型
                tableStructureDTO.fieldType = metaData.getColumnTypeName(i);
                // 字段长度
                tableStructureDTO.fieldLength = metaData.getColumnDisplaySize(i);

                colNameList.add(tableStructureDTO);
            }
            rs.close();
        } catch (SQLException e) {
            log.error("【PostgresConUtils/getColNames】读取字段信息异常, ex", e);
            throw new FkException(ResultEnum.PG_READ_FIELD_ERROR);
        }

        return colNameList;
    }

    /**
     * 获取指定表视图字段信息
     *
     * @param viewName
     * @return
     */
    public List<TableStructureDTO> getViewColumns(Connection conn, String viewName) {
        List<TableStructureDTO> colNameList = new ArrayList<>();
        try {
            ResultSet rs = conn.getMetaData().getColumns(null, "%", viewName, "%");
            while (rs.next()) {
                TableStructureDTO tableStructureDTO = new TableStructureDTO();
                //字段名称
                tableStructureDTO.setFieldName(rs.getString("COLUMN_NAME"));
                //字段类型
                tableStructureDTO.setFieldType(rs.getString("TYPE_NAME"));
                //字段长度
                tableStructureDTO.setFieldLength(Integer.parseInt(rs.getString("COLUMN_SIZE")));
                //备注
                //tableStructureDTO.setFieldDes(rs.getString("REMARKS"));
                colNameList.add(tableStructureDTO);
            }
        } catch (Exception e) {
            log.error("【PostgresConUtils/getViewColumns】读取字段信息异常, ex", e);
            throw new FkException(ResultEnum.PG_READ_FIELD_ERROR);
        }
        return colNameList;
    }

    /**
     * 创建pgsql连接驱动
     *
     * @param pgUrl       连接字符串
     * @param pgUsername  数据库账号
     * @param pgPpassword 数据库密码
     * @return java.sql.Connection
     * @description
     * @author Lock
     * @date 2022/1/21 17:18
     */
    public static Connection getPgConn(String pgUrl, String pgUsername, String pgPpassword) {
        Connection conn = null;
        try {
            Class.forName(DriverTypeEnum.POSTGRESQL.getName());
            conn = DriverManager.getConnection(pgUrl, pgUsername, pgPpassword);
        } catch (ClassNotFoundException | SQLException e) {
            log.error("【getPgConn】创建pgsql连接驱动失败, ex", e);
            throw new FkException(ResultEnum.CREATE_PG_CONNECTION);
        }
        return conn;
    }

    /**
     * 将数据从stg同步到ods
     *
     * @param sqlList sql集合
     * @param flag    0: 推送数据前清空stg; 1: 推送完数据,开始同步stg->ods
     * @return void
     * @description 将数据从stg同步到ods
     * @author Lock
     * @date 2022/2/25 15:39
     */
    public ResultEnum stgToOds(String pgUrl, String pgUsername, String pgPpassword, List<String> sqlList, int flag) throws SQLException {
        Connection pgConn = getPgConn(pgUrl, pgUsername, pgPpassword);
        Statement statement = pgConn.createStatement();
        try {
            // 执行sql
            System.out.println("操作类型AE86: 0: 推送数据前清空stg; 1: 推送完数据,开始同步stg->ods;  " + flag);
            System.out.println("pg推送数据的函数: " + sqlList.get(flag));
            statement.executeUpdate(sqlList.get(flag));

            statement.close();
            pgConn.close();
        } catch (SQLException e) {
            log.error("批量执行SQL异常: " + e);
            statement.close();
            pgConn.close();
            return ResultEnum.STG_TO_ODS_ERROR;
        }

        return ResultEnum.SUCCESS;
    }

    /**
     * 批量执行pgsql
     *
     * @param res             json数据
     * @param tablePrefixName pg中的物理表前缀名
     * @return void
     * @author Lock
     * @date 2022/1/21 17:24
     */
/*    public ResultEntity<Object> executeBatchPgsql(String tablePrefixName, List<JsonTableData> res) throws Exception {
        Connection con = getPgConn();
        Statement statement = con.createStatement();
        //这里必须设置为false，我们手动批量提交
        con.setAutoCommit(false);
        //这里需要注意，SQL语句的格式必须是预处理的这种，就是values(?,?,...,?)，否则批处理不起作用
        ////PreparedStatement statement = con.prepareStatement("insert into student(id,`name`,age) values(?,?,?)");

        // TODO 调用JsonUtils获取表对象集合
        int countSql = 0;
        List<ApiSqlResultDTO> list = new ArrayList<>();
        try {
            for (JsonTableData re : res) {

                ApiSqlResultDTO apiSqlResultDto = new ApiSqlResultDTO();

                String tableName = re.table;
                JSONArray data = re.data;
                for (Object datum : data) {
                    String insertSqlIndex = "insert into ";
                    String insertSqlLast = "(";
                    String inserSql = "";
                    insertSqlIndex = insertSqlIndex + tablePrefixName + tableName + "(";
                    JSONObject object = (JSONObject) datum;
                    Iterator<Map.Entry<String, Object>> iter = object.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = iter.next();
                        insertSqlIndex = insertSqlIndex + entry.getKey() + ",";
                        insertSqlLast = insertSqlLast + "'" + entry.getValue() + "'" + ",";
                    }
                    insertSqlIndex = insertSqlIndex.substring(0, insertSqlIndex.lastIndexOf(",")) + ") values";
                    insertSqlLast = insertSqlLast.substring(0, insertSqlLast.lastIndexOf(",")) + ")";
                    inserSql = insertSqlIndex + insertSqlLast;
                    log.info("数据推送到stg的sql为: " + inserSql);
                    countSql++;
                    statement.addBatch(inserSql);
                }
                // 批量执行sql
                statement.executeBatch();

                // 保存本次信息
                apiSqlResultDto.setCount(countSql);
                // 多表插入时重新清空条数
                countSql = 0;
                // stg表名
                apiSqlResultDto.setTableName(tablePrefixName + tableName);
                apiSqlResultDto.setMsg("成功");

                list.add(apiSqlResultDto);
            }
            System.out.println("本次添加的sql个数为: " + countSql);
            // 提交要执行的批处理，防止 JDBC 执行事务处理
            con.commit();
            statement.close();
            // 关闭相关连接
            con.close();
        } catch (SQLException e) {
            log.error("批量执行SQL异常: {}", e.getMessage());
            statement.close();
            con.close();
            // 执行sql异常,重置记录的条数
            countSql = 0;
            ApiSqlResultDTO apiSqlResultDto = new ApiSqlResultDTO();
            apiSqlResultDto.setMsg("失败");
            apiSqlResultDto.setCount(0);
            list.add(apiSqlResultDto);
            return ResultEntityBuild.build(ResultEnum.PUSH_DATA_SQL_ERROR, list);
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, JSON.toJSONString(list));
    }

    *//**
     * 批量执行pgsql
     *
     * @param importDataDto   task调用的入参
     * @param tablePrefixName 表前缀
     * @param res             json数据
     * @return com.fisk.common.core.response.ResultEntity<java.lang.Object>
     * @author Lock
     * @date 2022/7/18 14:26
     *//*
    public ResultEntity<Object> executeBatchPgsql(ApiImportDataDTO importDataDto, String tablePrefixName, List<JsonTableData> res) throws Exception {
        Connection con = getPgConn();
        Statement statement = con.createStatement();
        //这里必须设置为false，我们手动批量提交
        con.setAutoCommit(false);
        //这里需要注意，SQL语句的格式必须是预处理的这种，就是values(?,?,...,?)，否则批处理不起作用
        ////PreparedStatement statement = con.prepareStatement("insert into student(id,`name`,age) values(?,?,?)");

        // TODO 调用JsonUtils获取表对象集合
        int countSql = 0;
        List<ApiSqlResultDTO> list = new ArrayList<>();
        try {
            for (JsonTableData re : res) {

                ApiSqlResultDTO apiSqlResultDto = new ApiSqlResultDTO();

                String tableName = re.table;
                JSONArray data = re.data;
                for (Object datum : data) {
                    String insertSqlIndex = "insert into ";
                    String insertSqlLast = "(";
                    String inserSql = "";
                    insertSqlIndex = insertSqlIndex + tablePrefixName + tableName + "(";
                    JSONObject object = (JSONObject) datum;
                    Iterator<Map.Entry<String, Object>> iter = object.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = iter.next();
                        insertSqlIndex = insertSqlIndex + entry.getKey() + ",";
                        insertSqlLast = insertSqlLast + "'" + entry.getValue() + "'" + ",";
                    }
                    insertSqlIndex = insertSqlIndex.substring(0, insertSqlIndex.lastIndexOf(",")) + ") values";
                    insertSqlLast = insertSqlLast.substring(0, insertSqlLast.lastIndexOf(",")) + ")";
                    inserSql = insertSqlIndex + insertSqlLast;
                    log.info("数据推送到stg的sql为: " + inserSql);
                    countSql++;
                    statement.addBatch(inserSql);
                }
                if (importDataDto != null) {
                    String s1 = StringUtils.isNotBlank(importDataDto.pipelTraceId) ? importDataDto.pipelTraceId : "";
                    String s2 = StringUtils.isNotBlank(importDataDto.pipelTaskTraceId) ? importDataDto.pipelTaskTraceId : "";
                    String traceId = StringUtils.isNotBlank(s1) ? s1 : s2;
                    String updateBatchSql = "UPDATE " + tablePrefixName + tableName + " SET " + NifiConstants.AttrConstants.FIDATA_BATCH_CODE
                            + " = '" + traceId + "' WHERE " + NifiConstants.AttrConstants.FIDATA_BATCH_CODE + " IS NULL";

                    log.info("设置" + NifiConstants.AttrConstants.FIDATA_BATCH_CODE + "的update语句为: " + updateBatchSql);

                    statement.addBatch(updateBatchSql);
                }
                // 批量执行sql
                statement.executeBatch();

                // 保存本次信息
                apiSqlResultDto.setCount(countSql);
                // 多表插入时重新清空条数
                countSql = 0;
                // stg表名
                apiSqlResultDto.setTableName(tablePrefixName + tableName);
                apiSqlResultDto.setMsg("成功");

                list.add(apiSqlResultDto);
            }
            System.out.println("本次添加的sql个数为: " + countSql);
            // 提交要执行的批处理，防止 JDBC 执行事务处理
            con.commit();
            statement.close();
            // 关闭相关连接
            con.close();
        } catch (SQLException e) {
            log.error("批量执行SQL异常: {}", e.getMessage());
            statement.close();
            con.close();
            // 执行sql异常,重置记录的条数
            countSql = 0;
            ApiSqlResultDTO apiSqlResultDto = new ApiSqlResultDTO();
            apiSqlResultDto.setMsg("失败");
            apiSqlResultDto.setCount(0);
            list.add(apiSqlResultDto);
            return ResultEntityBuild.build(ResultEnum.PUSH_DATA_SQL_ERROR, list);
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, JSON.toJSONString(list));
    }*/
}
