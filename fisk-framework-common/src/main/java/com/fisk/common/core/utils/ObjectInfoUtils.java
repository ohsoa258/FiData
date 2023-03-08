package com.fisk.common.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author JianWenYang
 */
@Slf4j
public class ObjectInfoUtils {

    public static String[] arr = {"name", "description", "displayName", "owner", "qualifiedName", "dbList", "tableList", "columnList"};

    /**
     * 获取对象属性名集合
     *
     * @param o
     * @return
     */
    public static String[] getFiledName(Object o) {
        Field[] fields = o.getClass().getFields();
        String[] fieldNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            if (containsArr(fields[i].getName())) {
                continue;
            }
            fieldNames[i] = fields[i].getName();
        }
        return fieldNames;
    }

    /**
     * 根据属性名,获取属性值
     *
     * @param fieldName
     * @param o
     * @return
     */
    public static Object getFieldValueByName(String fieldName, Object o) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = o.getClass().getMethod(getter, new Class[]{});
            Object value = method.invoke(o, new Object[]{});
            return value;
        } catch (Exception e) {

            return null;
        }
    }

    /**
     * 是否存在数组中
     *
     * @param fileName
     * @return
     */
    public static boolean containsArr(String fileName) {
        if (Arrays.asList(arr).contains(fileName)) {
            return true;
        }
        return false;
    }

}
