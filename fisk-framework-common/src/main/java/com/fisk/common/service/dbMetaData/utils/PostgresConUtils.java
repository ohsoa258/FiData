package com.fisk.common.service.dbMetaData.utils;

import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    public List<TablePyhNameDTO> getTableNameAndColumns(String url, String user, String password, String driver) {

        List<TablePyhNameDTO> list = null;
        try {
            Class.forName(driver);
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
            log.error("【PostgresConUtils/getTables】读取字段信息异常, ex", e);
            throw new FkException(ResultEnum.PG_READ_FIELD_ERROR);
        }

        return colNameList;
    }

    /**
     * 获取指定表的字段信息（包括字段名称，字段类型，字段长度，备注）
     *
     * @param driver
     * @param url
     * @param user
     * @param pwd
     * @param tableName
     * @return
     */
    public List<Object[]> getTableColumns(String driver, String url, String user, String pwd, String tableName) {
        List<Object[]> result = new ArrayList();
        Connection conn = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, pwd);
            ResultSet rs = conn.getMetaData().getColumns(null, "%", tableName, "%");
            while (rs.next()) {
                Object[] objects = new Object[4];
                //字段名称
                String colName = rs.getString("COLUMN_NAME");
                objects[0] = colName;
                //字段类型
                String dbType = rs.getString("TYPE_NAME");
                objects[1] = dbType;
                //字段长度
                int columnSize = rs.getInt("COLUMN_SIZE");
                objects[2] = columnSize;
                //备注
                String remarks = rs.getString("REMARKS");
                if (remarks == null || remarks.equals("")) {
                    remarks = "";
                }
                objects[3] = remarks;
                result.add(objects);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
