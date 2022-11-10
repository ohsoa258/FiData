package com.fisk.common.service.dbBEBuild.common.impl;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.fisk.common.service.dbBEBuild.common.IBuildCommonSqlCommand;
import com.fisk.common.service.dbBEBuild.common.dto.DruidFieldInfoDTO;
import jdk.nashorn.internal.runtime.ParserException;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author JianWenYang
 */
@Slf4j
public class BuildCommonSqlServerCommand implements IBuildCommonSqlCommand {

    @Override
    public String buildAllDbSql() {
        return "SELECT name as dbname FROM  master..sysdatabases WHERE name NOT IN ( 'master', 'model', 'msdb', 'tempdb', 'northwind','pubs' )";
    }

    @Override
    public Object druidAnalyseSql(String sql) {
        //加载驱动
        String dbType = JdbcConstants.SQL_SERVER.toString();
        try {
            List<String> tableNameList = new ArrayList<>();
            //格式化输出
            String sqlResult = SQLUtils.format(sql, dbType);
            System.out.println("格式化后的sql:" + sqlResult);
            List<SQLStatement> stmtList = null;
            try {
                stmtList = SQLUtils.parseStatements(sql, dbType);
            } catch (ParserException e) {
                log.info("sql语法有误，请检查sql:{}", e);
                return null;
            }
            //解析sql，获取表名
            for (SQLStatement sqlStatement : stmtList) {
                SQLServerSchemaStatVisitor visitor = new SQLServerSchemaStatVisitor();
                sqlStatement.accept(visitor);

                //获取表名
                Map<TableStat.Name, TableStat> tables = visitor.getTables();
                log.info("druid解析sql表结果集:{}", tables + "");
                Set<TableStat.Name> tableNameSet = tables.keySet();
                for (TableStat.Name name : tableNameSet) {
                    String tableName = name.getName().trim();
                    if (StringUtils.isEmpty(tableName)) {
                        continue;
                    }
                    tableNameList.add(tableName);
                }

                //获取where条件
                List<TableStat.Condition> conditions = visitor.getConditions();
                log.info("解析sql后的where查询条件：{}", conditions + "");

                //获取字段以及别名
                List<DruidFieldInfoDTO> fieldList = new ArrayList<>();
                Set<String> set = new HashSet<>();
                List<SQLSelectItem> SourceBName = ((SQLSelectQueryBlock) ((SQLSelect) ((SQLSelectStatement) sqlStatement)
                        .getSelect())
                        .getQuery())
                        .getSelectList();
                for (SQLSelectItem item : SourceBName) {
                    DruidFieldInfoDTO fs = new DruidFieldInfoDTO();
                    if (item.getExpr() instanceof SQLPropertyExpr) {
                        SQLPropertyExpr itemex = (SQLPropertyExpr) item.getExpr();
                        SQLExprTableSource TableSource = (SQLExprTableSource) itemex.getResolvedOwnerObject();
                        fs.fieldName = itemex.getName();
                        fs.tableName = TableSource.getExpr().toString();
                        fs.alias = false;
                    } else {
                        System.out.println(item.getAlias() + "," + item.getExpr());
                        fs.fieldName = item.getAlias();
                        fs.alias = true;
                    }
                    fs.logic = item.getExpr().toString();
                    fieldList.add(fs);
                    set.add(item.getAlias());
                }
                ////System.out.println("解析结果："+JSON.toJSONString(fieldList));
                log.info("解析结果：{}", JSON.toJSONString(fieldList));
                return fieldList;
            }
        } catch (Exception e) {
            log.error("解析SQL异常：{}", e);
        }
        return null;
    }

}
