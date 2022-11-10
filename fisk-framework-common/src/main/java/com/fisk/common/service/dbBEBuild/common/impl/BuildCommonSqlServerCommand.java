package com.fisk.common.service.dbBEBuild.common.impl;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.common.BuildCommonHelper;
import com.fisk.common.service.dbBEBuild.common.IBuildCommonSqlCommand;
import com.fisk.common.service.dbBEBuild.common.dto.DruidFieldInfoDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public List<DruidFieldInfoDTO> druidAnalyseSql(String sql) {
        //加载驱动
        String dbType = JdbcConstants.SQL_SERVER.toString();
        //连接druid驱动
        List<SQLStatement> stmtList = BuildCommonHelper.connectionStatement(dbType, sql);
        try {
            //解析sql，获取表名
            for (SQLStatement sqlStatement : stmtList) {
                SQLServerSchemaStatVisitor visitor = new SQLServerSchemaStatVisitor();
                sqlStatement.accept(visitor);

                //获取字段以及别名
                List<DruidFieldInfoDTO> fieldList = new ArrayList<>();
                Set<String> set = new HashSet<>();
                List<SQLSelectItem> SourceBName = ((SQLSelectQueryBlock) ((SQLSelect) ((SQLSelectStatement) sqlStatement)
                        .getSelect())
                        .getQuery())
                        .getSelectList();

                for (SQLSelectItem item : SourceBName) {
                    DruidFieldInfoDTO fs = new DruidFieldInfoDTO();
                    //解析表名
                    SQLPropertyExpr itemEx = (SQLPropertyExpr) item.getExpr();
                    SQLExprTableSource TableSource = (SQLExprTableSource) itemEx.getResolvedOwnerObject();
                    fs.tableName = TableSource.getExpr().toString();
                    if (fs.tableName.indexOf(".") > 1) {
                        String[] split = fs.tableName.split(".");
                        fs.tableName = split[1];
                        fs.schema = split[0];
                    }
                    //是否存在别名
                    if (!StringUtils.isEmpty(item.getAlias())) {
                        fs.alias = item.getAlias();
                    }
                    fs.fieldName = itemEx.getName();
                    fs.logic = item.getExpr().toString();
                    fieldList.add(fs);
                    set.add(item.getAlias());
                }
                ////System.out.println("解析结果："+JSON.toJSONString(fieldList));
                log.info("解析结果：{}", JSON.toJSONString(fieldList));
                return fieldList;
            }
        } catch (Exception e) {
            log.error("Druid解析SQL异常：{}", e);
            throw new FkException(ResultEnum.DRUID_ERROR);
        }
        return null;
    }

    @Override
    public String buildColumnInfo(String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append("TABLE_NAME AS table_name,");
        str.append("CHARACTER_MAXIMUM_LENGTH AS column_length,");
        str.append("COLUMN_NAME AS column_name,");
        str.append("DATA_TYPE AS data_type ");
        str.append("INFORMATION_SCHEMA.COLUMNS ");
        str.append("WHERE ");
        str.append("TABLE_NAME in");
        str.append("(");
        str.append(tableName);
        str.append(")");
        return str.toString();
    }

}
