package com.fisk.datamodel.utils;

import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.ProjectDimensionMetaDTO;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Component
public class MySqlTableUtils {

    private  String url="jdbc:mysql://192.168.11.130:3306/dmp_datainput_db";
    private  String user="root";
    private  String pwd="root123";

    /**
     * 获取实时及非实时的表 表字段
     * @param metaDataEnum 维度表字段筛选类型。0:业务字段、1:属性
     * @return 查询结果
     */
    public List<ProjectDimensionMetaDTO> getTable() {


        List<ProjectDimensionMetaDTO> map = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, pwd);
            List<String> tableNames = getTables(conn);
            Statement st = conn.createStatement();
            map = new ArrayList<>();
            for (String tableName : tableNames) {
                ProjectDimensionMetaDTO model=new ProjectDimensionMetaDTO();
                ResultSet rs = st.executeQuery("show full columns from " + tableName);
                model.tableName=tableName;
                List<String> colNames = getColNames(rs);
                model.field=colNames;
                map.add(model);
                rs.close();
            }
            st.close();
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return map;
    }
    /**
     * 获取数据库中所有表名称
     *
     * @param conn conn
     * @return 返回值
     */
    private List<String> getTables(Connection conn){
        ArrayList<String> tablesList = null;
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet tables = databaseMetaData.getTables(null, null, "%", null);
            tablesList = new ArrayList<String>();
            while (tables.next()) {
                tablesList.add(tables.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR);
        }
        return tablesList;
    }

    /**
     * 获取表中所有字段名称
     *
     * @param rs rs
     */
    private List<String> getColNames(ResultSet rs){
        List<String> colNameList = null;
        try {

            colNameList = new ArrayList<String>();

            while (rs.next()) {
                colNameList.add(rs.getString("Field"));
            }
            rs.first();
        } catch (SQLException e) {
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return colNameList;
    }
}
