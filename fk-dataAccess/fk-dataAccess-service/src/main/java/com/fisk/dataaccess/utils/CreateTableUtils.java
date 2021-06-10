package com.fisk.dataaccess.utils;

import com.fisk.dataaccess.dto.TableAccessDTO;
import com.fisk.dataaccess.dto.TableFieldsDTO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author: Lock
 *
 * 远程连接第三方数据库,创建第三方表
 */
public class CreateTableUtils {

    /**
     * 创建表方法(mysql版本)
     *
     * @param tableAccessDTO
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public int createMysqlTB(TableAccessDTO tableAccessDTO) throws SQLException, ClassNotFoundException {

        // 要拉取到本地的服务器地址
        String url = "jdbc:mysql://192.168.206.99:3306/fisk?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";
        String user = "root";
        String pwd = "root";

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
            sb.append(dto.getFieldName()+" " + dto.getFieldType() +" COMMENT '" + dto.getFieldDes() + "',");
        }
        sb.append(sb_PRIMARYKEY.toString() + ");");

        // 生成的SQL语句
//        System.out.println(sb.toString());
        int i = stat.executeUpdate(sb.toString());

        // 释放资源
        stat.close();
        conn.close();
        return i;
    }

}
