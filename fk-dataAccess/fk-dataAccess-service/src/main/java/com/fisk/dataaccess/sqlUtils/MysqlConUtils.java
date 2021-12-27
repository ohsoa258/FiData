package com.fisk.dataaccess.sqlUtils;

import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lock
 * <p>
 * MySQL 获取表及表字段
 */
@Slf4j
public class MysqlConUtils {

    /**
     * 获取表及表字段
     * @param url url
     * @param user user
     * @param password password
     * @return 查询结果
     */
    public List<TablePyhNameDTO> getTableNameAndColumns(String url, String user, String password){

        List<TablePyhNameDTO> list = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            // 获取数据库中所有表名称
            List<String> tableNames = getTables(conn);
            Statement st = conn.createStatement();

            list = new ArrayList<>();

            int tag = 0;

            for (String tableName : tableNames) {
                ResultSet rs = st.executeQuery("select * from " + tableName);

                List<TableStructureDTO> colNames = getColNames(rs);

                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(tableName);
                tablePyhNameDTO.setFields(colNames);

                tag++;
                tablePyhNameDTO.setTag(tag);

                list.add(tablePyhNameDTO);

                rs.close();
            }

            st.close();
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            log.error("【getTableNameAndColumns】获取表名报错, ex", e);
            return null;
        }

        return list;
    }

    /**
     * 获取数据库中所有表名称
     *
     * @param conn conn
     * @return 返回值
     */
    private List<String> getTables(Connection conn){
        ArrayList<String> tablesList = null;
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet tables = databaseMetaData.getTables(null, null, "%", null);
            tablesList = new ArrayList<String>();
            while (tables.next()) {
                tablesList.add(tables.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR);
        }
        return tablesList;
    }

    /**
     * 获取表中所有字段名称
     *
     * @param rs rs
     */
    private List<TableStructureDTO> getColNames(ResultSet rs){
        List<TableStructureDTO> colNameList = null;
        try {
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
            rs.first();
        } catch (SQLException e) {
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return colNameList;
    }

}
