package com.fisk.datagovernance.test;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerStatementParser;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.parser.Token;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.fastjson.JSON;
import com.fisk.datagovernance.test.dto.QueryModelInfo;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description SQL语法解析
 * @date 2022/11/23 16:06
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class DruidTest {

    @Test
    public void test() {
        String sql = "SELECT\n" +
                "\tName,\n" +
                "\tProvince,\n" +
                "\t[City] AS CCtiy \n" +
                "FROM\n" +
                "\tcfg.c_customer c\n" +
                "\tLEFT JOIN cfg.c_smonrt m ON c.ID= m.id \n" +
                "WHERE\n" +
                "\tc.City= '黄浦区'";

        String edit_sql = "UPDATE cfg.c_smonrt SET ID=1";

        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(
                sql, DbType.postgresql);
        // 只接收SELECT
        if (!Token.SELECT.equals(parser.getExprParser().getLexer().token())) {
            System.out.println("不支持 " + parser.getExprParser().getLexer().token() + " 语法，仅支持 SELECT 语法");
        }
        SQLStatement sqlStatement = parser.parseStatement();
        SQLServerSchemaStatVisitor visitor = new SQLServerSchemaStatVisitor();
        sqlStatement.accept(visitor);
        System.out.println(visitor.getColumns());
        System.out.println(visitor.getTables());
        System.out.println(visitor.getConditions());
        System.out.println(visitor.getDbType());

        QueryModelInfo queryModelInfo = parse("sqlserver", sql);
        System.out.println("SQL解析结果：" + JSON.toJSONString(queryModelInfo));
    }

    /**
     * 解析select sql生成QueryModelInfo
     *
     * @param dbTypeName mysql, oracle...
     * @param selectSql
     * @return
     */
    public static QueryModelInfo parse(String dbTypeName, String selectSql) {
        QueryModelInfo queryModelInfo = new QueryModelInfo();
        DbType dbType = DbType.valueOf(dbTypeName.toLowerCase());
        List<SQLStatement> statementList = SQLUtils.parseStatements(selectSql, dbType);
        // 仅解析查询语句
        for (SQLStatement statement : statementList) {
            if (statement instanceof SQLSelectStatement) {
                SchemaStatVisitor visitor = new SchemaStatVisitor(dbType);
                statement.accept(visitor);

                //解析表名
                Map<TableStat.Name, TableStat> tables = visitor.getTables();
                Set<TableStat.Name> tableNameSet = tables.keySet();
                List<String> tableNames = new ArrayList<>();
                for (TableStat.Name tb : tableNameSet) {
                    String tableName = tb.getName();
                    tableNames.add(tableName);
                }
                queryModelInfo.setTableNameList(tableNames);

                //查询列
                Collection<TableStat.Column> columns = visitor.getColumns();
                List<String> columnList = new ArrayList<>();
                columns.stream().forEach(row -> {
                    // 是否是查询字段列
                    if (row.isSelect()) {
                        columnList.add(row.getName());
                    }
                });
                queryModelInfo.setColumnList(columnList);

                //查询过滤条件
                List<TableStat.Condition> conditions = visitor.getConditions();
                Map<String, String> whereMap = new HashMap<>();
                conditions.stream().forEach(row -> {
                    if (row.getColumn().isWhere()) {
                        // 获取where条件的字段
                        String columnName = row.getColumn().getName();
                        String operator = row.getOperator();
                        whereMap.put(columnName, operator);
                    } else if (row.getColumn().isJoin()) {
                        // 获取关联条件的字段
                    }
//                    row.getColumn().isSelect();
//                    row.getColumn().isUpdate();
//                    row.getColumn().isGroupBy();
//                    row.getColumn().isHaving();
//                    row.getColumn().isPrimaryKey();
//                    row.getColumn().isUnique();
                });
                queryModelInfo.setWhereMap(whereMap);
                break;
            }
        }
        return queryModelInfo;
    }
}


