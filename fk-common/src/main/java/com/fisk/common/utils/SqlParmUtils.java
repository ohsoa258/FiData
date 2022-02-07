package com.fisk.common.utils;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.utils.Dto.SqlParmDto;
import com.fisk.common.utils.Dto.SqlWhereDto;

import java.util.List;
import java.util.Optional;

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
}
