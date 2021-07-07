package com.fisk.dataaccess.utils;

import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.TableAccessDTO;
import com.fisk.dataaccess.dto.TableAccessNonDTO;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lock
 * <p>
 * SqlServer版
 */
@Component
public class SqlServerTableUtils {

    //这里可以设置远程数据库名称
//    private final static String URL = "jdbc:sqlserver://192.168.1.35:1433;databaseName=studb";
//    private static final String USER="sa";
//    private static final String PASSWORD="password01!";
////    private static Connection conn= null;
////    private static Statement stmt = null;
//
//    /**
//     * 创建表方法(SQL server版本  实时)
//     *
//     * @param tableAccessDTO
//     * @throws SQLException
//     * @throws ClassNotFoundException
//     */
//    public int createSqlServerTB(TableAccessDTO tableAccessDTO) throws SQLException, ClassNotFoundException {
//
//        // 将表创建到的数据库地址
//        String url = "jdbc:sqlserver://192.168.1.35:1433;DataBase=TestDB";
//        String user = "sa";
//        String pwd = "password01!";
//
//        // 1.连接数据库
//        Connection conn = DriverManager.getConnection(url, user, pwd);
//        Statement stat = conn.createStatement();
//
//        List<TableFieldsDTO> tableFieldsDTOS = tableAccessDTO.getList();
//
//        StringBuilder sb_PRIMARYKEY = new StringBuilder();
//        StringBuilder sb = new StringBuilder();
//        sb.append("CREATE TABLE [" + tableAccessDTO.getTableName() + "](");
//        for (TableFieldsDTO dto : tableFieldsDTOS) {
//            if (dto.getIsPrimarykey() == 1) {
//                sb_PRIMARYKEY.append("PRIMARY KEY(" + dto.getFieldName() + ")");
//            }
//            sb.append(dto.getFieldName() + " " + dto.getFieldType()/* + " COMMENT '" + dto.getFieldDes() */+ ",");
//        }
//        sb.append(sb_PRIMARYKEY.toString() + ");");
//
//        // 给字段添加注释
//        for (TableFieldsDTO dto : tableFieldsDTOS) {
//
//            // execute sp_addextendedproperty 'MS_Description','字段备注信息','user','dbo','table','字段所属的表名','column','添加注释的字段名';  
//            sb.append("execute sp_addextendedproperty 'MS_Description','"+dto.getFieldDes()+"','user','dbo','table'," +
//                    "'"+tableAccessDTO.getTableName()+"','column','"+dto.getFieldName()+"';");
//        }
//
//        // 生成的SQL语句
//        System.out.println(sb.toString());
//        int i = stat.executeUpdate(sb.toString());
//
//        // 释放资源
//        stat.close();
//        conn.close();
//        return i;
//    }
//
//    /**
//     * 创建表方法(SQL server版本  非实时)
//     *
//     * @param tableAccessNonDTO
//     * @throws SQLException
//     * @throws ClassNotFoundException
//     */
//    public int createSqlServerTB(TableAccessNonDTO tableAccessNonDTO) throws SQLException, ClassNotFoundException {
//
//        // 将表创建到的数据库地址
//        String user = "sa";
//        String pwd = "password01!";
//        String url = "jdbc:sqlserver://192.168.1.35:1433;DataBase=TestDB";
//
//        // 1.连接数据库
//        Connection conn = DriverManager.getConnection(url, user, pwd);
//        Statement stat = conn.createStatement();
//
//        List<TableFieldsDTO> tableFieldsDTOS = tableAccessNonDTO.getList();
//
//        StringBuilder sb_PRIMARYKEY = new StringBuilder();
//        StringBuilder sb = new StringBuilder();
//        sb.append("CREATE TABLE [" + tableAccessNonDTO.getTableName() + "](");
//        for (TableFieldsDTO dto : tableFieldsDTOS) {
//            if (dto.getIsPrimarykey() == 1) {
//                sb_PRIMARYKEY.append("PRIMARY KEY(" + dto.getFieldName() + ")");
//            }
//            sb.append(dto.getFieldName() + " " + dto.getFieldType()/* + " COMMENT '" + dto.getFieldDes() */+ ",");
//        }
//        sb.append(sb_PRIMARYKEY.toString() + ");");
//
//        // 给字段添加注释
//        for (TableFieldsDTO dto : tableFieldsDTOS) {
//
//            // execute sp_addextendedproperty 'MS_Description','字段备注信息','user','dbo','table','字段所属的表名','column','添加注释的字段名';  
//            sb.append("execute sp_addextendedproperty 'MS_Description','"+dto.getFieldDes()+"','user','dbo','table'," +
//                    "'"+ tableAccessNonDTO.getTableName()+"','column','"+dto.getFieldName()+"';");
//        }
//
//        // 生成的SQL语句
//        System.out.println(sb.toString());
//        int i = stat.executeUpdate(sb.toString());
//
//        // 释放资源
//        stat.close();
//        conn.close();
//        return i;
//    }
//
//    /**
//     * 更新表方法(SQL Server版本  实时)
//     *
//     * @param tableAccessDTO
//     * @throws SQLException
//     * @throws ClassNotFoundException
//     */
//    public int updateSqlServerTB(TableAccessDTO tableAccessDTO) throws SQLException, ClassNotFoundException {
//
//        // 修改表的数据库地址
//        String user = "sa";
//        String pwd = "password01!";
//        String url = "jdbc:sqlserver://192.168.1.35:1433;DataBase=TestDB";
//
//        // 1.连接数据库
//        Connection conn = DriverManager.getConnection(url, user, pwd);
//        Statement stat = conn.createStatement();
//
//        List<TableFieldsDTO> tableFieldsDTOS = tableAccessDTO.getList();
//
//        // 封装的是sql语句
//        StringBuilder sb = new StringBuilder();
//
//        /**
//         * 两种方式:
//         *  0:新增字段   1:修改字段
//         */
//        List<TableFieldsDTO> tb0 = new ArrayList<>(); // 新增字段
//        List<TableFieldsDTO> tb1 = new ArrayList<>(); // 修改字段
//
//        for (TableFieldsDTO tableFieldsDTO : tableFieldsDTOS) {
//            if (tableFieldsDTO.getFuncType() == 0) {
//                tb0.add(tableFieldsDTO);
//            } else if (tableFieldsDTO.getFuncType() == 1) {
//                tb1.add(tableFieldsDTO);
//            }
//        }
//
//        // 初始化执行完sql语句的返回值
//        int s = -2;
//
//        // 只有新增字段
//        if (!tb0.isEmpty() && tb1.isEmpty()) {
//
//            for (TableFieldsDTO dto : tb0) {
//                // 添加某一列
//                // Alter table [表名] add [列名] 类型
//                sb.append("Alter table [" + tableAccessDTO.getTableName() + "] add [" + dto.getFieldName() + "] " + dto.getFieldType() + ";");
//                // 给列添加字段
//                // execute sp_addextendedproperty 'MS_Description','字段备注信息','user','dbo','table','字段所属的表名','column','添加注释的字段名'; 
//                sb.append("execute sp_addextendedproperty 'MS_Description','"+dto.getFieldDes()+"','user','dbo','table'," +
//                        "'"+tableAccessDTO.getTableName()+"','column','"+dto.getFieldName()+"';");
//            }
//
//            // 生成的SQL语句
//            System.out.println(sb.toString());
//            s = stat.executeUpdate(sb.toString());
//        }
//
//        // 只有修改
//        // ALTER TABLE 表名 CHANGE  旧字段 新字段 INT COMMENT '注释',CHANGE tel tell VARCHAR(15) comment 'dianhua';
//        else if (tb0.isEmpty() && !tb1.isEmpty()) {
//
//            for (TableFieldsDTO dto : tb1) {
//
//                // 修改字段名
//                // 格式: EXEC sp_rename '表名.[旧列名]','新列名','COLUMN';
//
//                // TODO: 创建表没问题了,暂时是新增表字段(字段名  字段类型  注释)和修改表子字段待完善
//                // TODO: 旧列名获取不到,要更改[当时的想法,前端携带tb_table_fields表的id,根据id查询旧字段,当然了,我回显的时候,也将id携带给前端]
////                sb.append("EXEC sp_rename '"+tableAccessDTO.getTableName()+".["+dto.getFieldName()+"]','"+dto.getFieldName()+"','COLUMN';");
//
//            }
//
//            // 生成的SQL语句
//            System.out.println(sb.toString());
//            s = stat.executeUpdate(sb.toString());
//        } else if (!tb0.isEmpty() && !tb1.isEmpty()) {
//
//            String name = null;
//
//            sb.append("ALTER TABLE `" + tableAccessDTO.getTableName() + "` add(");
//            for (TableFieldsDTO dto : tb0) {
//
//                sb.append(dto.getFieldName() + " " + dto.getFieldType() + " COMMENT '" + dto.getFieldDes() + "',");
//            }
//
//
//
//            for (TableFieldsDTO dto : tb1) {
//                sb.append("CHANGE ");
//                // 要查询旧字段
////                TableFieldsPO fieldsPO = impl.query().eq("table_access_id", dto.getTableAccessId()).one();
//                ResultSet rs = stat.executeQuery("select field_name from tb_table_fields where table_access_id = " + dto.getTableAccessId() + ";");
//
//                while (rs.next()) {
//                    name = rs.getString(1);
//                }
//                if (name == null) {
//                    throw new FkException(ResultEnum.DATA_NOTEXISTS);
//                }
//                System.out.println(name);
//
//                sb.append(name + " " + dto.getFieldName() + " " + dto.getFieldType() + " COMMENT '" + dto.getFieldDes() + "',");
//
//                // 清空
//                name = null;
//            }
//
//            if (sb.length() > 0) {
//                sb.deleteCharAt(sb.length() - 1);// 去掉最后的逗号
//            }
//            sb.append(");");
//
//            // 生成的SQL语句
//            System.out.println(sb.toString());
//            s = stat.executeUpdate(sb.toString());
//
//
//        }
//
//        // 释放资源
//        stat.close();
//        conn.close();
//        return s;
//    }

}
