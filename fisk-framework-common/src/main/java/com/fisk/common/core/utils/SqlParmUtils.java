package com.fisk.common.core.utils;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.utils.Dto.SqlParmDto;
import com.fisk.common.core.utils.Dto.SqlWhereDto;
import com.google.common.base.Joiner;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version v1.0
 * @description sql 参数拼接
 * @date 2022/1/16 13:48
 */
public class SqlParmUtils {
    /**
     * 拼接sql条件
     *
     * @param list 参数集合
     * @return String
     */
    public static String SqlWhere(List<SqlWhereDto> list) {
        String sql = "";
        if (CollectionUtils.isEmpty(list))
            return sql;
        for (SqlWhereDto item : list) {
            switch (item.operator) {
                case "LIKE":
                    sql += String.format(" AND %s like %s", item.fieldName, "%" + item.fieldValue + "%");
                    break;
                case "EQU":
                    sql += String.format(" AND %s = %s", item.fieldName, item.fieldValue);
                    break;
                case "NEQ":
                    sql += String.format(" AND %s != %s", item.fieldName, item.fieldValue);
                    break;
                case "LSS":
                    sql += String.format(" AND %s < %s", item.fieldName, item.fieldValue);
                    break;
                case "LEQ":
                    sql += String.format(" AND %s <= %s", item.fieldName, item.fieldValue);
                    break;
                case "GTR":
                    sql += String.format(" AND %s > %s", item.fieldName, item.fieldValue);
                    break;
                case "GEQ":
                    sql += String.format(" AND %s >= %s", item.fieldName, item.fieldValue);
                    break;
            }
        }
        return sql;
    }

    /**
     * 拼接sql请求参数条件
     *
     * @param list   参数集合
     * @param repSql 替换的sql
     * @param symbol 符号
     * @return String
     */
    public static String SqlParm(List<SqlParmDto> list, String repSql, String symbol) {
        String sql = repSql;
        if (CollectionUtils.isEmpty(list) || sql == null || sql.isEmpty())
            return sql;
        for (SqlParmDto item : list) {
            String targetKey = String.format("%s%s", symbol, item.parmName);
            String replacement = "'" + item.parmValue + "'";
            sql = sql.replace(targetKey, replacement);
        }
        return sql;
    }

    /**
     * @return java.lang.String
     * @description 分割字符串为in查询
     * @author dick
     * @date 2022/3/28 19:33
     * @version v1.0
     * @params list
     */
    public static <T> String parseListToParmStr(List<T> list) {
        String result = null;
        if (CollectionUtils.isNotEmpty(list)) {
            result = Joiner.on(",").join(list);
        }
        return result;
    }

    /**
     * @return java.lang.String
     * @description in 查询，加 ‘’ 号
     * @author dick
     * @date 2022/6/2 16:34
     * @version v1.0
     * @params string
     */
    public static String getInParm(List<String> list) {
        list = list.stream().distinct().collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < list.size(); i++) {
            sb.append("'").append(list.get(i)).append("'");//拼接单引号,到数据库后台用in查询.
            if (i != list.size() - 1) {//前面的元素后面全拼上",",最后一个元素后不拼
                sb.append(",");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
