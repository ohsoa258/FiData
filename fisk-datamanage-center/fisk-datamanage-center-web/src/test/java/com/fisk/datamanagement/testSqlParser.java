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
        String sqlQuery = "WITH A1 AS(\n" +
                "SELECT\n" +
                "-- a.fidata_id,\n" +
                "-- a.fidata_create_time,\n" +
                "-- a.fidata_create_user,\n" +
                "-- a.fidata_update_time,\n" +
                "-- a.fidata_update_user,\n" +
                "-- a.fidata_del_flag\n" +
                "a.column_empid\n" +
                ",a.column_empno\n" +
                ",a.column_empname\n" +
                ",a.column_ename AS emp_ename\n" +
                ",a.column_usedname\n" +
                ",a.column_unitid as emp_unitid\n" +
                ",a.column_deptno\n" +
                ",a.column_costcenter\n" +
                ",a.column_statorgid\n" +
                "--,a.column_cardtype\n" +
                ",D2.codevalue as cardtype\n" +
                ",a.column_idcard\n" +
                ",a.column_idcard2\n" +
                "--,a.column_sex\n" +
                ",D3.codevalue as sex\n" +
                ",a.column_birthday\n" +
                "-- ,a.column_country\n" +
                ",D14.codevalue AS column_country\n" +
                "-- ,a.column_nationality\n" +
                ",D4.codevalue as nationality\n" +
                "-- ,a.column_politicalparty\n" +
                ",D5.codevalue as politicalparty\n" +
                ",a.column_joinpartdate\n" +
                "--,a.column_educationlevel\n" +
                ",D6.codevalue as educationlevel\n" +
                ",a.column_placeofbirth\n" +
                ",a.column_hometown\n" +
                ", D7.codevalue AS  registernature\n" +
                ",a.column_mobile\n" +
                ",a.column_email\n" +
                ",a.column_photo\n" +
                ",a.column_postcode\n" +
                ",a.column_postlevel\n" +
                "-- ,a.column_postgrade\n" +
                ",D13.codevalue AS postgrade\n" +
                ",a.column_posttitleid\n" +
                ",a.column_posttitle\n" +
                ",CASE WHEN column_probationstate='0'THEN '否'\n" +
                "WHEN column_probationstate='1'THEN '是'\n" +
                "ELSE NULL END AS  probationstate  -- 是否试用期? 0 否; 1 是\n" +
                "/*,CASE WHEN a.column_emptype='ET1'THEN '合同工'\n" +
                "WHEN a.column_emptype='ET2'THEN '劳务工(派遣)'\n" +
                "WHEN a.column_emptype='ET3'THEN '其他从业人员'\n" +
                "WHEN a.column_emptype='ET31'THEN '委派'\n" +
                "WHEN a.column_emptype='ET32'THEN '退休返聘'\n" +
                "WHEN a.column_emptype='ET33'THEN '协保'\n" +
                "WHEN a.column_emptype='ET34'THEN '实习'\n" +
                "WHEN a.column_emptype='ET35'THEN '灵活用工'\n" +
                "WHEN a.column_emptype='ET39' THEN '其他'\n" +
                "ELSE NULL END AS  emptype  -- 用工性质: ET1 合同工; ET2 劳务工(派遣); ET3 其他从业人员(ET31 委派, ET32 退休返聘, ET33 协保, ET34 实习,ET35 灵活用工, ET39 其他)\n" +
                "*/\n" +
                ",D8.codevalue AS emptype\n" +
                ",D1.codevalue AS status\n" +
                ",CASE WHEN a.column_poststatus='1'THEN '在岗'\n" +
                "WHEN a.column_poststatus='2'THEN '离岗'\n" +
                "WHEN a.column_poststatus='3'THEN '离退人员' -- 1-在岗; 2-离岗; 3-离退人员\n" +
                "ELSE NULL END AS poststatus\n" +
                "/* ,CASE WHEN a.column_contracttype='LW01'THEN '劳动合同'\n" +
                "WHEN a.column_contracttype='LW02'THEN '劳务合同'\n" +
                "WHEN a.column_contracttype='LW03'THEN '灵活用工合同'\n" +
                "ELSE NULL END AS contracttype     -- 合同类别(最新)：LW01 劳动合同; LW02 劳务合同; LW03 灵活用工合同\n" +
                "*/\n" +
                ",D9.codevalue AS contracttype\n" +
                ",a.column_contractstartdate\n" +
                ",a.column_contractenddate\n" +
                ",a.column_terminatedate\n" +
                "/*,CASE WHEN a.column_terminatetype='TC21'THEN '违规'\n" +
                "  WHEN a.column_terminatetype='TC22'THEN '合同终止'\n" +
                "  WHEN a.column_terminatetype='TC23'THEN '合同解除' \n" +
                "  WHEN a.column_terminatetype='TC25'THEN '退休' \n" +
                "  WHEN a.column_terminatetype='TC51'THEN '员工辞职' \n" +
                "  ELSE NULL END AS terminatetype     -- TC21 违规/TC22 合同终止/TC23 合同解除/TC25 退休/TC51 员工辞职/公司辞退/双方协商解除/员工到达法定退休年龄/员工死亡或失踪\n" +
                "  */\n" +
                "\n" +
                ",D10.codevalue as terminatetype\n" +
                ",CASE WHEN a.column_terminatestate='A'THEN '已登记'\n" +
                "WHEN a.column_terminatestate='B'THEN '已处理'\n" +
                "WHEN a.column_terminatestate='C'THEN '已退工'\n" +
                "WHEN a.column_terminatestate='D'THEN '已停社保'\n" +
                "WHEN a.column_terminatestate='E'THEN '已停薪'\n" +
                "ELSE NULL END AS terminatestate   -- 离职状态: A=已登记|B=已处理|C=已退工|D=已停社保|E=已停薪\n" +
                ",a.column_lastworkingdate\n" +
                ",a.column_driverlicenseno\n" +
                ",a.column_driverlicens2\n" +
                ",a.column_licensetype\n" +
                ",a.column_licensedatefrom\n" +
                ",a.column_licensedateto\n" +
                ",a.column_licensedistrict\n" +
                ",a.column_companyid\n" +
                ",b.column_unitid\n" +
                ",b.column_unitno\n" +
                ",b.column_unitname\n" +
                ",b.column_ename as unit_ename\n" +
                ",b.column_shortname\n" +
                ",b.column_shortcode\n" +
                ",b.column_superid\n" +
                ",b.column_superids\n" +
                "-- ,b.column_supername\n" +
                ",c.column_unitname as supername\n" +
                "/*,CASE WHEN b.column_unittype='BD1' THEN '企业'\n" +
                "WHEN b.column_unittype='BD2' THEN '事业'\n" +
                "WHEN b.column_unittype='BD3' THEN '机关'\n" +
                "WHEN b.column_unittype='BD4' THEN '主业'\n" +
                "WHEN b.column_unittype='BD5' THEN '多经'\n" +
                "ELSE NULL END AS unittype */\n" +
                ",D11.codevalue AS unittype\n" +
                "/*,CASE WHEN b.column_unitgrade='IH11' THEN '集团(总)公司'\n" +
                "WHEN b.column_unitgrade='IH12' THEN '区域中心'\n" +
                "WHEN b.column_unitgrade='IH13' THEN '公司'\n" +
                "WHEN b.column_unitgrade='IH14' THEN '部门'\n" +
                "WHEN b.column_unitgrade='IH23' THEN '室'\n" +
                "WHEN b.column_unitgrade='IH25' THEN '团'\n" +
                "ELSE NULL END AS  unitgrade   -- IH11 集团(总)公司; IH12 区域中心; IH13 公司; IH14 部门; IH23 室; IH25 团\n" +
                "*/\n" +
                ",D12.codevalue AS unitgrade\n" +
                ",split_part(b.column_superids,',',2) AS dept_1 -- 一级部门\n" +
                ",split_part(b.column_superids,',',3) AS dept_2 -- 二级部门\n" +
                ",split_part(b.column_superids,',',4) AS dept_3 -- 三级部门\n" +
                ",split_part(b.column_superids,',',5) AS dept_4 -- 四级部门\n" +
                ",split_part(b.column_superids,',',6) AS dept_5 -- 五级部门\n" +
                "\n" +
                "FROM qs_mdm_pg.public.mdm_staff_HR_Employee a\n" +
                "LEFT JOIN qs_mdm_pg.public.mdm_staff_gcorgunit b\n" +
                "ON a.column_unitid=b.column_unitid\n" +
                "LEFT JOIN qs_mdm_pg.public.mdm_staff_gcorgunit c\n" +
                "ON b.column_superid=c.column_unitid\n" +
                "LEFT JOIN ( select codeid,codevalue,codetype from qs_dmp_ods.qshr_prod.gc_basecodevalue where hudi_isdeleted='0' and codetype='ES' ) D1\n" +
                "ON A.column_status =D1.codeid\n" +
                "LEFT JOIN ( select codeid,codevalue,codetype from qs_dmp_ods.qshr_prod.gc_basecodevalue where hudi_isdeleted='0' and codetype='CY' ) D2\n" +
                "ON A.column_cardtype =D2.codeid\n" +
                "LEFT JOIN ( select codeid,codevalue,codetype from qs_dmp_ods.qshr_prod.gc_basecodevalue where hudi_isdeleted='0' and codetype='AX' ) D3\n" +
                "ON A.column_sex =D3.codeid\n" +
                "LEFT JOIN ( select codeid,codevalue,codetype from qs_dmp_ods.qshr_prod.gc_basecodevalue where hudi_isdeleted='0' and codetype='AE' ) D4\n" +
                "ON A.column_nationality =D4.codeid\n" +
                "LEFT JOIN ( select codeid,codevalue,codetype from qs_dmp_ods.qshr_prod.gc_basecodevalue where hudi_isdeleted='0' and codetype='AT' ) D5\n" +
                "ON A.column_politicalparty =D5.codeid\n" +
                "LEFT JOIN ( select codeid,codevalue,codetype from qs_dmp_ods.qshr_prod.gc_basecodevalue where hudi_isdeleted='0' and codetype='AM' ) D6\n" +
                "ON A.column_educationlevel =D6.codeid\n" +
                "LEFT JOIN ( select codeid,codevalue,codetype from qs_dmp_ods.qshr_prod.gc_basecodevalue where hudi_isdeleted='0' and codetype='HN' ) D7\n" +
                "ON A.column_registernature =D7.codeid\n" +
                "LEFT JOIN ( select codeid,codevalue,codetype from qs_dmp_ods.qshr_prod.gc_basecodevalue where hudi_isdeleted='0' and codetype='ET' ) D8\n" +
                "ON A.column_emptype =D8.codeid\n" +
                "LEFT JOIN ( select codeid,codevalue,codetype from qs_dmp_ods.qshr_prod.gc_basecodevalue where hudi_isdeleted='0' and codetype='LW' ) D9\n" +
                "ON A.column_contracttype =D9.codeid\n" +
                "LEFT JOIN ( select codeid,codevalue,codetype from qs_dmp_ods.qshr_prod.gc_basecodevalue where hudi_isdeleted='0' and codetype='TC' ) D10\n" +
                "ON A.column_terminatetype =D10.codeid\n" +
                "LEFT JOIN ( select codeid,codevalue,codetype from qs_dmp_ods.qshr_prod.gc_basecodevalue where hudi_isdeleted='0' and codetype='BD' ) D11\n" +
                "ON b.column_unittype =D11.codeid\n" +
                "LEFT JOIN ( select codeid,codevalue,codetype from qs_dmp_ods.qshr_prod.gc_basecodevalue where hudi_isdeleted='0' and codetype='IH' ) D12\n" +
                "ON b.column_unitgrade =D12.codeid\n" +
                "LEFT JOIN ( select codeid,codevalue,codetype from qs_dmp_ods.qshr_prod.gc_basecodevalue where hudi_isdeleted='0' and codetype IN ('PG','ZN') ) D13\n" +
                "ON a.column_postgrade =D13.codeid\n" +
                "LEFT JOIN ( select codeid,codevalue,codetype from qs_dmp_ods.qshr_prod.gc_basecodevalue where hudi_isdeleted='0' and codetype='AD' ) D14\n" +
                "ON A.column_country =D14.codeid\n" +
                ")\n" +
                "SELECT\n" +
                "A1.column_empid\n" +
                ",A1.column_empno\n" +
                ",A1.column_empname\n" +
                ",A1.emp_ename\n" +
                ",A1.column_usedname\n" +
                ",A1.emp_unitid\n" +
                ",A1.column_deptno\n" +
                ",A1.column_costcenter\n" +
                ",A1.column_statorgid\n" +
                ",A1.cardtype\n" +
                ",A1.column_idcard\n" +
                ",A1.column_idcard2\n" +
                ",A1.sex\n" +
                ",A1.column_birthday\n" +
                ",A1.column_country\n" +
                ",A1.nationality\n" +
                ",A1.politicalparty\n" +
                ",A1.column_joinpartdate\n" +
                ",A1.educationlevel\n" +
                ",A1.column_placeofbirth\n" +
                ",A1.column_hometown\n" +
                ",A1.registernature\n" +
                ",A1.column_mobile\n" +
                ",A1.column_email\n" +
                ",A1.column_photo\n" +
                ",A1.column_postcode\n" +
                ",A1.column_postlevel\n" +
                ",A1.postgrade\n" +
                ",A1.column_posttitleid\n" +
                ",A1.column_posttitle\n" +
                ",A1.probationstate  -- 是否试用期? 0 否; 1 是\n" +
                ",A1.emptype\n" +
                ",A1.status\n" +
                ",A1.poststatus\n" +
                ",A1.contracttype\n" +
                ",A1.column_contractstartdate\n" +
                ",A1.column_contractenddate\n" +
                ",A1.column_terminatedate\n" +
                ",A1.terminatetype\n" +
                ",A1.terminatestate   -- 离职状态: A=已登记|B=已处理|C=已退工|D=已停社保|E=已停薪\n" +
                ",A1.column_lastworkingdate\n" +
                ",A1.column_driverlicenseno\n" +
                ",A1.column_driverlicens2\n" +
                ",A1.column_licensetype\n" +
                ",A1.column_licensedatefrom\n" +
                ",A1.column_licensedateto\n" +
                ",A1.column_licensedistrict\n" +
                ",A1.column_companyid\n" +
                ",A1.column_unitid\n" +
                ",A1.column_unitno\n" +
                ",A1.column_unitname\n" +
                ",A1.unit_ename\n" +
                ",A1.column_shortname\n" +
                ",A1.column_shortcode\n" +
                ",A1.column_superid\n" +
                ",A1.column_superids\n" +
                ",A1.supername\n" +
                ",A1.unittype\n" +
                ",A1.unitgrade\n" +
                ",A1.dept_1 -- 一级部门\n" +
                ",CASE WHEN NVL(A1.dept_1,'')<>'' AND NVL(A1.dept_2,'')='' THEN A1.emp_unitid ELSE A1.dept_2 END AS dept_2 -- 二级部门\n" +
                ",CASE WHEN NVL(A1.dept_2,'')<>'' AND NVL(A1.dept_3,'')='' THEN A1.emp_unitid ELSE A1.dept_3 END AS dept_3 -- 三级部门\n" +
                ",CASE WHEN NVL(A1.dept_3,'')<>'' AND NVL(A1.dept_4,'')='' THEN A1.emp_unitid ELSE A1.dept_4 END AS dept_4 -- 四级部门\n" +
                ",CASE WHEN NVL(A1.dept_4,'')<>'' AND NVL(A1.dept_5,'')='' THEN A1.emp_unitid ELSE A1.dept_5 END AS dept_5 -- 五级部门\n" +
                ",B1.column_unitname AS deptname_1\n" +
                ",B2.column_unitname AS deptname_2\n" +
                ",B3.column_unitname AS deptname_3\n" +
                ",B4.column_unitname AS deptname_4\n" +
                ",B5.column_unitname AS deptname_5\n" +
                "FROM A1\n" +
                "LEFT JOIN qs_mdm_pg.public.mdm_staff_gcorgunit B1\n" +
                "ON A1.dept_1=B1.column_unitid\n" +
                "LEFT JOIN qs_mdm_pg.public.mdm_staff_gcorgunit B2\n" +
                "ON CASE WHEN NVL(A1.dept_1,'')<>'' AND NVL(A1.dept_2,'')='' THEN A1.emp_unitid ELSE A1.dept_2 END=B2.column_unitid\n" +
                "LEFT JOIN qs_mdm_pg.public.mdm_staff_gcorgunit B3\n" +
                "ON CASE WHEN NVL(A1.dept_2,'')<>'' AND NVL(A1.dept_3,'')='' THEN A1.emp_unitid ELSE A1.dept_3 END=B3.column_unitid\n" +
                "LEFT JOIN qs_mdm_pg.public.mdm_staff_gcorgunit B4\n" +
                "ON CASE WHEN NVL(A1.dept_3,'')<>'' AND NVL(A1.dept_4,'')='' THEN A1.emp_unitid ELSE A1.dept_4 END=B4.column_unitid\n" +
                "LEFT JOIN qs_mdm_pg.public.mdm_staff_gcorgunit B5\n" +
                "ON CASE WHEN NVL(A1.dept_4,'')<>'' AND NVL(A1.dept_5,'')='' THEN A1.emp_unitid ELSE A1.dept_5 END=B5.column_unitid";
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