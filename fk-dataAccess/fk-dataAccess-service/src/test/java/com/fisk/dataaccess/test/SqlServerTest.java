package com.fisk.dataaccess.test;

import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.TablePyhNameDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.service.impl.AppDataSourceImpl;
import com.fisk.dataaccess.service.impl.AppRegistrationImpl;
import com.fisk.dataaccess.utils.MysqlConUtils;
import com.fisk.dataaccess.utils.SqlServerConUtils;
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
            tablePyhNameDTO.setFields(columnsName);

            tag++;
            tablePyhNameDTO.setTag(tag);
            list.add(tablePyhNameDTO);
        }

        System.out.println(list);
    }

    @Test
    public void testTableFields() {

        String appName1 = "NonRealTime";
        String appName2 = "zfh_sqlserver_app";

        // 1.根据应用名称查询表id
        AppRegistrationPO modelReg = appRegistrationImpl.query()
//                .eq("app_name", appName1)
                .eq("app_name", appName2)
                .eq("del_flag", 1)
                .one();

        // tb_app_registration表id
        long appid = modelReg.getId();

        // 2.根据app_id查询关联表tb_app_datasource的connect_str  connect_account  connect_pwd
        AppDataSourcePO modelDataSource = appDataSourceImpl.query().eq("appid", appid).one();
        String url = modelDataSource.getConnectStr();
        String user = modelDataSource.getConnectAccount();
        String pwd = modelDataSource.getConnectPwd();

        List<TablePyhNameDTO> list = new ArrayList<>();
        switch (modelDataSource.driveType) {
            case "mysql":
                // 3.调用MysqlConUtils,连接远程数据库,获取所有表及对应字段
                MysqlConUtils mysqlConUtils = new MysqlConUtils();
                list = mysqlConUtils.getTableNameAndColumns(url, user, pwd);
                break;
            case "sqlserver":
                list = new SqlServerConUtils().getTableNameAndColumns(url, user, pwd);
                break;
            default:
                break;
        }

        System.out.println(list);

    }

}
