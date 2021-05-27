package com.fisk.common.mybatis.config;

import com.fisk.common.enums.BaseEnum;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class BaseEnumTypeHandler<T extends BaseEnum> extends BaseTypeHandler<T> {

    /**
     * 枚举的class
     */
    private Class<T> type;
    /**
     * 枚举的每个子类枚
     */
    private T[] enums;

    public BaseEnumTypeHandler() {
    }

    public BaseEnumTypeHandler(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
        this.enums = type.getEnumConstants();
        if (this.enums == null) {
            throw new IllegalArgumentException(type.getSimpleName()
                    + " does not represent an enum type.");
        }
    }

    /**
     * 用于定义设置参数时，该如何把Java类型的参数转换为对应的数据库类型；
     * @param preparedStatement
     * @param i
     * @param t
     * @param jdbcType
     * @throws SQLException
     */
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, T t, JdbcType jdbcType) throws SQLException {
        if (jdbcType == null) {
            preparedStatement.setString(i, Objects.toString(t.getValue()));
        } else {
            preparedStatement.setObject(i, t.getValue(), jdbcType.TYPE_CODE);
        }
    }

    /**
     * 用于定义通过字段名称获取字段数据时，如何把数据库类型转换为对应的Java类型
     * @param resultSet
     * @param s
     * @return
     * @throws SQLException
     */
    @Override
    public T getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String i = resultSet.getString(s);
        if (resultSet.wasNull()) {
            return null;
        } else {
            return locateEnumStatus(i);
        }
    }

    /**
     * 用于定义通过字段索引获取字段数据时，如何把数据库类型转换为对应的Java类型
     * @param resultSet
     * @param i
     * @return
     * @throws SQLException
     */
    @Override
    public T getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String res = resultSet.getString(i);
        if (resultSet.wasNull()) {
            return null;
        } else {
            return locateEnumStatus(res);
        }
    }

    /**
     * 用定义调用存储过程后，如何把数据库类型转换为对应的Java类型
     * @param callableStatement
     * @param i
     * @return
     * @throws SQLException
     */
    @Override
    public T getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String res = callableStatement.getString(i);
        if (callableStatement.wasNull()) {
            return null;
        } else {
            return locateEnumStatus(res);
        }
    }

    /**
     * 枚举类型转换，由于构造函数获取了枚举的子类 enums，让遍历更加高效快捷，
     * <p>
     * 我将取出来的值 全部转换成字符串 进行比较，
     *
     * @param value 数据库中存储的自定义value属性
     * @return value 对应的枚举类
     */
    private T locateEnumStatus(String value) {
        for (T e : enums) {
            if (Objects.toString(e.getValue()).equals(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知的枚举类型：" + value + ",请核对"
                + type.getSimpleName());
    }
}
