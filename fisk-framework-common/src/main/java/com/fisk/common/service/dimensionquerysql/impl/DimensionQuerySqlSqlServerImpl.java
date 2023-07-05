package com.fisk.common.service.dimensionquerysql.impl;

import com.fisk.common.service.dimensionquerysql.IBuildDimensionQuerySql;

/**
 * @author lishiji
 */
public class DimensionQuerySqlSqlServerImpl implements IBuildDimensionQuerySql {
    @Override
    public String buildDimensionQuerySql(String startTime, String endTime) {
        startTime = startTime.replace("-", "");
        endTime = endTime.replace("-", "");
        // 生成查询语句
        String selSql = "DECLARE @StartDate DATETIME = '" + startTime + "';\n" +
                "\n" +
                "DECLARE @EndDate DATETIME = '" + endTime + "';\n" +
                "\n" +
                "\n" +
                "\n" +
                "WITH DateList AS (\n" +
                "\n" +
                "  SELECT TOP (DATEDIFF(DAY, @StartDate, @EndDate) + 1)\n" +
                "\n" +
                "    [Date] = DATEADD(DAY, ROW_NUMBER() OVER(ORDER BY a.object_id) - 1, @StartDate)\n" +
                "\n" +
                "  FROM sys.all_objects a\n" +
                "\n" +
                "  CROSS JOIN sys.all_objects b\n" +
                "\n" +
                ")\n" +
                "\n" +
                "SELECT\n" +
                "\n" +
                "  [Date] as FullDateAlternateKey,\n" +
                "\n" +
                "  DATEPART(WEEKDAY, [Date]) as DayNumberOfWeek,\n" +
                "\n" +
                "  DATENAME(WEEKDAY, [Date]) COLLATE SQL_Latin1_General_CP1_CS_AS as EnglishDayNameOfWeek,\n" +
                "\n" +
                "  DAY([Date]) as DayNumberOfMonth,\n" +
                "\n" +
                "  DATEPART(DAYOFYEAR, [Date]) as DayNumberOfYear,\n" +
                "\n" +
                "  DATEPART(WEEK, [Date]) as WeekNumberOfYear,\n" +
                "\n" +
                "  DATENAME(MONTH, [Date]) as EnglishMonthName,\n" +
                "\n" +
                "  DATEPART(MONTH, [Date]) as MonthNumberOfYear,\n" +
                "\n" +
                "  DATEPART(QUARTER, [Date]) as CalendarQuarter,\n" +
                "\n" +
                "  YEAR([Date]) as CalendarYear\n" +
                "\n" +
                "FROM DateList";
        return selSql;
    }
}
