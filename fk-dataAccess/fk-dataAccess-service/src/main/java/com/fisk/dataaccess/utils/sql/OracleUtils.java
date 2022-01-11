package com.fisk.dataaccess.utils.sql;

import com.fisk.dataaccess.dto.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import com.fisk.dataaccess.enums.DriverTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.*;

/**
 * @author Lock
 * @version 1.0
 * @description Oracle工具类
 * @date 2022/1/10 10:20
 */
@Slf4j
public class OracleUtils {

    private static Connection conn = null;
    private static Statement stmt = null;

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
////                dto.fieldType = resultSet.getString("TYPE_NAME");
////                dto.fieldLength = Integer.parseInt(resultSet.getString("COLUMN_SIZE"));
///               dto.fieldDes = resultSet.getString("REMARKS");
                colNameList.add(dto);
            }

        } catch (Exception e) {
            log.error("【getColumnsName】获取表字段报错, ex", e);
            return null;
        }
        return colNameList;
    }

    public Map<String, String> getTables(Connection conn, String dbName) {
//        ArrayList<Map<String, String>> tableList = null;
        Map<String, String> tableMap = new LinkedHashMap<>();
        try {
            Statement stmt = conn.createStatement();

            ResultSet resultSet = stmt.executeQuery("select *,RANK() over(order by tabl.field) from \n" +
                    "(\n" +
                    "select name, schema_name(schema_id) as field from sys.tables\n" +
                    ") as tabl");
//            tableList = new ArrayList<>();
            while (resultSet.next()) {
                // TABLE_NAME
                String name = resultSet.getString("name");
                // 架构名
                String field2 = resultSet.getString("field");
                tableMap.put(name, field2);
            }
        } catch (SQLException e) {
            log.error("【getTablesPlus】获取表名及架构名失败, ex", e);
            return null;
        }
        return tableMap;
    }

    public List<TablePyhNameDTO> getTableNameAndColumns(String url, String user, String password, String dbName) {

        List<TablePyhNameDTO> list = null;

        try {
            //1.加载驱动程序
            Class.forName(DriverTypeEnum.ORACLE.getName());
            //2.获得数据库的连接
            conn = DriverManager.getConnection(url, user, password);
            stmt = conn.createStatement();
            list = new ArrayList<>();

            // 获取指定数据库所有表
            Map<String, String> mapList = this.getTables(conn, dbName);

            List<TablePyhNameDTO> finalList = list;

            Iterator<Map.Entry<String, String>> iterator = mapList.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                // 根据表名获取字段
                List<TableStructureDTO> columnsName = getColumnsName(conn, entry.getKey());
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(entry.getValue() + "." + entry.getKey());
                tablePyhNameDTO.setFields(columnsName);
                finalList.add(tablePyhNameDTO);
            }
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            log.error("【getTableNameAndColumnsPlus】获取表名及表字段失败, ex", e);
            return null;
        }
        return list;
    }

}
