package com.fisk.chartvisual.util.dbhelper.buildmdx;
import com.fisk.chartvisual.dto.ChartQueryFilter;
import com.fisk.chartvisual.dto.ChartQueryObjectSsas;
import com.fisk.chartvisual.dto.ColumnDetailsSsas;
import com.fisk.chartvisual.enums.MatrixElemTypeEnum;
import com.fisk.chartvisual.vo.DataServiceResult;
import org.apache.commons.lang.StringUtils;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.Position;
import org.olap4j.metadata.Member;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通过列行值筛选器获取mdx语句
 * @author JinXingWang
 */
@Service
public class BaseBuildMdx    {

    /**
     * 通过列行值获取mdx语句
     * @param query  查询条件
     * @param cubeName 模型名称
     * @return mdx语句
     */
    public  String buildMdx(ChartQueryObjectSsas query, String cubeName){
        List<ColumnDetailsSsas> columns=filterList(query.columnDetails,MatrixElemTypeEnum.COLUMN);
        //是否下钻
        List<ColumnDetailsSsas> rows=filterList(query.columnDetails,MatrixElemTypeEnum.ROW);
        if (query.chartDrillDown.isChartDrillDown){
            List<ColumnDetailsSsas> drillDownRow= new ArrayList<>();
            drillDownRow.add(rows.get(query.chartDrillDown.level-1));
            rows=drillDownRow;
        }
        List<ColumnDetailsSsas> values=filterList(query.columnDetails,MatrixElemTypeEnum.VALUE);
        String columnMdx=buildColumnMdx(columns,values);
        String rowMdx=buildRowMdx(rows,columns);
        String whereMdx=buildWhereMdx(values,query.queryFilters);
        return replaceMdxTemplateByColumnRowValue(columnMdx,rowMdx,whereMdx,cubeName);
    }

    /**
     * 生成列mdx语句
     * @param columns 列
     * @param values 值
     * @return mdx语句
     */
    public  String buildColumnMdx(List<ColumnDetailsSsas> columns,List<ColumnDetailsSsas> values){
        StringBuilder mdxColumn=new StringBuilder();
        mdxColumn.append("NON EMPTY ");
        if (columns.size()==0){
            mdxColumn.append(" {");
            mdxColumn.append(StringUtils.join(values.stream().map(en->en.uniqueName).collect(Collectors.toList()), ","));
            mdxColumn.append("} ");
        }else
        {
            mdxColumn.append(buildHierarchicalMdx(columns,MatrixElemTypeEnum.COLUMN,values));
        }
        mdxColumn.append(" DIMENSION PROPERTIES PARENT_UNIQUE_NAME,HIERARCHY_UNIQUE_NAME ON COLUMNS ");
        return mdxColumn.toString();
    }

    /**
     * 获取行mdx语句
     * @param rows 列
     * @return mdx语句
     */
    public  String buildRowMdx(List<ColumnDetailsSsas> rows,List<ColumnDetailsSsas> columns){
        // 无列 并且 值大于2
        if(rows.size()==0){
            return "";
        }else
        {
            return " , NON EMPTY "+buildHierarchicalMdx(rows, MatrixElemTypeEnum.ROW,null)+" DIMENSION PROPERTIES PARENT_UNIQUE_NAME,HIERARCHY_UNIQUE_NAME ON ROWS";
        }
    }

    /**
     * 获取where mdx语句（切片器）
     * @param values 值
     * @param wheres where
     * @return mdx语句
     */
    public  String buildWhereMdx(List<ColumnDetailsSsas> values, List<ChartQueryFilter> wheres){
        //where条件中只能包含元组
        if (wheres.size()>0){
            StringBuilder whereMdxSb=new StringBuilder();
            whereMdxSb.append("WHERE(");
            for(ChartQueryFilter where: wheres){
                String valuesStr= where.value.stream().map(e-> where.columnName+".&["+e+"]").collect(Collectors.joining(" , "," { "," } "));
                whereMdxSb.append(valuesStr);
                whereMdxSb.append(",");
            }
            whereMdxSb.deleteCharAt(whereMdxSb.length()-1);
            whereMdxSb.append(")");
            return  whereMdxSb.toString();
        }else {
            return "";
        }
    }

