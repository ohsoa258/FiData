package com.fisk.chartvisual.util.dbhelper;


import com.fisk.chartvisual.entity.HierarchyPO;
import com.fisk.chartvisual.enums.MatrixElemTypeEnum;
import org.apache.commons.lang.StringUtils;
import java.util.List;

/**
 * 通过列行值获取mdx语句
 * @author JinXingWang
 */
public class MdxHelper {

    public  String GetMdxByColumnRowValue(List<HierarchyPO> columns,List<HierarchyPO> rows,List<HierarchyPO> values,List<String> filters,String cubeName){
        String columnMdx=getColumnMdx(columns,rows,values);
        String rowMdx=getRowMdx(rows);
        String whereMdx=getWhereMdx(values,filters);
        return replaceMdxTemplateByColumnRowValue(columnMdx,rowMdx,whereMdx,cubeName);
    }

    /**
     * 获取列mdx语句
     * @param columns 列
     * @param rows 行
     * @param values 值
     * @return mdx语句
     */
    private  String getColumnMdx(List<HierarchyPO> columns,List<HierarchyPO> rows,List<HierarchyPO> values){
        int valuesSize=values.size();
        String mdxColumn="";
        if (columns.size()==0){
            if(valuesSize>1)
            {
                //无列并且值大于1 值代替列 成为列
                mdxColumn="{"+ StringUtils.join(values,",")+"}";
            }else {
                //无列并且无值 行代替列 成为列
                mdxColumn=getHierarchicalMdx(rows, MatrixElemTypeEnum.ROW,null);
            }
        }else
        {
            mdxColumn= getHierarchicalMdx(columns,MatrixElemTypeEnum.COLUMN,values);
        }
        if ( mdxColumn.length()>0){
            mdxColumn= "NON EMPTY "+mdxColumn+" DIMENSION PROPERTIES PARENT_UNIQUE_NAME,HIERARCHY_UNIQUE_NAME ON COLUMNS";
        }
        return mdxColumn;
    }

    /**
     * 获取行 mdx语句
     * @param rows 列
     * @return mdx语句
     */
    private  String getRowMdx(List<HierarchyPO> rows){
        String mdxRow=getHierarchicalMdx(rows, MatrixElemTypeEnum.ROW,null);
        if(mdxRow.length()>0){
            return  ", NON EMPTY "+mdxRow+" DIMENSION PROPERTIES PARENT_UNIQUE_NAME,HIERARCHY_UNIQUE_NAME ON ROWS";
        }else {
            return "";
        }

    }

    /**
     * 获取where mdx语句
     * @param values 值
     * @param filters 筛选
     * @return mdx语句
     */
    private  String getWhereMdx(List<HierarchyPO> values,List<String> filters){
        if (values!=null&&(values.size()==1||filters.size()>0)){
            StringBuilder whereMdxSb=new StringBuilder();
            whereMdxSb.append("WHERE(");
            whereMdxSb.append(StringUtils.join(filters,","));
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
     * @param hierarchyPOS 列或行
     * @param matrixElemTypeEnum 类型
     * @param values 值
     * @return mdx 语句
     */
    private  String getHierarchicalMdx(List<HierarchyPO> hierarchyPOS, MatrixElemTypeEnum matrixElemTypeEnum,List<HierarchyPO> values){
        int hierarchySize=hierarchyPOS.size();
        StringBuilder hMdxSb=new StringBuilder();
        if(hierarchySize==0){
            return "";
        }else if(hierarchySize==1){
            hMdxSb.append("Hierarchize({DrilldownLevel({");
            hMdxSb.append(hierarchyPOS.get(0).uniqueNameAll);
            hMdxSb.append("},,,INCLUDE_CALC_MEMBERS)})");
        }else {
            hMdxSb.append(getHierarchicalGreaterThanTwoMdx(hierarchyPOS));
        }
        //多值,并且是列元素,使用join语法.
        if (matrixElemTypeEnum==MatrixElemTypeEnum.COLUMN&& values!=null&& values.size()>1){
            hMdxSb.insert(0, "CrossJoin(");
            for (HierarchyPO value: values ){
                hMdxSb.append(","+value.uniqueName+"");
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
    private  String getHierarchicalGreaterThanTwoMdx(List<HierarchyPO> hierarchyPOS){
        StringBuilder hMdxSb=new StringBuilder();
        int hierarchySize=hierarchyPOS.size();
        StringBuilder dmBeforeMdxSb=new StringBuilder();
        StringBuilder dmAfterMdxSb=new StringBuilder();
        StringBuilder cjMdxSb=new StringBuilder();
        cjMdxSb.append("{(");
        for (int i =0;i<hierarchySize;i++ ){
            if (i<hierarchySize-1){
                dmBeforeMdxSb.append("DrilldownMember(");
                dmAfterMdxSb.append(", "+hierarchyPOS.get(i).uniqueNameAllMember+"\n" +
                        ", "+hierarchyPOS.get(i+1).uniqueName+"\n" +
                        ")");
            }
            if (i>=1){
                if (i==1){
                    cjMdxSb.append(hierarchyPOS.get(i).uniqueNameAll);
                }else {
                    cjMdxSb.append(","+hierarchyPOS.get(i).uniqueNameAll);
                }
            }
        }
        cjMdxSb.append(")}");
        hMdxSb.append("Hierarchize(");
        hMdxSb.append(dmBeforeMdxSb);
        hMdxSb.append("CrossJoin(" +
                "{"+hierarchyPOS.get(0).uniqueNameAll+" ,"+hierarchyPOS.get(0).uniqueNameAllMember+"}" +
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
        return "SELECT " +
                columnMdx+"\n"+
                rowMdx+" \n"+
                " FROM "+cubeName+" \n"+
                whereMdx+"\n"+
                " CELL PROPERTIES VALUE, FORMAT_STRING, LANGUAGE, BACK_COLOR, FORE_COLOR, FONT_FLAGS";
    }
}
