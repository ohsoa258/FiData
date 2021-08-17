package com.fisk.chartvisual.util.dbhelper;
import com.fisk.chartvisual.dto.ChartQueryFilter;
import com.fisk.chartvisual.dto.ChartQueryObjectSsas;
import com.fisk.chartvisual.dto.ColumnDetailsSsas;
import com.fisk.chartvisual.enums.MatrixElemTypeEnum;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通过列行值筛选器获取mdx语句
 * @author JinXingWang
 */
@Service
public class MdxHelper {

    /**
     * 通过列行值获取mdx语句
     * @param query  查询条件
     * @param cubeName 模型名称
     * @return mdx语句
     */
    public  String GetMdxByColumnRowValue(ChartQueryObjectSsas query, String cubeName){
        List<ColumnDetailsSsas> columns=filterList(query.columnDetails,MatrixElemTypeEnum.COLUMN);
        List<ColumnDetailsSsas> rows=filterList(query.columnDetails,MatrixElemTypeEnum.ROW);
        List<ColumnDetailsSsas> values=filterList(query.columnDetails,MatrixElemTypeEnum.VALUE);
        String columnMdx=getColumnMdx(columns,values);
        String rowMdx=getRowMdx(rows,columns,values);
        String whereMdx=getWhereMdx(values,query.queryFilters);
        return replaceMdxTemplateByColumnRowValue(columnMdx,rowMdx,whereMdx,cubeName);
    }

    /**
     * 获取列mdx语句
     * @param columns 列
     * @param values 值
     * @return mdx语句
     */
    private  String getColumnMdx(List<ColumnDetailsSsas> columns,List<ColumnDetailsSsas> values){
        StringBuilder mdxColumn=new StringBuilder();
        mdxColumn.append("NON EMPTY");
        if (columns.size()==0){
            mdxColumn.append("{");
            mdxColumn.append(StringUtils.join(values.stream().map(en->en.uniqueName).collect(Collectors.toList()), ","));
            mdxColumn.append("}");
        }else
        {
            mdxColumn.append(getHierarchicalMdx(columns,MatrixElemTypeEnum.COLUMN,values));
        }
        mdxColumn.append("DIMENSION PROPERTIES PARENT_UNIQUE_NAME,HIERARCHY_UNIQUE_NAME ON COLUMNS");

        return mdxColumn.toString();
    }

    /**
     * 获取行mdx语句
     * @param rows 列
     * @return mdx语句
     */
    private  String getRowMdx(List<ColumnDetailsSsas> rows,List<ColumnDetailsSsas> columns,List<ColumnDetailsSsas> values){
        // 无列 并且 值大于2
        if(columns.size()==0&&values.size()<2){
            return "";
        }else
        {
            return " , "+getHierarchicalMdx(rows, MatrixElemTypeEnum.ROW,null)+" DIMENSION PROPERTIES PARENT_UNIQUE_NAME,HIERARCHY_UNIQUE_NAME ON ROWS";
        }
    }

    /**
     * 获取where mdx语句（切片器）
     * @param values 值
     * @param wheres where
     * @return mdx语句
     */
    private  String getWhereMdx(List<ColumnDetailsSsas> values, List<ChartQueryFilter> wheres){
        //where条件中只能包含元组
        if (values!=null&&(values.size()==1||wheres.size()>0)){
            StringBuilder whereMdxSb=new StringBuilder();
            whereMdxSb.append("WHERE(");
            whereMdxSb.append(StringUtils.join(wheres,","));
            if(wheres.size()>0){
                whereMdxSb.append(" , ");
            }
            if (values.size()==1){
                whereMdxSb.append(values.get(0).uniqueName);
            }
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
    private  String getHierarchicalMdx(List<ColumnDetailsSsas> hierarchyPos, MatrixElemTypeEnum matrixElemTypeEnum,List<ColumnDetailsSsas> values){
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
            hMdxSb.append(getHierarchicalGreaterThanTwoMdx(hierarchyPos));
        }
        //多值,并且是列元素,使用join语法.
        if (matrixElemTypeEnum==MatrixElemTypeEnum.COLUMN&& values!=null&& values.size()>1){
            hMdxSb.insert(0, "CrossJoin(");
            for (ColumnDetailsSsas value: values ){
                hMdxSb.append(",");
                hMdxSb.append(value.uniqueName);
            }
            hMdxSb.append(")");
        }
        return  hMdxSb.toString();
    }

    /**
     *获取列或行长度大于二的部分mdx语句
     * @param hierarchyPOS 维度
     * @return mdx语句
     */
    private  String getHierarchicalGreaterThanTwoMdx(List<ColumnDetailsSsas> hierarchyPOS){
        StringBuilder hMdxSb=new StringBuilder();
        int hierarchySize=hierarchyPOS.size();
        StringBuilder dmBeforeMdxSb=new StringBuilder();
        StringBuilder dmAfterMdxSb=new StringBuilder();
        //所有层级
        StringBuilder cjMdxSb=new StringBuilder();
        cjMdxSb.append("{(");
        for (int i =0;i<hierarchySize;i++ ){
            if (i<hierarchySize-1){
                dmBeforeMdxSb.append("DrilldownMember(");
                dmAfterMdxSb.append(", "+hierarchyPOS.get(i).uniqueName+".["+hierarchyPOS.get(i).name+"].AllMembers\n" +
                        ", "+hierarchyPOS.get(i+1).uniqueName+"\n" +
                        ")");
            }
            if (i>=1){
                if (i==1){
                    cjMdxSb.append(hierarchyPOS.get(i).uniqueName+".[All]");
                }else {
                    cjMdxSb.append(",");
                    cjMdxSb.append(hierarchyPOS.get(i).uniqueName);
                    cjMdxSb.append(".[All]");
                }
            }
        }
        cjMdxSb.append(")}");
        hMdxSb.append("Hierarchize(");
        hMdxSb.append(dmBeforeMdxSb);
        hMdxSb.append("CrossJoin(" +
                "{"+hierarchyPOS.get(0).uniqueName+".[All] ,"+hierarchyPOS.get(0).uniqueName+".["+hierarchyPOS.get(0).name+"].AllMembers}" +
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
    private  String replaceMdxTemplateByColumnRowValue(String columnMdx,String rowMdx,String whereMdx,String cubeName){
        return "SELECT \n" +
                columnMdx + "\n" +
                rowMdx + "\n" +
                " FROM [" +
                cubeName +
                "] \n" +
                whereMdx + "\n" +
                " CELL PROPERTIES VALUE, FORMAT_STRING, LANGUAGE, BACK_COLOR, FORE_COLOR, FONT_FLAGS";
    }

    private   List<ColumnDetailsSsas> filterList(List<ColumnDetailsSsas> columnDetailsSsas,MatrixElemTypeEnum matrixElemType){
       return columnDetailsSsas.stream()
                .filter(p -> matrixElemType==p.matrixElemType)
                .collect(Collectors.toList());

    }
}