    /**
     * 获取列或行部分mdx语句
     * @param hierarchyPos 列或行
     * @param matrixElemTypeEnum 类型
     * @param values 值
     * @return mdx 语句
     */
    public  String buildHierarchicalMdx(List<ColumnDetailsSsas> hierarchyPos, MatrixElemTypeEnum matrixElemTypeEnum,List<ColumnDetailsSsas> values){
        int hierarchySize=hierarchyPos.size();
        StringBuilder hMdxSb=new StringBuilder();
        if(hierarchySize==0){
            return "";
        }else if(hierarchySize==1){
            hMdxSb.append("Hierarchize({DrilldownLevel({");
            hMdxSb.append(hierarchyPos.get(0).uniqueName);
            hMdxSb.append(".[All]");
            hMdxSb.append("},,,INCLUDE_CALC_MEMBERS)})");
        }else {
            hMdxSb.append(buildHierarchicalGreaterThanTwoMdx(hierarchyPos));
        }
        //多值,并且是列元素,使用join语法.
        if (matrixElemTypeEnum==MatrixElemTypeEnum.COLUMN&& values!=null&& values.size()>1){
            hMdxSb.insert(0, "CrossJoin(");
            hMdxSb.append(",{");
            hMdxSb.append(values.stream().map(e->e.uniqueName).collect(Collectors.joining(",")));
            hMdxSb.append("})");
        }
        return  hMdxSb.toString();
    }

    /**
     *获取列或行长度大于二的部分mdx语句
     * @param hierarchyPoS 维度
     * @return mdx语句
     */
    public  String buildHierarchicalGreaterThanTwoMdx(List<ColumnDetailsSsas> hierarchyPoS){
        StringBuilder hMdxSb=new StringBuilder();
        int hierarchySize=hierarchyPoS.size();
        StringBuilder dmBeforeMdxSb=new StringBuilder();
        StringBuilder dmAfterMdxSb=new StringBuilder();
        //所有层级
        StringBuilder cjMdxSb=new StringBuilder();
        cjMdxSb.append("{(");
        for (int i =0;i<hierarchySize;i++ ){
            if (i<hierarchySize-1){
                dmBeforeMdxSb.append("DrilldownMember(");
                dmAfterMdxSb.append(", "+hierarchyPoS.get(i).uniqueName+".["+hierarchyPoS.get(i).name+"].AllMembers\n" +
                        ", "+hierarchyPoS.get(i+1).uniqueName+"\n" +
                        ")");
            }
            if (i>=1){
                if (i==1){
                    cjMdxSb.append(hierarchyPoS.get(i).uniqueName+".[All]");
                }else {
                    cjMdxSb.append(",");
                    cjMdxSb.append(hierarchyPoS.get(i).uniqueName);
                    cjMdxSb.append(".[All]");
                }
            }
        }
        cjMdxSb.append(")}");
        hMdxSb.append("Hierarchize(");
        hMdxSb.append(dmBeforeMdxSb);
        hMdxSb.append("CrossJoin(" +
                "{"+hierarchyPoS.get(0).uniqueName+".[All] ,"+hierarchyPoS.get(0).uniqueName+".["+hierarchyPoS.get(0).name+"].AllMembers}" +
                ","+cjMdxSb.toString()+")");
        hMdxSb.append(dmAfterMdxSb);
        hMdxSb.append(")");
        return hMdxSb.toString();
    }

    /**
     * 通过列，行，条件替换mdx模板
     * @param columnMdx 列mdx语句
     * @param rowMdx 行mdx语句
     * @param whereMdx 条件mdx语句
     * @param cubeName 模型名称
     * @return 完整mdx语句
     */
    public  String replaceMdxTemplateByColumnRowValue(String columnMdx,String rowMdx,String whereMdx,String cubeName){
        return "SELECT \n" +
                columnMdx + "\n" +
                rowMdx + "\n" +
                " FROM [" +
                cubeName +
                "] \n" +
                whereMdx + "\n" +
                " CELL PROPERTIES VALUE, FORMAT_STRING, LANGUAGE, BACK_COLOR, FORE_COLOR, FONT_FLAGS";
    }

    /**
     * 获取行列值筛选条件
     * @param columnDetailsSsas
     * @param matrixElemType
     * @return
     */
    public   List<ColumnDetailsSsas> filterList(List<ColumnDetailsSsas> columnDetailsSsas,MatrixElemTypeEnum matrixElemType){
       return columnDetailsSsas.stream()
                .filter(p -> matrixElemType==p.matrixElemType)
                .collect(Collectors.toList());
    }

    /**
     * 解析 cellSet
     * @param cellSet 单元格集合
     * @return map集合
     */
    public DataServiceResult getDataByAnalyticalCellSet(CellSet cellSet){
        DataServiceResult rs=new DataServiceResult();
        List<Map<String,Object>> mapList=new ArrayList<>();
        for (Position row :cellSet.getAxes().get(1)){
            Map<String,Object> map=new HashMap<>();
            for (Position column:cellSet.getAxes().get(0)){
                final Cell cell=cellSet.getCell(column,row);
                for (Member member:column.getMembers()){
                    map.put(member.getName(),cell.getValue());
                }
            }
            mapList.add(map);
        }
        rs.data=mapList;
        return rs;
    }
}
