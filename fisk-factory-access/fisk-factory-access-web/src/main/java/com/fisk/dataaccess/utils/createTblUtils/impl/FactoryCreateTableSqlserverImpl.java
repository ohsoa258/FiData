package com.fisk.dataaccess.utils.createTblUtils.impl;

import com.fisk.dataaccess.dto.datasource.DataSourceFullInfoDTO;
import com.fisk.dataaccess.entity.TableFieldsPO;
import com.fisk.dataaccess.utils.createTblUtils.IBuildCreateTableFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

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
        sql.append("fi_createtime DATETIME DEFAULT CURRENT_TIMESTAMP");
        sql.append(");");
        return String.valueOf(sql);
    }

    @Override
    public String checkTableIfNotExists() {
        return "SELECT CASE WHEN EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?" +
                " AND TABLE_NAME = ?) THEN 1 ELSE 0 END AS isExists";
    }

    @Override
    public String createSourceSql(String tableName, List<TableFieldsPO> fieldList, DataSourceFullInfoDTO dto) {
        StringBuilder sql = new StringBuilder("CREATE TABLE source_" + tableName + "(");
        for (TableFieldsPO tableFieldsPO : fieldList) {
            //STRING
            if (tableFieldsPO.getFieldType().equalsIgnoreCase("VARCHAR")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("NVARCHAR")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("CHAR")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("NCHAR")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("TEXT")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("NTEXT")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("UNIQUEIDENTIFIER")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("XML")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" STRING,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("TINYINT")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("INT")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("int identity")
            ) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("INT,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("BINARY")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("VARBINARY")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("IMAGE")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("BYTES,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("TIME")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("TIME(0),");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("DATETIME")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("DATETIME2")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("SMALLDATETIME")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("timestamp")
            ) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("TIMESTAMP(3),");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("BIT")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("BOOLEAN,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("SMALLINT")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("SMALLINT,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("BIGINT")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("BIGINT,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("DECIMAL")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("NUMERIC")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("DECIMAL,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("FLOAT")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("DOUBLE,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("REAL")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("FLOAT,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("DATE")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("DATE,");
            }
        }
        List<TableFieldsPO> pkFields = fieldList.stream().filter(f -> f.getIsPrimarykey() == 1).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(pkFields)) {
            sql.append(" PRIMARY KEY (");
            for (TableFieldsPO pkField : pkFields) {
                sql.append(pkField.getFieldName())
                        .append(",");

            }
            //去除多余逗号
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") NOT ENFORCED ");
        } else {
            //去除多余逗号
            sql.deleteCharAt(sql.length() - 1);
        }

        //拼接WITH...
        sql.append(") WITH (")
                .append("'connector' = '")
                .append(dto.getConnector())
                .append("',")
                .append("'hostname' = '")
                .append(dto.getIp())
                .append("',")
                .append("'port' = '")
                .append(dto.getPort())
                .append("',")
                .append("'username' = '")
                .append(dto.getUserName())
                .append("',")
                .append("'password' = '")
                .append(dto.getPassword())
                .append("',")
                .append("'database-name' = '")
                .append(dto.getDbName())
                .append("',")
                .append("'table-name' = '")
                .append(dto.getSchemaName())
                .append(".")
                .append(dto.getTableName())
                .append("');");
        return String.valueOf(sql);
    }

    @Override
    public String createSinkSql(String tableName, List<TableFieldsPO> fieldList, DataSourceFullInfoDTO dto) {
        StringBuilder sql = new StringBuilder("CREATE TABLE target_" + tableName + "(");
        for (TableFieldsPO tableFieldsPO : fieldList) {
            //STRING
            if (tableFieldsPO.getFieldType().equalsIgnoreCase("VARCHAR")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("NVARCHAR")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("CHAR")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("NCHAR")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("TEXT")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("NTEXT")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("UNIQUEIDENTIFIER")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("XML")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" STRING,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("TINYINT")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("INT")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("int identity")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("INT,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("BINARY")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("VARBINARY")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("IMAGE")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("BYTES,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("TIME")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("TIME(0),");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("DATETIME")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("DATETIME2")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("SMALLDATETIME")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("timestamp")
            ) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("TIMESTAMP(3),");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("BIT")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("BOOLEAN,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("SMALLINT")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("SMALLINT,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("BIGINT")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("BIGINT,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("DECIMAL")
                    || tableFieldsPO.getFieldType().equalsIgnoreCase("NUMERIC")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("DECIMAL,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("FLOAT")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("DOUBLE,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("REAL")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("FLOAT,");
            } else if (tableFieldsPO.getFieldType().equalsIgnoreCase("DATE")) {
                sql.append(tableFieldsPO.getFieldName())
                        .append(" ")
                        .append("DATE,");
            }
        }
        List<TableFieldsPO> pkFields = fieldList.stream().filter(f -> f.getIsPrimarykey() == 1).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(pkFields)) {
            sql.append(" PRIMARY KEY (");
            for (TableFieldsPO pkField : pkFields) {
                sql.append(pkField.getFieldName())
                        .append(",");

            }
            //去除多余逗号
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") NOT ENFORCED ");
        } else {
            //去除多余逗号
            sql.deleteCharAt(sql.length() - 1);
        }

        //拼接WITH...
        sql.append(") WITH (")
                .append("'connector' = '")
                .append("jdbc")
                .append("',")
                .append("'url' = '")
                .append(dto.getUrl())
                .append("',")
                .append("'driver' = '")
                .append("com.microsoft.sqlserver.jdbc.SQLServerDriver")
                .append("',")
                .append("'username' = '")
                .append(dto.getUserName())
                .append("',")
                .append("'password' = '")
                .append(dto.getPassword())
                .append("',")
                .append("'table-name' = '")
                .append(dto.getSchemaName())
                .append(".")
                .append(dto.getTableName())
                .append("');");
        return String.valueOf(sql);
    }

    @Override
    public String createInsertSql(String tableName, List<TableFieldsPO> fieldList, DataSourceFullInfoDTO dto) {
        StringBuilder sql = new StringBuilder("INSERT INTO target_" + tableName + " SELECT ");
        for (TableFieldsPO po : fieldList) {
            sql.append(po.getFieldName())
                    .append(",");
        }
        //去除多余逗号
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" FROM source_")
                .append(tableName)
                .append(";");

        return String.valueOf(sql);
    }

}

