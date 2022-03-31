package com.fisk.dataaccess.utils.sql;

import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lock
 * <p>
 * SqlServer 获取表及表字段
 */
@Slf4j
public class SqlServerConUtils {

    private static Connection conn = null;
    private static Statement stmt = null;

    /**
     * 获取SQL server具体库中所有表名
     *
     * @param conn   连接通道
     * @param dbName 库名
     * @return tableName集合
     */
    public List<String> getTables(Connection conn, String dbName) {
        ArrayList<String> tableList = null;
        try {
            Statement stmt = conn.createStatement();

            ResultSet resultSet = stmt.executeQuery("SELECT name FROM " + dbName + "..sysobjects Where xtype='U' ORDER BY name");
            tableList = new ArrayList<>();
            while (resultSet.next()) {
                tableList.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            log.error("【getTables】获取表名报错, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR);
        }
        return tableList;
    }


    /**
     * 根据tableName获取tableFields
     *
     * @param tableName tableName
     * @return tableName中的表字段
     */
    public List<TableStructureDTO> getColumnsName(Connection conn, String tableName) {
        List<TableStructureDTO> colNameList = null;
        try {
            colNameList = new ArrayList<>();

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet resultSet = metaData.getColumns(null, "%", tableName, "%");
            while (resultSet.next()) {
                TableStructureDTO dto = new TableStructureDTO();
                dto.fieldName = resultSet.getString("COLUMN_NAME");
                dto.fieldType = resultSet.getString("TYPE_NAME");
                dto.fieldLength = Integer.parseInt(resultSet.getString("COLUMN_SIZE"));
                dto.fieldDes = resultSet.getString("REMARKS");
                colNameList.add(dto);
            }

        } catch (Exception e) {
            log.error("【getColumnsName】获取表字段报错, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return colNameList;
    }

    /**
     * 根据库名获取下属表及表字段
     *
     * @param url      url
     * @param user     user
     * @param password password
     * @param dbName   库名
     * @return 下属表及表字段
     */
    public List<TablePyhNameDTO> getTableNameAndColumns(String url, String user, String password, String dbName) {

        List<TablePyhNameDTO> list = null;

        try {
            //1.加载驱动程序
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            //2.获得数据库的连接
            conn = DriverManager.getConnection(url, user, password);
            stmt = conn.createStatement();
            list = new ArrayList<>();

            // 获取指定数据库所有表
            List<String> tableNames = this.getTables(conn, dbName);

            int tag = 0;
            for (String tableName : tableNames) {
                // 获取字段名
                List<TableStructureDTO> columnsName = getColumnsName(conn, tableName);
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(tableName);
                tablePyhNameDTO.setFields(columnsName);

                tag++;
                //tablePyhNameDTO.setTag(tag);
                list.add(tablePyhNameDTO);
            }

            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            log.error("【getTableNameAndColumns】获取表及表字段报错, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return list;
    }

}
