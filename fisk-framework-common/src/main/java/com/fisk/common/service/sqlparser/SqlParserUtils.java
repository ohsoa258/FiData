package com.fisk.common.service.sqlparser;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerSelectQueryBlock;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.sqlparser.model.FieldMetaDataObject;
import com.fisk.common.service.sqlparser.model.TableInfo;
import com.fisk.common.service.sqlparser.model.TableMetaDataObject;
import com.fisk.common.service.sqlparser.model.TableTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author gy
 * @version 1.0
 * @description 解析SQL帮助类
 * @date 2022/12/6 17:30
 */
@Slf4j
public class SqlParserUtils {


    /**
     * 解析SQL语句，获取所有出现的表
     */
    public static void getAllTableSource(int hierarchy,
                                         List<TableMetaDataObject> res,
                                         SQLSelectQuery query,
                                         SQLTableSource tableSource,
                                         TableMetaDataObject lastTable) {
        hierarchy++;
        TableInfo tableInfo;
        String msg = "";
        // 如果TableSource类型是 一元类型 ，那么说明不需要在递归查找表
        if (tableSource instanceof SQLExprTableSource) {
            SQLExprTableSource table = (SQLExprTableSource) tableSource;
            tableInfo = getTableInfo(table);
            msg = String.format("表：【%s】，表深度：【%s】，表类型：【一元】", tableInfo.name, hierarchy);
            res.add(
                    buildTableNode(hierarchy,
                            query,
                            tableInfo,
                            msg,
                            Objects.isNull(lastTable) ? "" : lastTable.id));
            log.info(msg);
        }
        // 说明TableSource不是 一元类型 ，需要继续向里层找所有表
        else {
            // 如果TableSource类型是 子查询类型 ，说明还需要继续递归查询
            if (tableSource instanceof SQLSubqueryTableSource) {
                SQLSubqueryTableSource subTableSource = (SQLSubqueryTableSource) tableSource;
                SQLSelectQuery subQuery = subTableSource.getSelect().getQuery();
                SQLTableSource childTableSource = getTableSource(subQuery);
                tableInfo = getTableInfo(subTableSource);
                msg = String.format("表：【%s】，表深度：【%s】，表类型：【子查询表】", tableInfo.name, hierarchy);
                TableMetaDataObject node = buildTableNode(hierarchy,
                        query,
                        tableInfo,
                        msg,
                        Objects.isNull(lastTable) ? "" : lastTable.id);
                getAllTableSource(hierarchy, res, query, childTableSource, node);
                res.add(node);
                log.info(msg);
            }
            // 如果TableSource是 UnionAll查询类型 ，需要获取 UnionAll 的上下两个表，分别解析
            else if (tableSource instanceof SQLUnionQueryTableSource) {
                SQLUnionQueryTableSource unionTableSource = (SQLUnionQueryTableSource) tableSource;
                tableInfo = getTableInfo(unionTableSource);
                msg = String.format("表：【%s】，表深度：【%s】，表类型：【Union all查询表】", tableInfo.name, hierarchy);
                TableMetaDataObject node = buildTableNode(hierarchy,
                        query,
                        tableInfo,
                        msg,
                        Objects.isNull(lastTable) ? "" : lastTable.id);
                res.add(node);
                log.info(msg);
                List<SQLSelectQuery> relations = unionTableSource.getUnion().getRelations();
                for (SQLSelectQuery itemQuery : relations) {
                    SQLTableSource childTableSource = getTableSource(itemQuery);
                    getAllTableSource(hierarchy, res, itemQuery, childTableSource, node);
                }
            }
            // 如果TableSource是 Join 查询类型，需要获取左右两个表
            else if (tableSource instanceof SQLJoinTableSource) {
                SQLJoinTableSource joinTableSource = (SQLJoinTableSource) tableSource;
                tableInfo = getTableInfo(joinTableSource);
                msg = String.format("表：【%s】，表深度：【%s】，表类型：【Join查询表】", tableInfo.name, hierarchy);
                TableMetaDataObject node = buildTableNode(hierarchy,
                        query,
                        tableInfo,
                        msg,
                        Objects.isNull(lastTable) ? "" : lastTable.id);
                res.add(node);
                log.info(msg);

                SQLSelectQuery rightQuery = JoinTableHandler(joinTableSource.getRight());
                getAllTableSource(hierarchy, res, rightQuery, joinTableSource.getRight(), node);
                SQLSelectQuery leftQuery = JoinTableHandler(joinTableSource.getLeft());
                getAllTableSource(hierarchy, res, leftQuery, joinTableSource.getLeft(), node);
            }
        }
        hierarchy--;
    }

