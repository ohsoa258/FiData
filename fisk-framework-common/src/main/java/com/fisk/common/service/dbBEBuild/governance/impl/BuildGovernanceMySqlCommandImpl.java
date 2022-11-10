package com.fisk.common.service.dbBEBuild.governance.impl;

import com.fisk.common.service.dbBEBuild.governance.IBuildGovernanceSqlCommand;
import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author dick
 * @version 1.0
 * @description mysql
 * @date 2022/11/7 18:06
 */
public class BuildGovernanceMySqlCommandImpl implements IBuildGovernanceSqlCommand {
    @Override
    public String buildPagingSql(String tableName, String fields, String orderBy, Integer pageIndex, Integer pageSize) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append(fields);
        str.append(" FROM ");
        str.append(tableName);
        if (StringUtils.isNotEmpty(orderBy)) {
            str.append(" ORDER BY " + orderBy);
        }
        // OFFSET从0开始
        str.append(String.format(" LIMIT %s OFFSET %s ", pageSize, (pageIndex - 1) * pageSize));
        return str.toString();
    }

    @Override
    public String buildPagingSql(String tableName, List<String> fields, String orderBy, Integer pageIndex, Integer pageSize) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append(Joiner.on(",").join(fields));
        str.append(" FROM ");
        str.append(tableName);
        if (StringUtils.isNotEmpty(orderBy)) {
            str.append(" ORDER BY " + orderBy);
        }
        str.append(String.format(" LIMIT %s OFFSET %s ", pageSize, (pageIndex - 1) * pageSize));
        return str.toString();
    }

    @Override
    public String buildQueryCountSql(String tableName, String queryConditions) {
        return null;
    }

    @Override
    public String buildQuerySql(String tableName, String fields, String queryConditions) {
        return null;
    }

    @Override
    public String buildSingleInsertSql(String tableName, Map<String, Object> member) {
        return null;
    }

    @Override
    public String buildSingleUpdateSql(String tableName, Map<String, Object> member, String editConditions) {
        StringBuilder str = new StringBuilder();
        str.append("UPDATE ");
        str.append(tableName);
        str.append(" SET ");
        String column = singleUpdateSql(member);
        str.append(column);
        str.append(" WHERE 1=1 ");
        str.append(editConditions);
        return str.toString();
    }

    /**
     * 单条修改SQL语句
     *
     * @param member
     * @return
     */
    private static String singleUpdateSql(Map<String, Object> member) {
        List<String> columnList = new ArrayList<>();
        Iterator iter = member.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = entry.getKey().toString();
            String value = "";
            if (StringUtils.isEmpty(entry.getValue() == null ? "" : entry.getValue().toString())) {
                value = "null";
            } else {
                value = "'" + entry.getValue().toString() + "'";
            }
            String result = name + "=" + value;
            columnList.add(result);
        }
        return Joiner.on(",").join(columnList);
    }
}
