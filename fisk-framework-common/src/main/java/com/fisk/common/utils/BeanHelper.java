package com.fisk.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Slf4j
public class BeanHelper {

    /**
     * 拷贝对象
     *
     * @param source 源对象
     * @param target 目标对象的类型
     * @param <T>    目标的泛型裂隙
     * @return 目标类型的对象
     */
    public static <T> T copyProperties(Object source, Class<T> target) {
        try {
            T t = target.newInstance();
            BeanUtils.copyProperties(source, t);
            return t;
        } catch (InstantiationException e) {
            throw new RuntimeException(target.getName() + "无法被实例化，可能是一个接口或抽象类");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(target.getName() + "无法被实例化，构造函数无法访问");
        }
    }

    public static <T> List<T> copyWithCollection(List<?> sourceList, Class<T> target) {
        return sourceList.stream().map(s -> copyProperties(s, target)).collect(Collectors.toList());
    }

    public static <T> Set<T> copyWithCollection(Set<?> sourceList, Class<T> target) {
        return sourceList.stream().map(s -> copyProperties(s, target)).collect(Collectors.toSet());
    }

    public static <T> List<T> resultSetToList(ResultSet resultSet, Class<T> className) {
        List<T> list = new ArrayList<>();
        Field[] fields = className.getDeclaredFields();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                T instance = className.newInstance();
                setFieldValue(resultSet, metaData, fields, instance);
                list.add(instance);
            }
        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            log.error("【resultSetToList】转换bean报错, ex", e);
            return null;
        }
        return list;
    }

    public static <T> T resultSetToBean(ResultSet resultSet, Class<T> className) {
        T instance = null;
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            instance = className.newInstance();
            Field[] fields = className.getDeclaredFields();
            setFieldValue(resultSet, metaData, fields, instance);
        } catch (InstantiationException | IllegalAccessException | SQLException e) {
            log.error("【resultSetToBean】转换bean报错, ex", e);
            return null;
        }
        return instance;
    }

    public static List<Map<String, Object>> resultSetToMaps(ResultSet rs) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next()) {
                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnLabel(i), rs.getString(i));
                }
                list.add(rowData);
            }
        } catch (SQLException e) {
            log.error("【resultSetToMaps】转换bean报错, ex", e);
            return null;
        }
        return list;
    }

    public static Map<String, Object> resultSetToMap(ResultSet rs) {
        Map<String, Object> rowData = new HashMap<>();
        try {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    rowData.put(md.getColumnLabel(i), rs.getObject(i));
                }
            }
        } catch (SQLException e) {
            log.error("【resultSetToMap】转换bean报错, ex", e);
            return null;
        }
        return rowData;
    }

    private static <T> void setFieldValue(ResultSet resultSet, ResultSetMetaData metaData, Field[] fields, T instance) throws SQLException, IllegalAccessException {
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String fieldName = metaData.getColumnLabel(i);
            Field field = Arrays.stream(fields).filter(e -> e.getName().equals(fieldName)).findFirst().orElse(null);
            if (field != null) {
                Object result = resultSet.getString(fieldName);
                boolean flag = field.isAccessible();
                field.setAccessible(true);
                field.set(instance, result);
                field.setAccessible(flag);
            }
        }
    }
}
