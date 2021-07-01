package com.fisk.dataaccess.utils;

import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.TableAccessDTO;
import com.fisk.dataaccess.dto.TableAccessNDTO;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.dataaccess.entity.TableFieldsPO;
import com.fisk.dataaccess.service.impl.TableFieldsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Lock
 * <p>
 * 远程连接第三方数据库,创建第三方表
 */
@Component
public class MysqlTableUtils {

    // 创建的表都是放在dmp_datainput_db数据库下
    String url = "jdbc:mysql://192.168.11.130:3306/dmp_datainput_db?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";
    String user = "root";
    String pwd = "root123";

    /**
     * 创建表方法(mysql版本  实时)
     *
     * @param tableAccessDTO
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public int createMysqlTB(TableAccessDTO tableAccessDTO) throws SQLException, ClassNotFoundException {

        // 要拉取到本地的服务器地址
/*        String url = "jdbc:mysql://192.168.11.130:3306/dmp_datainput_db?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";
        String user = "root";
        String pwd = "root123";*/

        // 1.连接数据库
        Connection conn = DriverManager.getConnection(url, user, pwd);
        Statement stat = conn.createStatement();

        // 获取数据库名
/*        ResultSet rs = conn.getMetaData().getTables(null, null, "input_table_access", null);
        // 判断表是否存在，如果存在则什么都不做，否则创建表
        if (rs.next()) {
            System.out.println("数据库已存在");
            return;
        }*/

        List<TableFieldsDTO> tableFieldsDTOS = tableAccessDTO.getTableFieldsDTOS();

        StringBuilder sb_PRIMARYKEY = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE `" + tableAccessDTO.getTableName() + "` (");
        for (TableFieldsDTO dto : tableFieldsDTOS) {
            if (dto.getIsPrimarykey() == 1) {
                sb_PRIMARYKEY.append("PRIMARY KEY (`" + dto.getFieldName() + "`)");
            }
            sb.append(dto.getFieldName() + " " + dto.getFieldType() + " COMMENT '" + dto.getFieldDes() + "',");
        }
        sb.append(sb_PRIMARYKEY.toString() + ");");

        // 生成的SQL语句
        System.out.println(sb.toString());
        int i = stat.executeUpdate(sb.toString());

        // 释放资源
        stat.close();
        conn.close();
        return i;
    }

    /**
     * 创建表方法(mysql版本  非实时)
     * @param tableAccessNDTO
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public int createMysqlTB(TableAccessNDTO tableAccessNDTO) throws SQLException, ClassNotFoundException {

        // 要拉取到本地的服务器地址
/*        String url = "jdbc:mysql://192.168.11.130:3306/dmp_datainput_db?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";
        String user = "root";
        String pwd = "root123";*/

        // 1.连接数据库
        Connection conn = DriverManager.getConnection(url, user, pwd);
        Statement stat = conn.createStatement();

        // 获取数据库名
/*        ResultSet rs = conn.getMetaData().getTables(null, null, "input_table_access", null);
        // 判断表是否存在，如果存在则什么都不做，否则创建表
        if (rs.next()) {
            System.out.println("数据库已存在");
            return;
        }*/

        List<TableFieldsDTO> tableFieldsDTOS = tableAccessNDTO.getTableFieldsDTOS();

        StringBuilder sb_PRIMARYKEY = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE `" + tableAccessNDTO.getTableName() + "` (");
        for (TableFieldsDTO dto : tableFieldsDTOS) {
            if (dto.getIsPrimarykey() == 1) {
                sb_PRIMARYKEY.append("PRIMARY KEY (`" + dto.getFieldName() + "`)");
            }
            sb.append(dto.getFieldName() + " " + dto.getFieldType() + " COMMENT '" + dto.getFieldDes() + "',");
        }
        sb.append(sb_PRIMARYKEY.toString() + ");");

        // 生成的SQL语句
        System.out.println(sb.toString());
        int i = stat.executeUpdate(sb.toString());

        // 释放资源
        stat.close();
        conn.close();
        return i;
    }


    /**
     * 更新表方法(mysql版本  实时)
     *
     * @param tableAccessDTO
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public int updateMysqlTB(TableAccessDTO tableAccessDTO) throws SQLException, ClassNotFoundException {

        // 远程数据库
        String url1 = "jdbc:mysql://192.168.11.130:3306/dmp_chartvisual_db?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";
        String user1 = "root";
        String pwd1 = "root123";
        Connection conn1 = DriverManager.getConnection(url, user, pwd);
        Statement stat1 = conn1.createStatement();

        // 1.连接数据库
        Connection conn = DriverManager.getConnection(url, user, pwd);
        Statement stat = conn.createStatement();

        List<TableFieldsDTO> tableFieldsDTOS = tableAccessDTO.getTableFieldsDTOS();

//        StringBuilder sb_PRIMARYKEY = new StringBuilder();
        StringBuilder sb = new StringBuilder();

        /**
         * 两种方式:
         *  0:新增字段   1:修改字段
         */
        List<TableFieldsDTO> tb0 = new ArrayList<>(); // 新增字段
        List<TableFieldsDTO> tb1 = new ArrayList<>(); // 修改字段

