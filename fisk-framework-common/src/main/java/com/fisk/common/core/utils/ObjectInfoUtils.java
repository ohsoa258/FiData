package com.fisk.common.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author JianWenYang
 */
@Slf4j
public class ObjectInfoUtils {

    /**
     * 获取对象属性名和属性值
     *
     * @param object
     * @return
     */
    public static Map<String, Object> getObjectKeyAndValue(Object object) {
        Map<String, Object> map = new HashMap<>();
        Class<?> clz = object.getClass();
        Field[] fields = clz.getDeclaredFields();

        try {
            for (Field field : fields) {
                Object val = FieldUtils.readField(field, object, true);
                map.put(field.getName(), val);

            }
        } catch (Exception e) {
            log.error("获取属性异常：{}", e);
        }

        return map;
    }

}
