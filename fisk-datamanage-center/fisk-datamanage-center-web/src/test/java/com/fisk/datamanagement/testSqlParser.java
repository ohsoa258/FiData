package com.fisk.datamanagement;

import com.fisk.common.service.sqlparser.SqlParserUtils;
import com.fisk.common.service.sqlparser.model.TableMetaDataObject;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class testSqlParser {
    public static void main(String[] args) {
        String sqlQuery = "SELECT\n" +
                "    t1.frame_no as code,\n" +
                "    null as name,\n" +
                "    t3.name as car_type,\n" +
                "    t4.name as car_brand,\n" +
                "    null as car_size,\n" +
                "    t1.model as model,\n" +
                "    t1.car_color as car_color,\n" +
                "    t1.car_numbers as car_numbers,\n" +
                "    t2.car_native as car_native,\n" +
                "    t1.car_grade as car_grade,\n" +
                "    if(regexp_replace(NVL(t1.country_origin,''),'[\\\\\\s]+|[\\\\\\,]','')='','其它',regexp_replace(NVL(country_origin,''),'[\\\\\\s]+|[\\\\\\,]','')) as car_country_origin,\n" +
                "    --t1.country_origin as car_country_origin,\n" +
                "    t1.engine_number as engine_number,\n" +
                "    t1.engine_type as engine_type,\n" +
                "    t5.name as car_fuel_type,\n" +
                "    null as chassis_model,\n" +
                "    null as chassis_number,\n" +
                "    t1.displacement as displacement,\n" +
                "    t1.power as power,\n" +
                "    t1.tyre_number as tyre_number,\n" +
                "    t1.tyre_size as car_tyre_size,\n" +
                "    null as spare_tire_number,\n" +
                "    null as spare_tire_size,\n" +
                "    concat(t1.car_long , \"/\" , t1.car_paragraph  , \"/\" , t1.car_high)   as overall_size,\n" +
                "    t1.seats as seats,\n" +
                "    null as battery_range,\n" +
                "    t2.dep as dep,\n" +
                "    t2.dep_id as dep_id ,\n" +
                "    '出租' as business_unit\n" +
                "FROM qs_dmp_ods.qs_erp_dev.car_base as t1 \n" +
                "LEFT JOIN qs_dmp_ods.qs_erp_dev.car_operation as t2\n" +
                "ON t1.frame_no = t2.frame_no\n" +
                "LEFT JOIN(SELECT CODE,NAME FROM qs_dmp_ods.qs_erp_dev.c_common_dictionary_item where dictionary_type = 'CAR_TYPE') t3\n" +
                "  ON t1.CAR_TYPE=t3.CODE\n" +
                "LEFT JOIN (SELECT CODE,NAME FROM qs_dmp_ods.qs_erp_dev.c_common_dictionary_item where dictionary_type = 'CAR_BRAND') t4\n" +
                "  ON t1.brand=t4.CODE\n" +
                "LEFT JOIN (SELECT CODE,NAME FROM qs_dmp_ods.qs_erp_dev.c_common_dictionary_item where dictionary_type = 'CAR_FUEL_TYPE') t5\n" +
                "  ON t1.fuel_type=t5.CODE\n" +
                "UNION ALL\n" +
                "SELECT \n" +
                "    t1.Frame_number_hudi as code ,\n" +
                "    null as name ,\n" +
                "    null as car_type ,\n" +
                "    t1.Brand_hudi as car_brand ,\n" +
                "    null as car_size ,\n" +
                "    t1.Vehicle_model_hudi as model ,\n" +
                "    t1.Vehicle_color_hudi as car_color ,\n" +
                "    t1.License_plate_number_hudi as car_numbers ,\n" +
                "    t1.Registered_City_hudi as car_native ,\n" +
                "    CAST((year(current_date()) - year(t1.Factory_date_hudi) + 1) AS CHAR) as car_grade ,\n" +
                "    t1.Imported_and_domestically_produced_hudi as car_country_origin ,\n" +
                "    t1.Engine_number_hudi as engine_number ,\n" +
                "    t1.Engine_model_hudi as engine_type ,\n" +
                "    if(regexp_replace(NVL(Oil_type_hudi,''),'[\\\\\\s]+|[\\\\\\,]','')='','其它',regexp_replace(NVL(Oil_type_hudi,''),'[\\\\\\s]+|[\\\\\\,]','')) as car_fuel_type ,\n" +
                "    --t1.Oil_type_hudi as car_fuel_type ,\n" +
                "    t1.Chassis_Model_hudi as chassis_model ,\n" +
                "    t1.chassis_number_hudi as chassis_number ,\n" +
                "    t1.displacement_hudi as displacement ,\n" +
                "    t1.maximum_power_hudi as power ,\n" +
                "    t1.Number_of_tires_hudi as tyre_number ,\n" +
                "    t1.Tire_specifications_hudi as car_tyre_size ,\n" +
                "    null as spare_tire_number ,\n" +
                "    null as spare_tire_size ,\n" +
                "    t1.Length_hudi as overall_size ,\n" +
                "    null as seats ,\n" +
                "    null as battery_range ,\n" +
                "    t2.department_hudi as dep,\n" +
                "    t2.id_hudi as dep_id ,\n" +
                "   '租赁' as business_unit\n" +
                "FROM \n" +
                "    qs_dmp_ods.cx_cdxt_data_jtcs.dbo_zlgl_clzl as t1 \n" +
                "LEFT JOIN qs_dmp_ods.cx_cdxt_data_jtcs.dbo_zlgl_bumen as t2 ON t1.department_hudi = t2.department_hudi\n" +
                "    WHERE t1.rank=1 AND t1.Frame_number_hudi not in (SELECT frame_no FROM qs_dmp_ods.qs_erp_dev.car_base)";
//        String sqlQuery2="WITH cte AS (SELECT column1 FROM table1) SELECT column1 FROM cte WHERE column2 = 'value'";
//        List<TableMetaDataObject> res = SqlParserUtils.sqlDriveConversionName(1, "postgresql", sqlQuery);

        try {
            Statement statement = CCJSqlParserUtil.parse(sqlQuery);

            if (statement instanceof Select) {
                Select selectStatement = (Select) statement;
                TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
                List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
                System.out.println("Tables: " + tableList);
                // 获取第一个SELECT的元数据信息
                SelectBody selectBody = selectStatement.getSelectBody();
                processSelectBody(selectBody);
            }

        } catch (JSQLParserException e) {
            e.printStackTrace();
        }

    }

    private static void processSelectBody(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            processPlainSelect(plainSelect, null);
        } else if (selectBody instanceof SetOperationList) {
            SetOperationList setOperationList = (SetOperationList) selectBody;
            for (SelectBody select : setOperationList.getSelects()) {
                processSelectBody(select);
            }
        }
    }

    private static void processPlainSelect(PlainSelect plainSelect, String alias) {
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        for (SelectItem selectItem : selectItems) {
            if (selectItem instanceof SelectExpressionItem) {
                SelectExpressionItem expressionItem = (SelectExpressionItem) selectItem;
                Expression expression = expressionItem.getExpression();
                String columnAlias = expressionItem.getAlias() != null ? expressionItem.getAlias().getName() : null;
                processExpression(expression, alias, columnAlias);
            }
        }

        // 处理FROM子句
        processFromItem(plainSelect.getFromItem(), alias);

        // 处理JOIN子句
        List<Join> joins = plainSelect.getJoins();
        if (joins != null) {
            for (Join join : joins) {
                processFromItem(join.getRightItem(), alias);
            }
        }
    }

    private static void processExpression(Expression expression, String tableAlias, String columnAlias) {
        if (expression instanceof Column) {
            Column column = (Column) expression;
            Table table = (Table) column.getTable();
            String tableName = getRealTableName(table, tableAlias);
            String columnName = column.getColumnName();

            System.out.println("Table: " + tableName + ", Column: " + columnName + ", Alias: " + columnAlias);
        } else if (expression instanceof SubSelect) {
            // 处理子查询
            SubSelect subSelect = (SubSelect) expression;
            processSelectBody(subSelect.getSelectBody());
        } else if (expression instanceof Function) {
            //处理函数
//            Function fun = (Function) expression;

        } else {
            processExpression(expression);
        }
    }

    private static void processFromItem(FromItem fromItem, String parentAlias) {
        if (fromItem instanceof Table) {
            Table table = (Table) fromItem;
            String tableName = getRealTableName(table, parentAlias);
            System.out.println("Table: " + tableName);
        } else if (fromItem instanceof SubSelect) {
            // 处理子查询
            SubSelect subSelect = (SubSelect) fromItem;
            processSelectBody(subSelect.getSelectBody());
        }
    }

    private static String getRealTableName(Table table, String parentAlias) {
        if (table != null) {
            String tableName = table.getName();
            String tableAlias = table.getAlias() != null ? table.getAlias().getName() : parentAlias;
            return (tableAlias != null) ? tableAlias : tableName;
        }
        return null;
    }


    private static void processExpression(Expression expression) {
        expression.accept(new ExpressionVisitorAdapter() {
            @Override
            public void visit(Function function) {
                System.out.println("Function Name: " + function.getName());
                System.out.println("Parameters: " + function.getParameters());
            }

            @Override
            public void visit(CastExpression cast) {
                System.out.println("CAST to: " + cast.getType());
                System.out.println("Expression inside CAST:");
                processExpression(cast.getLeftExpression());
            }

            @Override
            public void visit(Column column) {
                System.out.println("Column: " + column.getColumnName());
            }

            @Override
            public void visit(Addition addition) {
                System.out.println("Addition Operation");
                processExpression(addition.getLeftExpression());
                processExpression(addition.getRightExpression());
            }

            // 可以添加其他表达式类型的处理

        });
    }
}