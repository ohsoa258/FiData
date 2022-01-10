package com.fisk.datamanagement.utils.druid;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import jdk.nashorn.internal.runtime.ParserException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author JianWenYang
 */
@Component
public class AnalysisSqlHelper {

    public List<String> AnalysisSql(String sql,String dbType)
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

}
