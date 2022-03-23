package com.fisk.datamanagement.utils.druid;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import com.fisk.datamanagement.dto.druid.FieldStructureDTO;
import jdk.nashorn.internal.runtime.ParserException;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author JianWenYang
 */
@Component
public class AnalysisSqlHelper {

    public List<String> AnalysisTableSql(String sql, String dbType)
    {
        List<String> tableNameList = new ArrayList<>();
        try {
            //格式化输出
            String sqlResult = SQLUtils.format(sql, dbType);
            System.out.println("格式化后的sql:"+sqlResult);
            List<SQLStatement> stmtList = null;
            try {
                stmtList = SQLUtils.parseStatements(sql, dbType);
            } catch (ParserException e) {
                System.out.println("sql语法有误，请检查sql");
                return tableNameList;
            }
            for (SQLStatement sqlStatement : stmtList) {
                SQLServerSchemaStatVisitor visitor=new SQLServerSchemaStatVisitor();
                sqlStatement.accept(visitor);
                //获取表名
                Map<TableStat.Name, TableStat> tables = visitor.getTables();
                System.out.println("druid解析sql的结果集:"+tables+"");
                Set<TableStat.Name> tableNameSet = tables.keySet();
                for (TableStat.Name name : tableNameSet) {
                    String tableName = name.getName();
                    if (tableName != null && tableName.length() > 0 && tableName.trim().length() > 0) {
                        tableNameList.add(tableName);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return tableNameList;
    }

    public List<FieldStructureDTO> AnalysisColumnSql(String sql, String dbType)
    {
        List<FieldStructureDTO> columnList = new ArrayList<>();
        try {
            //格式化输出
            String sqlResult = SQLUtils.format(sql, dbType);
            System.out.println("格式化后的sql:"+sqlResult);
            List<SQLStatement> stmtList = null;
            try {
                stmtList = SQLUtils.parseStatements(sql, dbType);
            } catch (ParserException e) {
                System.out.println("sql语法有误，请检查sql");
                return columnList;
            }
            for (SQLStatement sqlStatement : stmtList) {
                SQLServerSchemaStatVisitor visitor=new SQLServerSchemaStatVisitor();
                sqlStatement.accept(visitor);
                //获取where条件
                List<TableStat.Condition> conditions = visitor.getConditions();
                System.out.println("解析sql后的查询条件："+conditions+"");
                //别名  *详细字段 2022年01月05日18:06:30 Dennyhui
                Set<String> set = new HashSet<>();
                List<FieldStructureDTO> fields=new ArrayList<>();
                List<SQLSelectItem> SourceBName = ((SQLSelectQueryBlock)((SQLSelect)((SQLSelectStatement)sqlStatement).getSelect()).getQuery()).getSelectList();
                for(SQLSelectItem item : SourceBName){
                    if(item.getExpr() instanceof SQLPropertyExpr) {
                        SQLPropertyExpr itemex = (SQLPropertyExpr) item.getExpr();
                        SQLExprTableSource TableSource=(SQLExprTableSource)itemex.getResolvedOwnerObject();
                        System.out.println(TableSource.getExpr()+"."+itemex.getName());
                        FieldStructureDTO fs=new FieldStructureDTO();
                        fs.fieldName=itemex.getName();
                        fs.source=TableSource.getExpr().toString();
                        fs.logic=item.getExpr().toString();
                        fs.alias=false;
                        fields.add(fs);
                    }else {
                        FieldStructureDTO fs=new FieldStructureDTO();
                        System.out.println(item.getAlias()+","+item.getExpr());
                        fs.fieldName=item.getAlias();
                        fs.alias=true;
                        fs.logic=item.getExpr().toString();
                        fields.add(fs);
                    }
                    set.add(item.getAlias());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return columnList;
    }

}