    public static SQLSelectQuery JoinTableHandler(SQLTableSource table) {
        // 一元表类型
        if (table instanceof SQLExprTableSource) {
            return null;
        }
        // Union All
        else if (table instanceof SQLUnionQueryTableSource) {
            return null;
        }
        // 子查询
        else if (table instanceof SQLSubqueryTableSource) {
            return ((SQLSubqueryTableSource) table).getSelect().getQuery();
        }
        // join查询
        else if (table instanceof SQLJoinTableSource) {
            return null;
        }
        return null;
    }

    /**
     * 构建 TableMetaDataObject 对象
     */
    public static TableMetaDataObject buildTableNode(Integer hierarchy,
                                                     SQLSelectQuery query,
                                                     TableInfo tableInfo,
                                                     String details,
                                                     String lastNodeId) {
        return TableMetaDataObject.builder()
                .id(UUID.randomUUID().toString())
                .name(tableInfo.name)
                .alias(tableInfo.alias)
                .details(details)
                .schema(tableInfo.schema)
                .lastNodeId(lastNodeId)
                .hierarchy(hierarchy)
                .tableType(tableInfo.tableType)
                .fields(buildFieldNode(query))
                .build();
    }

    /**
     * 构建 FieldMetaDataObject 对象
     */
    public static List<FieldMetaDataObject> buildFieldNode(SQLSelectQuery query) {
        ArrayList<FieldMetaDataObject> fieldNodes = new ArrayList<>();
        List<SQLSelectItem> items = getSelectItem(query);
        if (!CollectionUtils.isEmpty(items)) {
            // 判断字段列表是不是 *
            if (items.size() == 1 && "*".equals(items.get(0).getExpr().toString())) {
                fieldNodes.add(
                        FieldMetaDataObject
                                .builder()
                                .id(UUID.randomUUID().toString())
                                .name("*")
                                .alias("*")
                                .owner("")
                                .build());
            } else {
                items.forEach(e -> {
                    String[] fieldInfo = getFieldName(e.getExpr());
                    fieldNodes.add(
                            FieldMetaDataObject
                                    .builder()
                                    .id(UUID.randomUUID().toString())
                                    .name(fieldInfo[0])
                                    .alias(e.getAlias())
                                    .owner(fieldInfo[1])
                                    .build());
                });
            }
        }
        return fieldNodes;
    }

    /**
     * 获取字段列表
     */
    public static List<SQLSelectItem> getSelectItem(SQLSelectQuery query) {
        if (query == null) {
            return null;
        }
        if (query instanceof SQLServerSelectQueryBlock) {
            return ((SQLServerSelectQueryBlock) query).getSelectList();
        } else {
            log.info("未知类型的Query");
            return null;
        }
    }

