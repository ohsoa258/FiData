package com.fisk.common.service.dimensionquerysql.impl;

import com.fisk.common.service.dimensionquerysql.IBuildDimensionQuerySql;

/**
 * @author lishiji
 */
public class DimensionQuerySqlPgSqlImpl implements IBuildDimensionQuerySql {
    @Override
    public String buildDimensionQuerySql(String startTime, String endTime) {
        startTime = startTime.replace("-", "");
        endTime = endTime.replace("-", "");
        // 生成查询语句
        String selSql = "WITH DateList AS (\n" +
                "  SELECT generate_series('" + startTime + "'::date, '" + endTime + "'::date, interval '1 day') AS \"Date\"\n" +
                ")\n" +
                "SELECT\n" +
                "  TO_CHAR(\"Date\", 'yyyy-MM-dd') AS FullDateAlternateKey,\n" +
                "  EXTRACT(DOW FROM \"Date\") AS DayNumberOfWeek,\n" +
                "  TO_CHAR(\"Date\", 'Day') AS EnglishDayNameOfWeek,\n" +
                "  EXTRACT(DAY FROM \"Date\") AS DayNumberOfMonth,\n" +
                "  EXTRACT(DOY FROM \"Date\") AS DayNumberOfYear,\n" +
                "  EXTRACT(WEEK FROM \"Date\") AS WeekNumberOfYear,\n" +
                "  TO_CHAR(\"Date\", 'Month') AS EnglishMonthName,\n" +
                "  EXTRACT(MONTH FROM \"Date\") AS MonthNumberOfYear,\n" +
                "  EXTRACT(QUARTER FROM \"Date\") AS CalendarQuarter,\n" +
                "  EXTRACT(YEAR FROM \"Date\") AS CalendarYear\n" +
                "FROM DateList;";
        return selSql;
    }
}
