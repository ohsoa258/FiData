package com.fisk.common.service.dbBEBuild.datamodel.impl;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.service.dbBEBuild.datamodel.IBuildDataModelSqlCommand;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceTableConfigDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public class BuildDataModelOracleCommandImpl implements IBuildDataModelSqlCommand {
    @Override
    public String buildAppendField(List<TableSourceTableConfigDTO> entity) {
        return null;
    }

    @Override
    public String buildAppendRelationTable(List<TableSourceRelationsDTO> relations) {
        return null;
    }

    @Override
    public String buildPageSql(String sql, Integer pageSize) {
        return null;
    }

    @Override
    public String buildSelectTable(List<TableSourceRelationsDTO> relations, String tableName, JSONObject jsonObject) {
        return null;
    }

    @Override
    public String buildTimeDimensionCreateTable(String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE ");
        str.append(tableName + "(");
        str.append("FullDateAlternateKey date not null,");
        str.append("DayNumberOfWeek number  not null,");
        str.append("EnglishDayNameOfWeek varchar2(10) not null,");
        str.append("DayNumberOfMonth number not null,");
        str.append("DayNumberOfYear number not null,");
        str.append("WeekNumberOfYear number not null,");
        str.append("EnglishMonthName varchar2(10) not null,");
        str.append("MonthNumberOfYear number not null,");
        str.append("CalendarQuarter number not null,");
        str.append("CalendarYear number not null)");
        str.append("FullDateKey date not null)");
        str.append("Is_WeekDay number not null)");

        return str.toString();
    }
}
