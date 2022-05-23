package com.fisk.common.service.mdmBEBuild;

import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;

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

}
