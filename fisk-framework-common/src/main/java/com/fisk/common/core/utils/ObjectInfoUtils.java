package com.fisk.common.core.utils;

import com.fisk.common.core.utils.Dto.SqlParmDto;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JianWenYang
 */
public class ObjectInfoUtils {

    /**
     * 获取对象属性名以及属性值
     *
     * @param o
     * @return
     */
    public static List<Map<String, Object>> getFiledsInfo(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        List list = new ArrayList();
        Map infoMap;
        for (int i = 0; i < fields.length; i++) {
            infoMap = new HashMap();
            infoMap.put(fields[i].getName(), getFieldValueByName(fields[i].getName(), o));
            list.add(infoMap);
        }
        return list;
    }

    /**
     * 根据属性名获取属性值
     *
     * @param fieldName
     * @param o
     * @return
     */
    private static Object getFieldValueByName(String fieldName, Object o) {
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

    @Test
    public void test() {
        SqlParmDto dto = new SqlParmDto();
        dto.parmName = "a";
        dto.parmValue = "b";
        List filedsInfo = getFiledsInfo(dto);
        String test = "";
    }

}
