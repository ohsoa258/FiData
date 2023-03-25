package com.fisk.common.service.sqlparser;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.fastjson.JSON;
import com.fisk.common.service.sqlparser.model.TableMetaDataObject;
import com.fisk.common.service.sqlparser.model.TableTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gy
 * @version 1.0
 * @description 解析SQL方法
 * @date 2022/12/6 17:30
 */
@Slf4j
public class SqlParserV1 implements ISqlParser {

    private int hierarchy = 0;

    @Override
    public List<TableMetaDataObject> getDataTableBySql(String sql, DbType dbType) throws Exception {
        List<TableMetaDataObject> res = new ArrayList<>();
        String tmp=sql.replace("collate","")
                .replace("sql_latin1_general_cp1_ci_as","")
                .replace("COLLATE","")
                .replace("SQL_Latin1_General_CP1_CI_AS","");
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(tmp, dbType).stream()
                .filter(s -> s instanceof SQLSelectStatement)
                .collect(Collectors.toList());
        if (sqlStatements.size() == 0) {
            throw new Exception("SQL解析失败，未获取到有效SQL代码段");
        }
        for (SQLStatement statement : sqlStatements) {
            // 获取查询
            SQLSelectQuery query = ((SQLSelectStatement) statement).getSelect().getQuery();
            if (query instanceof SQLUnionQuery) {
                log.debug("Union All查询 START");
                SQLUnionQuery sqlUnionQuery = (SQLUnionQuery) query;
                log.debug("Union All查询 TABLESOURCE START");
                SQLTableSource tableSource = sqlUnionQuery.getFirstQueryBlock().getFrom();
                log.debug("TABLESOURCE 查寻成功"+ JSON.toJSONString(tableSource));
                // 获取查询中，出现的所有表
                log.debug("Union All 获取查询中，出现的所有表START");
                SqlParserUtils.getAllTableSource(hierarchy, res, sqlUnionQuery, tableSource, null);
                log.debug("Union All 获取查询中，出现的所有表END");
                //throw new Exception("暂时不支持Union All解析");
            } else if (query instanceof SQLSelectQueryBlock) {
                log.info("一元查询");
                SQLSelectQueryBlock blockQuery = (SQLSelectQueryBlock) query;
                // get table source
                SQLTableSource tableSource = blockQuery.getFrom();
                // 获取查询中，出现的所有表
                SqlParserUtils.getAllTableSource(hierarchy, res, blockQuery, tableSource, null);
            }
        }
        return res.stream()
                .filter(e -> e.tableType == TableTypeEnum.Expr)
                .collect(Collectors.toList());
    }
}
