package com.fisk.common.service.dbBEBuild.datamodel.impl;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.service.dbBEBuild.datamodel.IBuildDataModelSqlCommand;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceFieldConfigDTO;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceTableConfigDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
public class BuildDataModelSqlServerCommandImpl implements IBuildDataModelSqlCommand {

    @Override
    public String buildAppendField(List<TableSourceTableConfigDTO> entity) {
        StringBuilder str = new StringBuilder();
        List<String> fieldList = new ArrayList<>();
        for (TableSourceTableConfigDTO item : entity) {
            for (TableSourceFieldConfigDTO field : item.columnConfig) {
                //判断字段名称是否重复
                if (fieldList.contains(field.fieldName)) {
                    field.alias = item.tableName + "_" + field.fieldName;
                    str.append("external_" + item.tableName + "." + field.fieldName + " as " + field.alias + ",");
                } else {
                    str.append("external_" + item.tableName + "." + field.fieldName + ",");
                    fieldList.add(field.fieldName);
                }
            }
        }
        return str.toString();
    }

    @Override
    public String buildAppendRelationTable(List<TableSourceRelationsDTO> relations) {
        StringBuilder appendSql = new StringBuilder();
        Map<String, List<TableSourceRelationsDTO>> groupMap = relations.stream().collect(Collectors.groupingBy(TableSourceRelationsDTO::getSourceTable));
        for (int i = 0; i < relations.size(); i++) {
            if (i == 0) {
                appendSql.append(" from " + prefixTable(relations.get(i).sourceTable) + " ");
                appendSql.append(relations.get(i).joinType + " " + prefixTable(relations.get(i).targetTable));
                appendSql.append(" on " + prefixTable(relations.get(i).sourceTable) + "." + relations.get(i).sourceColumn);
                appendSql.append(" = ");
                appendSql.append(prefixTable(relations.get(i).targetTable) + "." + relations.get(i).targetColumn);
            } else {
                TableSourceRelationsDTO attribute = relations.get(i);
                appendSql.append(" " + attribute.joinType + " ");
                appendSql.append(prefixTable(attribute.targetTable));
                appendSql.append(" on " + prefixTable(attribute.sourceTable) + "." + attribute.sourceColumn + " = ");
                appendSql.append(prefixTable(attribute.targetTable) + "." + attribute.targetColumn + " ");
            }
            //判断on后面关联条件是否存在多个
            for (Map.Entry<String, List<TableSourceRelationsDTO>> map : groupMap.entrySet()) {
                for (TableSourceRelationsDTO item : map.getValue()) {
                    if (relations.get(i) != item && item.targetTable.equals(relations.get(i).targetTable)) {
                        appendSql.append(" and " + prefixTable(item.sourceTable) + "." + item.sourceColumn);
                        appendSql.append(" = ");
                        appendSql.append(prefixTable(item.targetTable) + "." + item.targetColumn);
                        relations.remove(item);
                    }
                }
            }
        }
        return appendSql.toString();
    }

    @Override
    public String buildPageSql(String sql, Integer pageSize) {
        return "select top " + pageSize + " * from (" + sql + ") as tabInfo";
    }

    @Override
    public String buildSelectTable(List<TableSourceRelationsDTO> relations, String tableName, JSONObject jsonObject) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT * FROM ").append(tableName).append(" WHERE ");
        for (TableSourceRelationsDTO relation : relations) {

            // 源字段的值
            String targetColumnValue = jsonObject.getString(relation.sourceColumn);
            str.append(relation.targetColumn).append(" = ")
                    .append("'").append(targetColumnValue == null ? "" : targetColumnValue).append("'")
                    .append(" AND ");
        }
        // 去掉最后的 AND
        str.delete(str.length() - 4, str.length());
        return str.toString();
    }

    @Override
    public String buildTimeDimensionCreateTable(String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE ");
        str.append(tableName + "(");
        str.append("FullDateAlternateKey date not null,");
        str.append("DayNumberOfWeek int not null,");
        str.append("EnglishDayNameOfWeek varchar(10) not null,");
        str.append("DayNumberOfMonth int not null,");
        str.append("DayNumberOfYear int not null,");
        str.append("WeekNumberOfYear int not null,");
        str.append("EnglishMonthName varchar(10) not null,");
        str.append("MonthNumberOfYear int not null,");
        str.append("CalendarQuarter int not null,");
        str.append("CalendarYear int not null,");
        str.append("FullDateKey date not null,");
        str.append("Is_WeekDay int not null)");

        return str.toString();
    }

    public String prefixTable(String tableName) {
        return "external_" + tableName;
    }
}
