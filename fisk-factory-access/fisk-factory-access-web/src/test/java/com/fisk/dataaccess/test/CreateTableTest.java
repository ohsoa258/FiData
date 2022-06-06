package com.fisk.dataaccess.test;

import com.fisk.dataaccess.dto.table.TableAccessDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author: Lock
 * <p>
 * 测试连接数据库: 创建表及表字段
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class CreateTableTest {

    @Test
    public void test(TableAccessDTO tableAccessDTO) throws SQLException, ClassNotFoundException {
        String url = "jdbc:mysql://192.168.206.99:3306/fisk?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";
        String user = "root";
        String pwd = "root";

/*        TableAccessDTO tableAccessDTO = new TableAccessDTO();
        tableAccessDTO.setTableName("test");
        tableAccessDTO.setSyncSrc("jdbc:mysql://192.168.206.99:3306/fisk?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false");
        tableAccessDTO.setId(3);
        tableAccessDTO.setTableDes("物理表描述");

        List<TableFieldsDTO> tableFieldsDTOList = new ArrayList<>();
*//*        for (TableFieldsDTO tableFieldsDTO : tableFieldsDTOList) {
            tableFieldsDTO.setFieldName("id");
            tableFieldsDTO.setFieldType("int");
            tableFieldsDTO.setFieldDes("主键");
            tableFieldsDTO.setIsPrimarykey(1);
        }*//*
        TableFieldsDTO tableFieldsDTO1 = new TableFieldsDTO();
        tableFieldsDTO1.setFieldName("id");
        tableFieldsDTO1.setFieldType("int");
        tableFieldsDTO1.setFieldDes("主键");
        tableFieldsDTO1.setIsPrimarykey(1);
        for (TableFieldsDTO tableFieldsDTO : tableFieldsDTOList) {
            tableFieldsDTO.setFieldName("id");
            tableFieldsDTO.setFieldType("int");
            tableFieldsDTO.setFieldDes("主键");
            tableFieldsDTO.setIsPrimarykey(1);
        }


*//*        TableFieldsDTO tableFieldsDTO2 = new TableFieldsDTO();
        tableFieldsDTO2.setFieldName("name");
        tableFieldsDTO2.setFieldType("String");
        tableFieldsDTO2.setFieldDes("名称");
        tableFieldsDTO2.setIsPrimarykey(0);*//*


        tableFieldsDTOList.add(tableFieldsDTO1);
//        tableFieldsDTOList.add(tableFieldsDTO2);

        System.out.println(tableAccessDTO);*/


        // 1.连接数据库
        Connection conn = DriverManager.getConnection(url,user,pwd);
        Statement stat = conn.createStatement();

        // 获取数据库名
/*        ResultSet rs = conn.getMetaData().getTables(null, null, "input_table_access", null);
        // 判断表是否存在，如果存在则什么都不做，否则创建表
        if (rs.next()) {
            System.out.println("数据库已存在");
            return;
        }*/

        List<TableFieldsDTO> tableFieldsDTOS = tableAccessDTO.getList();
        for (TableFieldsDTO tableFieldsDTO : tableFieldsDTOS) {

        }
        StringBuilder sb_PRIMARYKEY=new StringBuilder();
        StringBuilder sb=new StringBuilder();
        sb.append("CREATE TABLE Test");
        for (TableFieldsDTO dto : tableFieldsDTOS) {
            if(dto.getIsPrimarykey()==1)
            {
                sb_PRIMARYKEY.append("PRIMARY KEY ("+dto.getFieldName()+")");
            }
            sb.append(dto.getFieldName()+dto.getFieldType()+dto.getFieldDes());
        }
        sb.append(sb_PRIMARYKEY.toString());

//        System.out.println(sb.toString());

//        stat.executeUpdate("+sb.+");

        /*stat.executeUpdate("CREATE TABLE `input_table_access` (\n" +
        for (TableFieldsDTO tableFieldsDTO : tableFieldsDTOS) {
            long id = tableAccessDTO.getId();

        }*/
/*                "  `id` int NOT NULL AUTO_INCREMENT,\n" +
                "  `appid` int NOT NULL COMMENT 'app_registration（id）',\n" +
                "  `table_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '物理表名',\n" +
                "  `table_des` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物理表描述',\n" +
                "  `sync_src` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '如果是实时物理表，需要提供数据同步地址',\n" +
                "  `is_realtime` tinyint(1) NOT NULL COMMENT '0是实时物理表，1是非实时物理表',\n" +
                "  `create_time` datetime DEFAULT NULL COMMENT '创建时间',\n" +
                "  `create_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人（id）',\n" +
                "  `update_time` datetime DEFAULT NULL COMMENT '更新时间',\n" +
                "  `update_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人（id）',\n" +
                "  `del_flag` tinyint DEFAULT NULL COMMENT '逻辑删除（1未删除，0删除）',\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;"*/
//        );


        // 创建表
//        stat.executeUpdate("CREATE TABLE `" + tableAccessDTO.getTableName() + "`");



        // 释放资源
        stat.close();
        conn.close();
    }
}
