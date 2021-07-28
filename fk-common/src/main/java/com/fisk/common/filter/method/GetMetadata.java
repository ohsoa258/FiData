package com.fisk.common.filter.method;

import com.fisk.common.exception.FkException;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.response.ResultEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Component
public class GetMetadata {

    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String URL = "jdbc:mysql://192.168.11.130:3306/";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root123";

    /**
     * 获取表所有字段、描述、类型
     * @param dataBaseName 库名称
     * @param tableName 表名称
     * @param tableAlias 表别名
     * @return
     */
    public List<FilterFieldDTO> getMetadataList(String dataBaseName,String tableName,String tableAlias)
    {
        List<FilterFieldDTO> list=new ArrayList<>();
        try {
            Class.forName(DRIVER);
            //连接数据源
            Connection conn = DriverManager.getConnection(URL+dataBaseName, USERNAME, PASSWORD);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("show full columns from " + tableName + " where Field not in ('id','del_flag','create_user','update_user')");
            //获取表字段名称、描述、数据类型
            while (rs.next()) {
                FilterFieldDTO model = new FilterFieldDTO();
                if (StringUtils.isNotEmpty(tableAlias))
                {
                    model.columnName = tableAlias+"."+ rs.getString("Field");
                }else {
                    model.columnName = rs.getString("Field");
                }
                model.columnDes = rs.getString("Comment");
                model.columnType = rs.getString("Type");
                list.add(model);
            }
            st.close();
            conn.close();
        }
        catch (ClassNotFoundException | SQLException e)
        {
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return list;
    }


}
