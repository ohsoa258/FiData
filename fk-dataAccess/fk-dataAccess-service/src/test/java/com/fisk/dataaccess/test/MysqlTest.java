package com.fisk.dataaccess.test;

import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Lock
 * <p>
 * JDBC获取所有表及获取表所有字段
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class MysqlTest {

    @Test
    public void test() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
//        Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.206.99:3306/fisk", "root", "root");
        Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.11.130:3306/dmp_datainput_db", "root", "root123");
        List<String> tableNames = getTables(conn);
//        System.out.println(tableNames);
        System.out.println("====================================================");
        Statement st = conn.createStatement();

        Map<String, List<TableStructureDTO>> map = new HashMap<>();
//        for (String tableName : tableNames) {
        ResultSet rs = st.executeQuery("select * from tb_table_syncmode");
//            System.out.print(tableName+":  ");
        List<TableStructureDTO> colNames = getColNames(rs);
//            System.out.println(colNames);

//            System.out.println("==================================================");

        map.put("tb_table_syncmode", colNames);
//            System.out.println(map);
        rs.close();
//        }
        System.out.println(map);

//        ResultSet rs = st.executeQuery("select * from tb_table_access");
//        /*List<String> colNames = */getColNames(rs);

//        System.out.println(colNames);
//        while(rs.next()){
//            for (int i = 0; i < colNames.size(); i++) {
//                System.out.print(rs.getObject(colNames.get(i)));
//                if(i!=colNames.size()-1)
//                    System.out.print("\t");
//            }
//            System.out.println();
//        }
//        rs.close();
        st.close();
        conn.close();
    }

    /**
     * 获取数据库中所有表名称
     *
     * @param conn
     * @return
     * @throws SQLException
     */
    private List<String> getTables(Connection conn) throws SQLException {
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        ResultSet tables = databaseMetaData.getTables(null, null, "%", null);
        ArrayList<String> tablesList = new ArrayList<String>();
        while (tables.next()) {
            tablesList.add(tables.getString("TABLE_NAME"));
        }
        return tablesList;
    }

    /**
     * 获取表中所有字段名称
     *
     * @param rs
     * @throws SQLException
     */
    private List<TableStructureDTO> getColNames(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int count = metaData.getColumnCount();
/*
        System.out.println("getCatalogName(int column) 获取指定列的表目录名称。"+metaData.getCatalogName(1));
//        System.out.println("getColumnClassName(int column) 构造其实例的 Java 类的完全限定名称。"+metaData.getColumnClassName(1));
        System.out.println("getColumnCount()  返回此 ResultSet 对象中的列数。"+metaData.getColumnCount());
//        System.out.println("getColumnDisplaySize(int column) 指示指定列的最大标准宽度，以字符为单位. "+metaData.getColumnDisplaySize(1));
//        System.out.println("getColumnLabel(int column) 获取用于打印输出和显示的指定列的建议标题。 "+metaData.getColumnLabel(1));
        System.out.println("getColumnName(int column)  获取指定列的名称。"+metaData.getColumnName(1));
//        System.out.println("getColumnType(int column) 获取指定列的 SQL 类型。 "+metaData.getColumnType(1));
        System.out.println("getColumnTypeName(int column) 获取指定列的数据库特定的类型名称。 "+metaData.getColumnTypeName(1));
//        System.out.println("getPrecision(int column)  获取指定列的指定列宽。 "+metaData.getPrecision(1));
//        System.out.println("getScale(int column) 获取指定列的小数点右边的位数。 "+metaData.getScale(1));
//        System.out.println("getSchemaName(int column) 获取指定列的表模式。 "+metaData.getSchemaName(1));
        System.out.println("getTableName(int column) 获取指定列的名称。 "+metaData.getTableName(1));
*/
        while (rs.next()) {

            String comment = rs.getString("name");
            System.out.println("测试注释" + comment);
        }

        List<TableStructureDTO> colNameList = new ArrayList<>();
        for (int i = 1; i <= count; i++) {

            TableStructureDTO tableStructureDTO = new TableStructureDTO();

            tableStructureDTO.fieldName = metaData.getColumnName(i);
            tableStructureDTO.fieldType = metaData.getColumnTypeName(i);
            tableStructureDTO.fieldLength = metaData.getColumnDisplaySize(i);

//            colNameList.add(metaData.getColumnName(i));
            colNameList.add(tableStructureDTO);

//            System.out.println("字段名称" + metaData.getColumnName(i));
            // 与原表的结构可能不一致
//            System.out.println("字段类型" + metaData.getColumnTypeName(i));
            // 列的正常最大宽度(与原表的结构可能不一致)
//            System.out.println("字段长度" + metaData.getColumnDisplaySize(i));
            System.out.println("---------------");
        }
        // 打印
//        System.out.println(colNameList);
//		rs.close();
        rs.first();
        return colNameList;
    }

}
