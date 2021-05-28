package com.fisk.chartvisual;

import com.fisk.chartvisual.util.dscon.AbstractUseDataBase;
import com.fisk.chartvisual.util.dscon.DataSourceConFactory;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UseDataBaseTest {


    @Test
    public void getConnection_SQLServer() {
        Connection con = null;
        Statement st = null;
        String sqlServerConnection = "jdbc:sqlserver://192.168.3.16:1433;databaseName=WorkOrder;user=sa;password=zxc19981213";
        String sql = "select * from Tb_WorkOrder";
        try {
            AbstractUseDataBase db = DataSourceConFactory.getConnection(DataSourceTypeEnum.SQLSERVER);
            con = db.connection(sqlServerConnection, null, null);
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                System.out.println(rs.getString("WorkOrderNumber") + ", " + rs.getString("WorkOrderName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }

    @Test
    public void getConnection_Mysql() {
        Connection con = null;
        Statement st = null;
        String sqlServerConnection = "jdbc:mysql://192.168.11.130:3306/dmp_chartvisual_db?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";
        String sql = "select * from tb_datasource_con";
        try {
            AbstractUseDataBase db = DataSourceConFactory.getConnection(DataSourceTypeEnum.MYSQL);
            con = db.connection(sqlServerConnection, "root", "root123");
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                System.out.println(rs.getString("con_type") + ", " + rs.getString("con_str"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }
}
