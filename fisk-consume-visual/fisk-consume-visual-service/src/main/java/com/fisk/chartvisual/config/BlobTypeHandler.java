package com.fisk.chartvisual.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.util.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.sql.*;

/**
 * blod 转base64
 * @author JinXingWang
 */
public class BlobTypeHandler extends BaseTypeHandler<String> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Blob blob = rs.getBlob(columnName);
        if (blob == null){
            return null;
        }

        String base64Binary = DatatypeConverter.printBase64Binary(blob.getBytes(1, (int) blob.length()));
        if (blob == null) {
            return "";
        } else if (!StringUtils.isEmpty(base64Binary)) {
            return "data:image/jpg;base64," + base64Binary;
        }

        return "";
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Blob blob = rs.getBlob(columnIndex);
        if (blob == null){
            return null;
        }

        String base64Binary = DatatypeConverter.printBase64Binary(blob.getBytes(1, (int) blob.length()));
        if (blob == null) {
            return "";
        } else if (!StringUtils.isEmpty(base64Binary)) {
            return "data:image/jpg;base64," + base64Binary;

        }

        return "";
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Blob blob = cs.getBlob(columnIndex);
        if (blob == null){
            return null;
        }

        String base64Binary = DatatypeConverter.printBase64Binary(blob.getBytes(1, (int) blob.length()));
        if (blob == null) {
            return "";
        } else if (!StringUtils.isEmpty(base64Binary)) {
            return "data:image/jpg;base64," + base64Binary;
        }

        return "";
    }

}