        for (TableFieldsDTO tableFieldsDTO : tableFieldsDTOS) {
            if (tableFieldsDTO.getFuncType() == 0) {
                tb0.add(tableFieldsDTO);
            } else if (tableFieldsDTO.getFuncType() == 1) {
                tb1.add(tableFieldsDTO);
            }
        }

        int s = -2;

        // 只有新增字段
        // ALTER TABLE input_test1 ADD b1 varchar(15) COMMENT '电话',ADD b2 varchar(15) COMMENT '地址',
        if (!tb0.isEmpty() && tb1.isEmpty()) {
            sb.append("ALTER TABLE `" + tableAccessDTO.getTableName() + "` add(");
            for (TableFieldsDTO dto : tb0) {

                sb.append(dto.getFieldName() + " " + dto.getFieldType() + " COMMENT '" + dto.getFieldDes() + "',");
            }

            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);// 去掉最后的逗号
            }
            sb.append(");");

            // 生成的SQL语句
            System.out.println(sb.toString());
            s = stat.executeUpdate(sb.toString());
        }

        // 只有修改
        // ALTER TABLE 表名 CHANGE  旧字段 新字段 INT COMMENT '注释',CHANGE tel tell VARCHAR(15) comment 'dianhua';
        else if (tb0.isEmpty() && !tb1.isEmpty()) {

            String name = null;

            sb.append("ALTER TABLE `" + tableAccessDTO.getTableName() + "` CHANGE ");
            for (TableFieldsDTO dto : tb1) {


                // 要查询旧字段
//                TableFieldsPO fieldsPO = impl.query().eq("table_access_id", dto.getTableAccessId()).one();
                ResultSet rs = stat.executeQuery("select field_name from tb_table_fields where table_access_id = " + dto.getTableAccessId() + ";");

                while (rs.next()) {
                    name = rs.getString(1);
                }
                if (name == null) {
                    throw new FkException(ResultEnum.DATA_NOTEXISTS);
                }
                System.out.println(name);

                sb.append(name + " " + dto.getFieldName() + " " + dto.getFieldType() + " COMMENT '" + dto.getFieldDes() + "',");

                // 清空
                name = null;
            }

            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);// 去掉最后的逗号
            }
            sb.append(";");

            // 生成的SQL语句
            System.out.println(sb.toString());
            s = stat.executeUpdate(sb.toString());
        } else if (!tb0.isEmpty() && !tb1.isEmpty()) {

            String name = null;

            sb.append("ALTER TABLE `" + tableAccessDTO.getTableName() + "` add(");
            for (TableFieldsDTO dto : tb0) {

                sb.append(dto.getFieldName() + " " + dto.getFieldType() + " COMMENT '" + dto.getFieldDes() + "',");
            }



            for (TableFieldsDTO dto : tb1) {
                sb.append("CHANGE ");
                // 要查询旧字段
//                TableFieldsPO fieldsPO = impl.query().eq("table_access_id", dto.getTableAccessId()).one();
                ResultSet rs = stat.executeQuery("select field_name from tb_table_fields where table_access_id = " + dto.getTableAccessId() + ";");

                while (rs.next()) {
                    name = rs.getString(1);
                }
                if (name == null) {
                    throw new FkException(ResultEnum.DATA_NOTEXISTS);
                }
                System.out.println(name);

                sb.append(name + " " + dto.getFieldName() + " " + dto.getFieldType() + " COMMENT '" + dto.getFieldDes() + "',");

                // 清空
                name = null;
            }

            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);// 去掉最后的逗号
            }
            sb.append(");");

            // 生成的SQL语句
            System.out.println(sb.toString());
            s = stat.executeUpdate(sb.toString());


        }












        /*sb.append("ALTER TABLE `" + tableAccessDTO.getTableName() + "` add(");


        for (TableFieldsDTO dto : tableFieldsDTOS) {
            *//*if (dto.getIsPrimarykey() == 1) {
                sb_PRIMARYKEY.append("PRIMARY KEY (`" + dto.getFieldName() + "`)");
            }*//*
            sb.append(dto.getFieldName()+" " + dto.getFieldType() +" COMMENT '" + dto.getFieldDes() + "',");
        }
//        sb.append(sb_PRIMARYKEY.toString() + ");");
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);// 去掉最后的逗号
        }
        sb.append(");");

        // 生成的SQL语句
        System.out.println(sb.toString());
        int i = stat.executeUpdate(sb.toString());*/

        // 释放资源
        stat.close();
        conn.close();
        return s;
    }


    /**
     * 更新表方法(mysql版本  非实时)
     *
     * @param tableAccessNDTO
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public int updateMysqlTB(TableAccessNDTO tableAccessNDTO) throws SQLException, ClassNotFoundException {

        // 远程数据库
        String url1 = "jdbc:mysql://192.168.11.130:3306/dmp_chartvisual_db?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";
        String user1 = "root";
        String pwd1 = "root123";
        Connection conn1 = DriverManager.getConnection(url, user, pwd);
        Statement stat1 = conn1.createStatement();

        // 1.连接数据库
        Connection conn = DriverManager.getConnection(url, user, pwd);
        Statement stat = conn.createStatement();

        List<TableFieldsDTO> tableFieldsDTOS = tableAccessNDTO.getTableFieldsDTOS();

//        StringBuilder sb_PRIMARYKEY = new StringBuilder();
        StringBuilder sb = new StringBuilder();

        /**
         * 两种方式:
         *  0:新增字段   1:修改字段
         */
        List<TableFieldsDTO> tb0 = new ArrayList<>(); // 新增字段
        List<TableFieldsDTO> tb1 = new ArrayList<>(); // 修改字段

        for (TableFieldsDTO tableFieldsDTO : tableFieldsDTOS) {
            if (tableFieldsDTO.getFuncType() == 0) {
                tb0.add(tableFieldsDTO);
            } else if (tableFieldsDTO.getFuncType() == 1) {
                tb1.add(tableFieldsDTO);
            }
        }

        int s = -2;

        // 只有新增字段
        // ALTER TABLE input_test1 ADD b1 varchar(15) COMMENT '电话',ADD b2 varchar(15) COMMENT '地址',
        if (!tb0.isEmpty() && tb1.isEmpty()) {
            sb.append("ALTER TABLE `" + tableAccessNDTO.getTableName() + "` add(");
            for (TableFieldsDTO dto : tb0) {

                sb.append(dto.getFieldName() + " " + dto.getFieldType() + " COMMENT '" + dto.getFieldDes() + "',");
            }

            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);// 去掉最后的逗号
            }
            sb.append(");");

            // 生成的SQL语句
            System.out.println(sb.toString());
            s = stat.executeUpdate(sb.toString());
        }

        // 只有修改
        // ALTER TABLE 表名 CHANGE  旧字段 新字段 INT COMMENT '注释',CHANGE tel tell VARCHAR(15) comment 'dianhua';
        else if (tb0.isEmpty() && !tb1.isEmpty()) {

            String name = null;

            sb.append("ALTER TABLE `" + tableAccessNDTO.getTableName() + "` CHANGE ");
            for (TableFieldsDTO dto : tb1) {


                // 要查询旧字段
//                TableFieldsPO fieldsPO = impl.query().eq("table_access_id", dto.getTableAccessId()).one();
                ResultSet rs = stat.executeQuery("select field_name from tb_table_fields where table_access_id = " + dto.getTableAccessId() + ";");

                while (rs.next()) {
                    name = rs.getString(1);
                }
                if (name == null) {
                    throw new FkException(ResultEnum.DATA_NOTEXISTS);
                }
                System.out.println(name);

                sb.append(name + " " + dto.getFieldName() + " " + dto.getFieldType() + " COMMENT '" + dto.getFieldDes() + "',");

                // 清空
                name = null;
            }

            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);// 去掉最后的逗号
            }
            sb.append(";");

            // 生成的SQL语句
            System.out.println(sb.toString());
            s = stat.executeUpdate(sb.toString());
        } else if (!tb0.isEmpty() && !tb1.isEmpty()) {

            String name = null;

            sb.append("ALTER TABLE `" + tableAccessNDTO.getTableName() + "` add(");
            for (TableFieldsDTO dto : tb0) {

                sb.append(dto.getFieldName() + " " + dto.getFieldType() + " COMMENT '" + dto.getFieldDes() + "',");
            }



            for (TableFieldsDTO dto : tb1) {
                sb.append("CHANGE ");
                // 要查询旧字段
//                TableFieldsPO fieldsPO = impl.query().eq("table_access_id", dto.getTableAccessId()).one();
                ResultSet rs = stat.executeQuery("select field_name from tb_table_fields where table_access_id = " + dto.getTableAccessId() + ";");

                while (rs.next()) {
                    name = rs.getString(1);
                }
                if (name == null) {
                    throw new FkException(ResultEnum.DATA_NOTEXISTS);
                }
                System.out.println(name);

                sb.append(name + " " + dto.getFieldName() + " " + dto.getFieldType() + " COMMENT '" + dto.getFieldDes() + "',");

                // 清空
                name = null;
            }

            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);// 去掉最后的逗号
            }
            sb.append(");");

            // 生成的SQL语句
            System.out.println(sb.toString());
            s = stat.executeUpdate(sb.toString());


        }

        // 释放资源
        stat.close();
        conn.close();
        return s;
    }



}
