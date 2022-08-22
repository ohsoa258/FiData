package com.fisk.common.service.pageFilter.utils;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
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

    /**
     * 获取表特定字段、描述、类型
     *
     * @return 查询结果
     */
    public List<FilterFieldDTO> getMetadataList(MetaDataConfigDTO dto) {
        List<FilterFieldDTO> list = new ArrayList<>();
        try {
            Class.forName(dto.driver);
            //连接数据源
            Connection conn = DriverManager.getConnection(dto.url, dto.userName, dto.password);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("show full columns from " + dto.tableName + dto.filterSql);
            //获取表字段名称、描述、数据类型
            while (rs.next()) {
                FilterFieldDTO model = new FilterFieldDTO();
                if (StringUtils.isNotEmpty(dto.tableAlias)) {
                    model.columnName = dto.tableAlias + "." + rs.getString("Field");
                } else {
                    model.columnName = "`" + rs.getString("Field") + "`";
                }
                model.columnDes = rs.getString("Comment");
                model.columnType = rs.getString("Type");
                list.add(model);
            }
            st.close();
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return list;
    }


}
