package com.fisk.common.service.mdmBEBuild;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
            //获取列名
            if (type == 0) {
                columnList.add(name);
            }
            //拼接value
            else {
                if (StringUtils.isEmpty(entry.getValue() == null ? "" : entry.getValue().toString())) {
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
                    if ("null".equals(strList.get(i))) {
                        sb.append("null");
                        continue;
                    }
                    sb.append("'").append(strList.get(i)).append("'");
                } else {
                    if ("null".equals(strList.get(i))) {
                        sb.append(",").append("null");
                        continue;
                    }
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

    /**
     * 对象深拷贝
     *
     * @param src
     * @param <T>
     * @return
     */
    public static <T> List<T> deepCopy(List<T> src) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(src);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            @SuppressWarnings("unchecked")
            List<T> dest = (List<T>) in.readObject();
            return dest;
        } catch (Exception e) {
            throw new FkException(ResultEnum.COPY_ERROR, e);
        }
    }

    /**
     * Json串key替换指定值
     *
     * @param jsonObj
     * @param keyMap
     * @return
     */
    public static JSONObject changeJsonObj(JSONObject jsonObj, Map<String, String> keyMap) {
        JSONObject resJson = new JSONObject();
        Set<String> keySet = jsonObj.keySet();
        for (String key : keySet) {
            String resKey = keyMap.get(key) == null ? key : keyMap.get(key);
            try {
                JSONObject jsonObj1 = jsonObj.getJSONObject(key);
                resJson.put(resKey, changeJsonObj(jsonObj1, keyMap));
            } catch (Exception e) {
                try {
                    JSONArray jsonArr = jsonObj.getJSONArray(key);
                    resJson.put(resKey, changeJsonArr(jsonArr, keyMap));
                } catch (Exception x) {
                    resJson.put(resKey, jsonObj.get(key));
                }
            }
        }
        return resJson;
    }

    public static JSONArray changeJsonArr(JSONArray jsonArr, Map<String, String> keyMap) {
        JSONArray resJson = new JSONArray();
        for (int i = 0; i < jsonArr.size(); i++) {
            JSONObject jsonObj = jsonArr.getJSONObject(i);
            resJson.add(changeJsonObj(jsonObj, keyMap));
        }
        return resJson;
    }


}
