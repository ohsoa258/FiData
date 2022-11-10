package com.fisk.dataaccess.test;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.dataaccess.service.impl.AppDataSourceImpl;
import com.fisk.dataaccess.service.impl.AppRegistrationImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Lock
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class SqlServerTest {

    @Resource
    private AppRegistrationImpl appRegistrationImpl;
    @Resource
    private AppDataSourceImpl appDataSourceImpl;

    //这里可以设置数据库名称
    private final static String URL = "jdbc:sqlserver://192.168.1.35:1433;DatabaseName=TestDB";
    private static final String USER = "sa";
    private static final String PASSWORD = "password01!";
    private static Connection conn = null;
    private static Statement stmt = null;


    /**
     * 加载驱动、连接数据库
     */
    @Before
    public void init() {
        try {
            //1.加载驱动程序
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            //2.获得数据库的连接
            conn = (Connection) DriverManager.getConnection(URL, USER, PASSWORD);
            stmt = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询获取所有数据库名
     */
    @Test
    public void findAllDatabases() {

        try {
            ResultSet resultSet = stmt.executeQuery("SELECT name FROM  master..sysdatabases WHERE name NOT IN ( 'master', 'model', 'msdb', 'tempdb', 'northwind','pubs' )");
            while (resultSet.next()) {//如果对象中有数据，就会循环打印出来
                System.out.println(resultSet.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询获取数据库所有表名
     */
//    @Test
//    public void getTables(){
    public List<String> getTables() {
        ArrayList<String> tableList = null;
        try {
            ResultSet resultSet = stmt.executeQuery("SELECT name FROM TestDB..sysobjects Where xtype='U' ORDER BY name");
            tableList = new ArrayList<>();
            while (resultSet.next()) {//如果对象中有数据，就会循环打印出来
                tableList.add(resultSet.getString("name"));
            }
            System.out.println(tableList);

//            resultSet.close();
//            stmt.close();
//            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableList;
    }


    /**
     * 查询获取数据库表的所有字段
     */
//    @Test
    public List<String> getColumnsName(String tableName) {
        List<String> colNameList = null;
        try {
            // SELECT * FROM syscolumns WHERE id=Object_Id('表名');
            ResultSet resultSet = stmt.executeQuery("SELECT name FROM syscolumns WHERE id=Object_Id('" + tableName + "');");

            colNameList = new ArrayList<>();

            while (resultSet.next()) {

                String name = resultSet.getString("name");
                colNameList.add(name);
            }
            System.out.println(colNameList);

            return colNameList;
        } catch (Exception e) {
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
    }

    @Test
    public void getTableNameAndColumnsByAppName() {
        List<TablePyhNameDTO> list = new ArrayList<>();

        // 获取指定数据库所有表
        List<String> tableNames = this.getTables();

        int tag = 0;
        for (String tableName : tableNames) {
            List<String> columnsName = getColumnsName(tableName);
            TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
            tablePyhNameDTO.setTableName(tableName);
//            tablePyhNameDTO.setFields(columnsName);

            tag++;
            //tablePyhNameDTO.setTag(tag);
            list.add(tablePyhNameDTO);
        }

        System.out.println(list);
    }

    @Test
    public  void  getView(){

        try {
            String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            String url = "jdbc:sqlserver://192.168.1.35:1433;DatabaseName=DWC_MDS;";
            String username = "sa";
            String password = "password01!";

            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, username, password);
            // 获取指定模式下所有表名和视图名
            ResultSet tables = conn.getMetaData().getTables(null, "mdm", null, new String[]{"VIEW"});
            while (tables.next()) {
                System.out.println(tables.getString(3));
                // 获取表及视图名称
//                System.out.println(tables.getString(1)+","+
//                        tables.getString(2)+","+
//                        tables.getString(3)+","+
//                        tables.getString(4)+","+
//                        tables.getString(5));
            }
            conn.close();
        } catch (Exception e) {

        }
    }

    @Test
    public void loadDataSourceMeta() {
        try {
            List<AppDataSourcePO> dataSourcePoList = appDataSourceImpl.query()
                    .eq("drive_type", DataSourceTypeEnum.MYSQL.getName())
                    .or()
                    .eq("drive_type", DataSourceTypeEnum.SQLSERVER.getName())
                    .or()
                    .eq("drive_type", DataSourceTypeEnum.ORACLE.getName())
                    .list();
        } catch (Exception e) {
            throw new FkException(ResultEnum.LOAD_DATASOURCE_META, e);
        }
    }
}