    public static String[] getFieldName(SQLExpr expr) {
        String[] arr = new String[2];
        // 表名.字段名
        if (expr instanceof SQLPropertyExpr) {
            SQLPropertyExpr propertyExpr = (SQLPropertyExpr) expr;
            arr[0] = propertyExpr.getName();
            arr[1] = propertyExpr.getOwner().toString();
        }
        // 最简单的查询，没有表名.
        else if (expr instanceof SQLIdentifierExpr) {
            SQLIdentifierExpr identifierExpr = (SQLIdentifierExpr) expr;
            arr[0] = identifierExpr.getName();
            arr[1] = "";
        }
        // 子查询
        else if (expr instanceof SQLQueryExpr) {
            SQLQueryExpr queryExpr = (SQLQueryExpr) expr;
            List<TableMetaDataObject> tableNodes = new ArrayList<>();
            Integer h = 0;
            getAllTableSource(h, tableNodes, queryExpr.subQuery.getQuery(), getTableSource(queryExpr.subQuery.getQuery()), new TableMetaDataObject());
        }
        return arr;
    }

    public static SQLTableSource getTableSource(SQLSelectQuery query) {
        // 一元类型查询，直接返回TableSource
        if (query instanceof SQLSelectQueryBlock) {
            return ((SQLSelectQueryBlock) query).getFrom();
        } else {
            log.info("目前只支持一元类型的查询");
            return null;
        }
    }

    public static TableInfo getTableInfo(SQLTableSource table) {
        TableInfo tableInfo = new TableInfo();
        // Union All表类型
        if (table instanceof SQLUnionQueryTableSource) {
            tableInfo.name = "Union All表";
            tableInfo.alias = table.getAlias();
            tableInfo.tableType = TableTypeEnum.UNION;
        }
        // 一元表类型
        else if (table instanceof SQLExprTableSource) {
            SQLExprTableSource exprTable = (SQLExprTableSource) table;
            tableInfo.name = exprTable.getTableName();
            tableInfo.alias = table.getAlias();
            tableInfo.tableType = TableTypeEnum.Expr;
            tableInfo.schema = exprTable.getSchema();
        }
        // 子查询
        else if (table instanceof SQLSubqueryTableSource) {
            tableInfo.name = "子查询表";
            tableInfo.alias = table.getAlias();
            tableInfo.tableType = TableTypeEnum.SUBQUERY;
        }
        // join查询
        else if (table instanceof SQLJoinTableSource) {
            tableInfo.name = "join查询表";
            tableInfo.alias = table.getAlias();
            tableInfo.tableType = TableTypeEnum.JOIN;
        }
        // 未知表类型
        else {
            tableInfo.name = "未知表类型";
            tableInfo.alias = "";
            tableInfo.tableType = TableTypeEnum.NONE;
        }
        return tableInfo;
    }

    /**
     * 数据库类型
     *
     * @param driveType
     * @param sqlScript
     * @return
     */
    public static List<TableMetaDataObject> sqlDriveConversion(String driveType, String sqlScript) {
        DbType dbType;
        if ("mysql".equals(driveType)) {
            dbType = DbType.mysql;
        } else if ("oracle".equals(driveType)) {
            dbType = DbType.oracle;
        } else if ("postgresql".equals(driveType)) {
            dbType = DbType.postgresql;
        } else if ("sqlserver".equals(driveType)) {
            dbType = DbType.sqlserver;
        } else {
            return new ArrayList<>();
        }

        List<TableMetaDataObject> res;
        try {
            ISqlParser parser = SqlParserFactory.parser(ParserVersion.V1);
            res = parser.getDataTableBySql(sqlScript, dbType);
        } catch (Exception e) {
            log.error("【sql解析失败】,{}", e);
            throw new FkException(ResultEnum.SQL_PARSING);
        }

        return res;
    }

    public static List<TableMetaDataObject> sqlDriveConversionName(String driveType, String sqlScript) {
        List<TableMetaDataObject> tableMetaDataObjects = sqlDriveConversion(driveType, sqlScript);
        if (CollectionUtils.isEmpty(tableMetaDataObjects)) {
            return tableMetaDataObjects;
        }
        tableMetaDataObjects.stream().forEach(e -> {
            if (!StringUtils.isEmpty(e.schema)) {
                e.name = e.schema + "." + e.name;
            }
            e.setName(e.name.replace("[", "").replace("]", ""));
        });
        return tableMetaDataObjects;
    }

}
