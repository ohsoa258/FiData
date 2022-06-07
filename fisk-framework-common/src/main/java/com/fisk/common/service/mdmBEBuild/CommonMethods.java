package com.fisk.common.service.mdmBEBuild;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author JianWenYang
 */
public class CommonMethods {

    /**
     * 拼接insert语句，获取列名或value
     *
     * @param member
     * @param type
     * @return
     */
    public static String getColumnNameAndValue(Map<String, Object> member, int type) {
        List<String> columnList = new ArrayList<>();
        Iterator iter = member.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = entry.getKey().toString();
            if (name.equals("internalId") || name.equals("ErrorAttribute")) {
                continue;
            }
            //获取列名
            if (type == 0) {
                columnList.add(name);
            }
            //拼接value
            else {
                if (StringUtils.isEmpty(entry.getValue().toString())) {
                    columnList.add("null");
                } else {
                    columnList.add("'" + entry.getValue().toString() + "'");
                }
            }
        }
        return Joiner.on(",").join(columnList);
    }

    public static String getFormatDate(Date date) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateTimeFormat.format(date);
    }

    /**
     * List<String>转为逗号隔开并加单引号
     *
     * @param strList
     * @return
     */
    public static String convertListToString(List<String> strList) {
        StringBuffer sb = new StringBuffer();
        if (CollectionUtils.isNotEmpty(strList)) {
            for (int i = 0; i < strList.size(); i++) {
                if (i == 0) {
                    sb.append("'").append(strList.get(i)).append("'");
                } else {
                    sb.append(",").append("'").append(strList.get(i)).append("'");
                }
            }
        }
        return sb.toString();
    }

    /**
     * 对象转key-value格式
     *
     * @param object
     * @return
     */
    public static Map beanToMap(Object object) {
        Map<String, Object> map = new HashMap<>();
        try {
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(object));
            }
            return map;
        } catch (IllegalAccessException e) {
            return map;
        }
    }

}
