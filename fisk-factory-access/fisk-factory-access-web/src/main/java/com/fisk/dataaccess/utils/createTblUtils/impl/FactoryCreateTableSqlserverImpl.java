package com.fisk.dataaccess.utils.createTblUtils.impl;

import com.fisk.dataaccess.entity.TableFieldsPO;
import com.fisk.dataaccess.utils.createTblUtils.IBuildCreateTableFactory;

import java.util.List;

public class FactoryCreateTableSqlserverImpl implements IBuildCreateTableFactory {

    @Override
    public String createTable(String tableName, List<TableFieldsPO> fieldList) {
        StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + "(");
        for (TableFieldsPO tableFieldsPO : fieldList) {
            //主键
            if (tableFieldsPO.getIsPrimarykey() == 1) {
                if (tableFieldsPO.getFieldType().equalsIgnoreCase("VARCHAR")
                        || tableFieldsPO.getFieldType().equalsIgnoreCase("NVARCHAR")) {
                    sql.append(tableFieldsPO.getFieldName())
                            .append(" ")
                            .append(tableFieldsPO.getFieldType())
                            .append(" (")
                            .append(tableFieldsPO.getFieldLength())
                            .append(") PRIMARY KEY,");
                } else {
                    sql.append(tableFieldsPO.getFieldName())
                            .append(" ")
                            .append(tableFieldsPO.getFieldType())
                            .append(" ")
                            .append("PRIMARY KEY,");
                }
            } else {
                if (tableFieldsPO.getFieldType().equalsIgnoreCase("VARCHAR")
                        || tableFieldsPO.getFieldType().equalsIgnoreCase("NVARCHAR")) {
                    sql.append(tableFieldsPO.getFieldName())
                            .append(" ")
                            .append(tableFieldsPO.getFieldType())
                            .append(" (")
                            .append(tableFieldsPO.getFieldLength())
                            .append("),");
                } else {
                    sql.append(tableFieldsPO.getFieldName())
                            .append(" ")
                            .append(tableFieldsPO.getFieldType())
                            .append(",");
                }
            }
        }
        //去除多余逗号
        sql.deleteCharAt(sql.length() - 1);
        sql.append(");");
        return String.valueOf(sql);
    }

    @Override
    public String checkTableIfNotExists() {
        return "SELECT CASE WHEN EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?" +
                " AND TABLE_NAME = ?) THEN 1 ELSE 0 END AS isExists";
    }

}

