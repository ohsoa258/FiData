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
 * 远程连接第三方数据库,创建第三方表
 */
@Component
public class MysqlTableUtils {

    /**
     * url
     */
    String url = "jdbc:mysql://192.168.11.130:3306/dmp_datainput_db?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";
    /**
     * user
     */
    String user = "root";
    /**
     * password
     */
    String pwd = "root123";

//    /**
//     * 创建表方法(mysql版本  实时)
//     *
//     * @param tableAccessDTO dto
//     * @throws SQLException 异常
//     * @throws ClassNotFoundException 异常
//     */
//    public int createmysqltb(TableAccessDTO tableAccessDTO) throws SQLException, ClassNotFoundException {
//
//        // 1.连接数据库
//        Connection conn = DriverManager.getConnection(url, user, pwd);
//        Statement stat = conn.createStatement();
//
//        List<TableFieldsDTO> list = tableAccessDTO.getList();
//
//        StringBuilder sb1 = new StringBuilder();
//        StringBuilder sb = new StringBuilder();
//        sb.append("CREATE TABLE `" + tableAccessDTO.getTableName() + "` (");
//        for (TableFieldsDTO dto : list) {
//            if (dto.getIsPrimarykey() == 1) {
//                sb1.append("PRIMARY KEY (`" + dto.getFieldName() + "`)");
//            }
//            sb.append(dto.getFieldName() + " " + dto.getFieldType() + " COMMENT '" + dto.getFieldDes() + "',");
//        }
//        sb.append(sb1.toString() + ");");
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
//     * 创建表方法(mysql版本  非实时)
//     * @param tableAccessNonDTO dto
//     * @return 返回值
//     * @throws SQLException 异常
//     * @throws ClassNotFoundException 异常
//     */
//    public int createmysqltb(TableAccessNonDTO tableAccessNonDTO) throws SQLException, ClassNotFoundException {
//
//        // 1.连接数据库
//        Connection conn = DriverManager.getConnection(url, user, pwd);
//        Statement stat = conn.createStatement();
//
//        List<TableFieldsDTO> list = tableAccessNonDTO.getList();
//
//        StringBuilder sb1 = new StringBuilder();
//        StringBuilder sb = new StringBuilder();
//        sb.append("CREATE TABLE `" + tableAccessNonDTO.getTableName() + "` (");
//        for (TableFieldsDTO dto : list) {
//            if (dto.getIsPrimarykey() == 1) {
//                sb1.append("PRIMARY KEY (`" + dto.getFieldName() + "`)");
//            }
//            sb.append(dto.getFieldName() + " " + dto.getFieldType() + " COMMENT '" + dto.getFieldDes() + "',");
//        }
//        sb.append(sb1.toString() + ");");
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
//
//    /**
//     * 更新表方法(mysql版本  实时)
//     *
//     * @param tableAccessDTO dto
//     * @throws SQLException 异常
//     */
//    public int updatemysqltb(TableAccessDTO tableAccessDTO) throws SQLException {
//
//        // 远程数据库
//        String url1 = "jdbc:mysql://192.168.11.130:3306/dmp_chartvisual_db?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";
//        String user1 = "root";
//        String pwd1 = "root123";
//        Connection conn1 = DriverManager.getConnection(url, user, pwd);
//        Statement stat1 = conn1.createStatement();
//
//        // 1.连接数据库
//        Connection conn = DriverManager.getConnection(url, user, pwd);
//        Statement stat = conn.createStatement();
//
//        List<TableFieldsDTO> list = tableAccessDTO.getList();
//
////        StringBuilder sb_PRIMARYKEY = new StringBuilder();
//        StringBuilder sb = new StringBuilder();
//
//        // 0:新增字段   1:修改字段
//        // 新增字段
//        List<TableFieldsDTO> tb0 = new ArrayList<>();
//        // 修改字段
//        List<TableFieldsDTO> tb1 = new ArrayList<>();
//
//        for (TableFieldsDTO tableFieldsDTO : list) {
//            if (tableFieldsDTO.getFuncType() == 0) {
//                tb0.add(tableFieldsDTO);
//            } else if (tableFieldsDTO.getFuncType() == 1) {
//                tb1.add(tableFieldsDTO);
//            }
//        }
//
//        int s = -2;
//
//        // 只有新增字段
//        // ALTER TABLE input_test1 ADD b1 varchar(15) COMMENT '电话',ADD b2 varchar(15) COMMENT '地址',
//        if (!tb0.isEmpty() && tb1.isEmpty()) {
//            sb.append("ALTER TABLE `" + tableAccessDTO.getTableName() + "` add(");
//            for (TableFieldsDTO dto : tb0) {
//
//                sb.append(dto.getFieldName() + " " + dto.getFieldType() + " COMMENT '" + dto.getFieldDes() + "',");
//            }
//
//            if (sb.length() > 0) {
//                // 去掉最后的逗号
//                sb.deleteCharAt(sb.length() - 1);
//            }
//            sb.append(");");
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
//            String name = null;
//
//            sb.append("ALTER TABLE `" + tableAccessDTO.getTableName() + "` CHANGE ");
//            for (TableFieldsDTO dto : tb1) {
//
//
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
//                sb.deleteCharAt(sb.length() - 1);
//            }
//            sb.append(";");
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
//                // 去掉最后的逗号
//                sb.deleteCharAt(sb.length() - 1);
//            }
//            sb.append(");");
//
//            // 生成的SQL语句
//            System.out.println(sb.toString());
//            s = stat.executeUpdate(sb.toString());
//        }
//
//        // 释放资源
//        stat.close();
//        conn.close();
//        return s;
//    }
//
//
//    /**
//     * 更新表方法(mysql版本  非实时)
//     *
//     * @param tableAccessNonDTO dto
//     * @throws SQLException 异常
//     */
//    public int updatemysqltb(TableAccessNonDTO tableAccessNonDTO) throws SQLException {
//
//        // 远程数据库
//        String url1 = "jdbc:mysql://192.168.11.130:3306/dmp_chartvisual_db?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";
//        String user1 = "root";
//        String pwd1 = "root123";
//        Connection conn1 = DriverManager.getConnection(url, user, pwd);
//        Statement stat1 = conn1.createStatement();
//
//        // 1.连接数据库
//        Connection conn = DriverManager.getConnection(url, user, pwd);
//        Statement stat = conn.createStatement();
//
//        List<TableFieldsDTO> list = tableAccessNonDTO.getList();
//
//
//        StringBuilder sb = new StringBuilder();
//
//        // 新增字段
//        List<TableFieldsDTO> tb0 = new ArrayList<>();
//        // 修改字段
//        List<TableFieldsDTO> tb1 = new ArrayList<>();
//
//        for (TableFieldsDTO tableFieldsDTO : list) {
//            if (tableFieldsDTO.getFuncType() == 0) {
//                tb0.add(tableFieldsDTO);
//            } else if (tableFieldsDTO.getFuncType() == 1) {
//                tb1.add(tableFieldsDTO);
//            }
//        }
//
//        int s = -2;
//
//        // 只有新增字段
//        // ALTER TABLE input_test1 ADD b1 varchar(15) COMMENT '电话',ADD b2 varchar(15) COMMENT '地址',
//        if (!tb0.isEmpty() && tb1.isEmpty()) {
//            sb.append("ALTER TABLE `" + tableAccessNonDTO.getTableName() + "` add(");
//            for (TableFieldsDTO dto : tb0) {
//
//                sb.append(dto.getFieldName() + " " + dto.getFieldType() + " COMMENT '" + dto.getFieldDes() + "',");
//            }
//
//            if (sb.length() > 0) {
//                sb.deleteCharAt(sb.length() - 1);
//            }
//            sb.append(");");
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
//            String name = null;
//
//            sb.append("ALTER TABLE `" + tableAccessNonDTO.getTableName() + "` CHANGE ");
//            for (TableFieldsDTO dto : tb1) {
//
//
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
//                // 去掉最后的逗号
//                sb.deleteCharAt(sb.length() - 1);
//            }
//            sb.append(";");
//
//            // 生成的SQL语句
//            System.out.println(sb.toString());
//            s = stat.executeUpdate(sb.toString());
//        } else if (!tb0.isEmpty() && !tb1.isEmpty()) {
//
//            String name = null;
//
//            sb.append("ALTER TABLE `" + tableAccessNonDTO.getTableName() + "` add(");
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
//                // 去掉最后的逗号
//                sb.deleteCharAt(sb.length() - 1);
//            }
//            sb.append(");");
//
//            // 生成的SQL语句
//            System.out.println(sb.toString());
//            s = stat.executeUpdate(sb.toString());
//
//        }
//
//        // 释放资源
//        stat.close();
//        conn.close();
//        return s;
//    }

}
